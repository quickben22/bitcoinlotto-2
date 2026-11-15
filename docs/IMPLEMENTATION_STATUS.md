# BitcoinLotto Rewrite — Implementation Status

Last updated: 2026-11-12

This document records the completed rewrite work, verification results, differences from
`docs/REWRITE_PLAN.md`, and the remaining work needed before release.

---

## 1. Status at a Glance

| Milestone | Status | Notes |
|---|---|---|
| M0 — Preserve original / parity checklist / upload key | Not complete | Original APK and assets are preserved under `legacy/`; tagging, parity checklist, and upload keystore are still outstanding. |
| M1 — Project scaffold / build / tooling | Partial | New Gradle project and version catalog are in place. Hilt, Detekt, Ktlint, and CI are not implemented. |
| M2 — Crypto | Complete | Real secp256k1 via BouncyCastle, Base58Check, WIF, P2PKH, fixtures, and vectors pass. |
| M3 — Database / Room / Bloom / migration | Partial | Prepacked SQLite lookup and asset tests work. Room, Bloom filter, build-time DB generator, and legacy database migration are not implemented. |
| M4 — Search engine and visible-app controller | Mostly complete | Sequential point addition, in-memory address-hash lookup, encrypted results and manual checkpoint/resume are implemented. Search access uses rewarded-ad gates at 6M and 60M cumulative keys. |
| M5 — Compose Stats and Solver | Complete | Both screens, navigation, guessed dialogs, privacy entry point, and original background are implemented. |
| M6 — Ads / privacy / secure storage | Complete | UMP, test ads, production AdMob IDs, `FLAG_SECURE`, sensitive clipboard, encrypted checkpoints, and backup exclusion are implemented. |
| M7 — Release candidate | Partial | R8 release build is configured with production ad-unit IDs. Signing, signed AAB, device matrix, and Play Console declarations remain. |

---

## 2. Current Stack and Architecture

- Application ID: `com.quickben22.bitcoinlotto`
- Version: `versionCode 11`, `versionName 1.0.0`
- SDKs: `minSdk 24`, `targetSdk 36`, `compileSdk 37`
- Install Android SDK Platform 37 through Android Studio's SDK Manager to build this project.
- Build: AGP `9.1.1`, Kotlin `2.4.21`, Gradle `9.3.1`, Java/Kotlin target 17
- UI: Jetpack Compose, Material 3, Navigation Compose
- Async: Kotlin coroutines and `StateFlow`
- Persistence: DataStore Preferences and prepacked SQLite
- Security: Tink `1.24.0` with Android Keystore
- Ads: Google Mobile Ads `25.5.0` and UMP `4.0.0`
- Modules:
  - `:app` — UI, search service, ads, manual dependency container
  - `:core:crypto` — pure JVM Bitcoin cryptography
  - `:core:data` — SQLite repository, encrypted checkpoint store

Dependency injection is manual through `AppContainer`; Hilt is not used.

---

## 3. Completed Work

### 3.1 Project and build

- Replaced the legacy Java/XML application with a Kotlin/Compose project.
- Added `gradle/libs.versions.toml`, Kotlin DSL build files, and the three-module layout.
- Preserved the original application under `legacy/`.
- Configured debug/release AdMob placeholders.
- Added release R8 shrinking and resource shrinking.
- Added a release pre-build guard that fails when production AdMob IDs are absent.
- Use AGP's built-in Kotlin support for Android modules.
- Added `dataExtractionRules` and disabled backup.

### 3.2 Bitcoin cryptography

- Implemented secp256k1 public-key derivation with BouncyCastle.
- Implemented Base58Check encoding/decoding.
- Implemented P2PKH address generation for compressed and uncompressed public keys.
- Implemented WIF encoding/decoding with compression-marker preservation.
- Added private-key validation for zero, curve order, values above the curve order, and wrong lengths.
- Added known-answer tests, including private key `1`.
- Added three fixture key pairs and their six addresses.

### 3.3 Address database

- Moved the prepacked database to `core/data/src/main/assets/blockchain.sqlite`.
- Implemented installation of the asset into the app database directory with a version marker.
- Implemented read-only exact address lookup against table `bitcoin(_id, value)`.
- The shipped asset contains only addresses that are still dormant: status `still_funded` with a current balance at or above the snapshot balance, per `docs/zombie-bitcoins.md`.
- Build an in-memory hash160 index on first search; exact address lookups remain available through the `_id` query.
- Implemented row-count and cohort tests: the count is `82,086`, a still-dormant address is present, and a partially spent address, a ghost wallet, an emptied address, and a never-funded fixture address are all absent.
- Current tested row count: `82,086` (7.1 MB asset, down from `92,083`).
- Lookup intentionally ignores the `value` column.

### 3.4 Search

- Implemented sequential 32-byte private-key increment with carry.
- Reuse the previous elliptic-curve point via point addition and compare public-key hashes without Base58 encoding on misses.
- The starting key is checked before the first increment.
- Both compressed and uncompressed addresses are checked.
- Implemented stop conditions for user stop, limit reached, address found, and invalid key.
- Track cumulative searched keys; pause at 6 million until a rewarded ad is earned, pause again at 60 million, then unlock permanently after a second rewarded ad.
- Implemented conflated progress updates at roughly 4 Hz.
- Persist checkpoints every two seconds and on user stop or application backgrounding.
- Use an application-owned `VisibleSearchController` observed by `ProcessLifecycleOwner`; Stats/Solver navigation and configuration changes do not stop the search.
- Search only while the app is visible. Switching apps or locking the screen cancels computation and saves progress; returning never automatically resumes.
- Removed the search service, foreground-service permissions, search wake lock, notifications, and notification-permission prompt.
- Manifest-merger directives remove WorkManager's unused foreground service/permission from the ad dependency. Ordinary SDK scheduling and its transitive wake-lock permission remain.
- Resume manually from an encrypted checkpoint after backgrounding or process death. Abrupt OS termination can recover only the last completed periodic save.
- Save found keys encrypted before removing the checkpoint; display them in-app until dismissed.
- Added unit tests for hit-at-start, increment distance, variants, limits, checkpoints, and misses.

#### Foreground-service removal validation

- The focused controller, search engine, access policy, and Stats ViewModel run passed all 30 tests, including real process-lifecycle recreation/background transitions under Robolectric.
- Android debug lint completed without errors; debug APK and release AAB builds succeeded.
- Both the merged release manifest and the AAB base manifest contain no foreground-service permissions, search service, special-use subtype, or WorkManager foreground service.
- The generated `app/build/outputs/bundle/release/app-release.aab` is unsigned. Publishing still requires an upload-key-signed build and a versionCode above the Console maximum.
- Manual device verification is incomplete: the API 35 emulator launch failed in the unchanged `MainActivity` edge-to-edge setup with "Window couldn't find content container view"; the emulator subsequently became unavailable. No end-to-end device or Play approval claim is made.

### 3.5 Encrypted checkpoint storage

- Added `SearchCheckpointStore` backed by DataStore Preferences.
- Added versioned binary checkpoint serialization in `SearchCheckpointCodec`.
- Added Tink AES-256-GCM encryption through an Android Keystore master key.
- Store checkpoint and found-hit data as Base64 ciphertext.
- Migrate the older plaintext DataStore keys, then remove them.
- Treat missing ciphertext as “no checkpoint”; report corrupt or undecryptable data instead of starting a new search.
- Added tests for round-trip encryption, legacy migration, plaintext absence, corruption, and wrong-key handling.

### 3.6 UI

- Implemented the Stats screen:
  - Keys checked, duration, and key frequency.
  - Start/Stop button.
  - Resume-available indicator.
  - Hit dialog.
  - Banner ad.
- Implemented the Solver screen:
  - 64-hex input validation.
  - Set Private Key.
  - Secure random key generation.
  - Compressed/uncompressed address display.
  - Wallet-key display with warning.
  - Guessed-address dialog.
  - Info dialog.
  - Sensitive clipboard copies.
- Added Material 3 dark theme with the cyan `#B3FFFF` identity.
- Added navigation between Stats and Solver.
- Added the top-bar overflow with the single “Privacy options” action.

### 3.7 Original background

- Reused the original `background.jpg` from the legacy app.
- Moved it to `app/src/main/res/drawable-nodpi/background.jpg`.
- Added `AppBackground`, which draws the image full-screen with `ContentScale.Crop`.
- Applied a `0.45f` black scrim for readability.
- Wrapped the app scaffold and privacy dialog in `AppBackground`.
- Made the `Scaffold` and `TopAppBar` transparent.
- Made the bottom navigation bar translucent (`surface` at `0.82f` alpha).
- Visually verified the Stats screen on the emulator. The Solver screen uses `FLAG_SECURE`, so emulator screenshots appear black by design.

### 3.8 Security and privacy

- `android:allowBackup="false"` and `fullBackupContent="false"`.
- `dataExtractionRules` excludes cloud backup and device-to-device transfer.
- `FLAG_SECURE` is applied on screens showing private keys.
- Clipboard entries use `ClipDescription.EXTRA_IS_SENSITIVE`.
- Search checkpoints are encrypted at rest.
- The service is not exported; only `MainActivity` is exported.
- Request UMP consent once per process on activity creation, retrying after errors on a later activity creation; initialize and load ads only when UMP permits ad requests.
- Debug builds use Google's official test AdMob IDs; release builds use the Play-linked app ID `ca-app-pub-3535032153893847~2306137168`, banner unit `ca-app-pub-3535032153893847/3541547263`, and rewarded unit `ca-app-pub-3535032153893847/3485706949`. Gradle properties can override these defaults.
- AdMob app IDs use `~`; banner and rewarded ad-unit IDs use `/`. The submitted `...~2319490240` value is an app ID and is not used as an ad unit.

### 3.9 Tests

Current unit-test suites:

- `BitcoinCryptoTest`, `SearchCheckpointStoreTest`, and `SqliteAddressRepositoryTest`
- `CheckPrivateKeyUseCaseTest`, `SolverViewModelTest`, and `StatsViewModelTest`
- `SearchEngineTest`, `VisibleSearchControllerTest`, and `SearchBenchmarkTest`

---

## 4. Verification Performed

### 4.1 Unit tests

```powershell
.\gradlew.bat :core:crypto:test :core:data:testDebugUnitTest :app:testDebugUnitTest
```

Run this command to verify the current unit-test suites.

### 4.2 Lint

```powershell
.\gradlew.bat :app:lintDebug :core:data:lintDebug
```

Result: 0 errors.

- `:app`: 13 warnings.
- `:core:data`: 2 warnings.

Notable warnings:

- `targetSdk 36` is not the latest SDK.
- Newer Gradle and AGP versions are available.
- The historical partial wake-lock warning no longer applies: the search wake lock has been removed.
- Adaptive launcher icons lack a monochrome layer.
- Unused/duplicate launcher icon resources.
- Long vector path in the launcher foreground.
- Formatting string should be a plural.
- `SharedPreferences.commit()` and non-KTX edit usage in `SqliteAddressRepository`.
- Compose configuration width API deprecation in `AdBanner`.

### 4.3 Release build

```powershell
.\gradlew.bat assembleRelease -PadmobAppId=<test-app-id> -PadmobBannerUnitId=<test-banner-id>
```

Result: successful R8 release build using test IDs.

- Output: `app/build/outputs/apk/release/app-release-unsigned.apk`
- The APK is unsigned because no signing configuration exists.
- Running `assembleRelease` without the two Gradle properties fails with the intended clear error.

### 4.4 Emulator verification

Device: `Pixel_5` emulator.

Verified:

- App launches with the original background.
- UMP consent form appears in debug/test geography.
- Test banner ad loads on Stats.
- “Privacy options” is reachable and opens the UMP form.
- Search starts, updates progress, and stops.
- Checkpoint survives force-stop and resumes.
- `checkpoint_keyset_prefs.xml` is created.
- DataStore inspection found no 64-hex plaintext private keys.
- Stats layout and background render correctly.
- Solver screen is secure against screenshots.

The emulator was stopped after verification.

---

## 5. Differences from `docs/REWRITE_PLAN.md`

These are intentional or currently unimplemented deviations. They should be resolved by either
updating the plan or completing the implementation.

| Plan | Current implementation |
|---|---|
| ACINQ `secp256k1-kmp-jni-android` plus BouncyCastle | BouncyCastle only |
| Hilt + KSP | Manual `AppContainer` |
| Room entities and DAO | Raw read-only `SQLiteDatabase` |
| Build-time DB generator | Prebuilt `blockchain.sqlite` asset committed to the repo |
| Bloom filter plus batched SQLite verification | In-memory hash160 index built from the read-only SQLite asset |
| Search limit reachable from the UI | Engine/service support a limit, but Stats exposes no limit control |
| Legacy app database migration | Not implemented; only plaintext DataStore checkpoint migration exists |
| JDK 21 toolchain | Java/Kotlin target 17 |
| JUnit 5 | JUnit 4 |
| Detekt, Ktlint, Kover | Not configured |
| Bitbucket Pipelines CI | Not configured |
| Compose UI tests, instrumented tests, macrobenchmark | Not present in the current source tree |
| `compileSdk 36` | `compileSdk 37` |
| `.gradle-local/` and `.tools/` ignored | Currently untracked and not ignored |

The JVM benchmark compares the old scalar-multiplication/SQLite path with the new
sequential-point/hash-index path. Physical-device throughput and memory usage still need measurement.

---

## 6. Remaining Work

### P0 — Release blockers

1. **Obtain and configure the upload keystore**
   - Add the signing configuration.
   - Produce a signed AAB.
   - Verify the upgrade path from the legacy app with the same application ID and upload key.

2. **Verify production AdMob configuration**
   - Build a release artifact and confirm no Google test IDs remain.

3. **Complete M0**
   - Tag the original APK.
   - Record the legacy behavior/parity checklist.
   - Confirm the Play Console owner and upload-key availability.

4. **Complete Play Console declarations**
   - Upload a replacement AAB without foreground-service permissions; inspect its merged manifest first.
   - Use a versionCode greater than every previously uploaded version (source currently uses 13; check Console before publishing).
   - Review detected permissions and App content declarations for applicable release tracks. Removing permissions in source does not change already-uploaded artifacts.
   - No special-use description or video is required for a replacement artifact that does not request that permission. Other artifacts may still require declarations.
   - Data safety form for Advertising ID and ad-related data.
   - Confirm target API compliance.

### P1 — Correctness, performance, and release confidence

1. **Resolve the database/performance gap**
   - Measure the hash-index memory footprint and throughput on physical devices.
   - Decide whether a Bloom filter and batched lookup would be preferable on low-memory devices.

2. **Add the missing database pipeline**
   - Build-time generator that reproduces the `82,086`-row still-dormant asset from `docs/address-audit.csv`.
   - Validation that the asset matches the still-dormant set exactly, with no duplicates.
   - Legacy database migration if upgrade compatibility requires it.

3. **Add UI and device tests**
   - Compose tests for Stats and Solver.
   - Guessed-dialog tests.
   - Application visibility, rotation, navigation, and process-death/manual-resume tests.
   - API 24 and API 36 device/emulator runs.
   - Accessibility, font-scale, portrait/landscape, and tablet checks.

4. **Complete release verification**
   - Visible-app search, background/lock checkpoint saving, and explicit resume on return.
   - Battery and thermal observation.
   - Confirm the guessed dialog using a debug-injected repository: the fixture keys are zero-at-snapshot and are no longer in the shipped still-dormant asset.
   - Confirm no private keys in logcat or crash reports.

### P2 — Quality and maintainability

1. Add Detekt, Ktlint, Kover, and CI.
2. Add the coverage gates from the plan.
3. Add `.gradle-local/` and `.tools/` to `.gitignore`.
4. Address lint warnings:
   - Add monochrome launcher icons and remove duplicate/unused icon assets.
   - Fix the plural string.
   - Replace `commit()` and non-KTX preference editing.
   - Replace deprecated Compose width API.
   - Decide whether to move to the latest target SDK/AGP/Gradle.
5. Update `docs/REWRITE_PLAN.md` so it matches the actual architecture, or implement the remaining planned architecture.

---

## 7. Suggested Next Steps

1. Review this document with the project owner and decide whether to update the plan or close the implementation gaps.
2. Obtain the upload keystore.
3. Add signing and produce the first signed AAB.
4. Run the local device gate and the manual release checklist.
5. Complete the Play Console declarations.

---

## 8. Repository State

- The rewrite is currently uncommitted.
- The old Java/XML source is deleted from the working tree and preserved under `legacy/`.
- New Kotlin source is under `app/src/main/kotlin/` and `core/`.
- `legacy/app/release/app-release.apk` and `legacy/app/src/main/assets/blockchain.sqlite` are retained.
- Do not commit build outputs, `.gradle-local/`, `.tools/`, or machine-specific IDE state.

---

## 9. Key Files

| Area | File |
|---|---|
| Rewrite plan | `docs/REWRITE_PLAN.md` |
| App build / AdMob guard | `app/build.gradle.kts` |
| Dependency versions | `gradle/libs.versions.toml` |
| Manual DI | `app/src/main/kotlin/com/quickben22/bitcoinlotto/AppContainer.kt` |
| Crypto | `core/crypto/src/main/kotlin/com/quickben22/bitcoinlotto/crypto/` |
| SQLite repository | `core/data/src/main/kotlin/com/quickben22/bitcoinlotto/data/SqliteAddressRepository.kt` |
| Address database asset | `core/data/src/main/assets/blockchain.sqlite` |
| Checkpoint storage | `core/data/src/main/kotlin/com/quickben22/bitcoinlotto/data/SearchCheckpointStore.kt` |
| Checkpoint encryption | `core/data/src/main/kotlin/com/quickben22/bitcoinlotto/data/CheckpointCipher.kt` |
| Search engine | `app/src/main/kotlin/com/quickben22/bitcoinlotto/search/SearchEngine.kt` |
| Visible-app search controller | `app/src/main/kotlin/com/quickben22/bitcoinlotto/search/VisibleSearchController.kt` |
| Stats screen | `app/src/main/kotlin/com/quickben22/bitcoinlotto/feature/stats/StatsScreen.kt` |
| Solver screen | `app/src/main/kotlin/com/quickben22/bitcoinlotto/feature/solver/SolverScreen.kt` |
| App background | `app/src/main/kotlin/com/quickben22/bitcoinlotto/ui/components/AppBackground.kt` |
| Root UI / privacy menu | `app/src/main/kotlin/com/quickben22/bitcoinlotto/ui/BitcoinLottoRoot.kt` |
| UMP consent | `app/src/main/kotlin/com/quickben22/bitcoinlotto/ads/AdsConsentManager.kt` |
| Manifest / backup rules | `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/data_extraction_rules.xml` |
| Legacy app and APK | `legacy/` |
