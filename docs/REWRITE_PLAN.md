# Bitcoin Key Hunter — Rewrite Plan

Status: Approved plan, ready for implementation
Last updated: 2026-10-23

---

## 1. Purpose

Rewrite the existing Java/XML Android app as a modern Kotlin/Jetpack Compose application with correct
Bitcoin cryptography, a reliable foreground search service, and a test suite that proves the
"address guessed" flow end to end.

Scope is intentionally reduced: **Stats** and **Solver** screens only. Riddles, a settings screen,
and the hardcoded puzzle special case are removed.

---

## 2. Current app assessment

The current app (`com.quickben22.bitcoinlotto`) is a single-activity Java app using
`ViewPager` + three fragments.

### Critical findings

| Issue | Location | Impact |
|---|---|---|
| Public-key derivation is a stub | `CryptoClass.PrivToPub()` / `PrivToPub_compressed()` | No real secp256k1 math; the search cannot find real addresses |
| RIPEMD160 fallback truncates SHA-256 | `CryptoClass.PubHexToHash()` | Wrong hash160 on devices without RIPEMD160 |
| Non-cryptographic randomness | `CryptoClass.random()` uses `java.util.Random` | Predictable "random" keys |
| Raw thread started from a Fragment | `OneFragment.startCracking()` | Not lifecycle-aware; lost on process death |
| Artificial throughput cap | `Thread.sleep(1)` in `CrackingClass.run()` | ~1k keys/s maximum |
| DB writes every 50 keys | `CrackingClass.run()` | Heavy I/O in the hot path |
| Global mutable singletons | `CryptoClass.keysD`, `CryptoClass.cl` | Untestable, race-prone |
| Hand-rolled listener bus | `KeysData` | Alert state can be cleared before observers read it |
| String-concatenated SQL | `SqliteClass.CheckIsDataAlreadyInDBorNot()` | Injection-shaped queries |
| Plaintext private keys at rest | `search.last_a`, `adrese.start/end` | Sensitive data exposure |
| Backup enabled | `AndroidManifest.xml` | Keys can leave the device via cloud backup |
| Legacy UI stack | `FragmentPagerAdapter`, AppCompat 1.3, Holo theme | Deprecated APIs |
| Test ad IDs in release | `BitcoinApplication`, `fragment_one.xml` | Ads not production-ready |

### Bundled database

`app/src/main/assets/blockchain.sqlite`:

- Table `bitcoin(_id PRIMARY KEY, value)` — `_id` is a Base58 P2PKH address, `value` is the
  balance in satoshis.
- **92,077 rows**, all mainnet `1...` addresses.
- Legacy tables `search` (single-row search state) and `adrese` (scanned ranges) are not part of the
  new data model.
- No Room schema metadata; the asset must be regenerated rather than loaded via `createFromAsset`.

### Build state

- Gradle 9.3.1, AGP 9.1.1, Java 17.
- `gradle.properties` contains a machine-specific `org.gradle.java.home`; `.tools/` and
  `.gradle-local/` are untracked and must be ignored in the new project.

---

## 3. Decisions

| Topic | Decision |
|---|---|
| Delivery | Fresh Kotlin/Compose project, same `applicationId` + upload key, upgrade path preserved |
| Platform | Android-only |
| SDK | `minSdk 24`, `compileSdk 36`, `targetSdk 36` |
| Version | `versionCode 11`, `versionName 1.0.0` |
| Screens | Stats and Solver only; no riddles, no settings |
| Privacy entry point | Single top-bar "Privacy options" action (UMP requirement), not a settings screen |
| Search runtime | Foreground `LifecycleService` with progress notification and Stop action |
| FGS type | `specialUse` (not `dataSync`), because `dataSync` is limited to 6 hours per 24 hours on Android 15+ |
| Crypto | ACINQ `secp256k1-kmp-jni-android` + BouncyCastle |
| Address types | Compressed and uncompressed P2PKH only |
| Data | Regenerated prepacked DB + Bloom filter; Room for app state; DataStore for flags |
| Ads | AdMob + UMP consent; test IDs in debug builds only |
| UI | Material 3, dark theme with cyan `#B3FFFF` identity |
| Puzzle special case | Removed; all DB hits use one uniform "Address guessed!" flow |
| Test fixtures | 3 known key pairs (6 addresses) kept as test-only fixtures; not in the shipped still-dormant asset |
| Instrumented tests | Run locally, not in CI |

---

## 4. Target stack

| Layer | Choice |
|---|---|
| Language | Kotlin 2.x, Kotlin DSL, `libs.versions.toml` version catalog |
| UI | Jetpack Compose, Material 3, Navigation Compose, `WindowSizeClass` |
| Architecture | MVVM + unidirectional data flow (`ViewModel` + `StateFlow`) |
| Async | Coroutines + Flow; crypto on `Dispatchers.Default`, DB on `Dispatchers.IO` |
| DI | Hilt + KSP |
| Persistence | Room, DataStore, Tink + Android Keystore for secrets |
| Search | `LifecycleService` foreground service, checkpoint/resume |
| Crypto | `fr.acinq.secp256k1:secp256k1-kmp-jni-android`, `org.bouncycastle:bcprov-jdk18on` |
| Address lookup | Build-time Bloom filter asset, batched SQLite verification on hit |
| Ads | Google Mobile Ads SDK + User Messaging Platform |
| Build | AGP 9.x, Gradle 9.3.1, JDK 21 toolchain, R8, baseline profiles |
| Static analysis | Detekt, Ktlint, Android Lint |
| Testing | JUnit5, Truth, MockK, Turbine, Robolectric, Compose UI Test, macrobenchmark |
| Coverage | Kover |
| CI | Bitbucket Pipelines: unit tests, lint, static analysis, release build only |

---

## 5. Architecture

### Modules

```
:app                 Compose UI, navigation, SearchService, Hilt, ads
:core:crypto         secp256k1, Base58Check, WIF, P2PKH, test fixtures (no Android deps)
:core:data           Room, DataStore, prepacked DB, Bloom filter, legacy migration
```

### Package layout (`:app`)

```
com.quickben22.bitcoinlotto
├── BitcoinLottoApp.kt
├── MainActivity.kt
├── core/
│   ├── clock/
│   ├── dispatchers/
│   └── ui/theme/
├── domain/
│   ├── model/          PrivateKey, KeyCheckResult, SearchResult, SearchSession
│   └── usecase/        CheckPrivateKey, StartSearch, StopSearch, GenerateWallet
├── data/
│   ├── repository/
│   └── migration/
├── search/
│   ├── SearchEngine.kt
│   └── SearchService.kt
├── feature/
│   ├── stats/
│   └── solver/
└── ads/
```

### Testability requirements

- No static singletons or global mutable state.
- Inject `SecureRandom`, `Clock`, `DispatcherProvider`, `AddressChecker`, and
  `KnownAddressRepository`.
- The search engine is pure Kotlin and deterministic so it can be unit tested on the JVM.
- The service exposes state through a repository `StateFlow`; ViewModels never talk to Android
  services directly.

---

## 6. Data model and database changes

### New prepacked database

Build-time generator (`tools/prepare-address-db`) will:

1. Read the per-address audit in `docs/address-audit.csv`.
2. Keep only still-dormant rows (`status = still_funded` and `current_sats >= snapshot_sats`); validate every kept address (`^1[1-9A-HJ-NP-Za-km-z]{25,34}$`), reject duplicates, report excluded rows.
3. Do not append fixtures: the six fixture addresses are `zero_at_snapshot` and are excluded with the rest of the non-dormant cohort.
4. Emit a Room-compatible `addresses.db` asset.
5. Emit `bloom.bin` sized for the final row count (target false-positive rate 1%).
6. Fail the build if the final count is not **82,086** or the kept set differs from the audit.

The lookup must never filter on `value`. Fixtures remain in the codebase for fake-repository tests only.

### Room entities

```kotlin
@Entity(tableName = "bitcoin")
data class KnownAddress(
    @PrimaryKey @ColumnInfo(name = "_id") val address: String,
    val value: Long
)

@Entity(tableName = "search_session")
data class SearchSession(
    @PrimaryKey val id: Int = 1,
    val combinations: Long,
    val lastKey: String,
    val elapsedMillis: Long
)
```

### Migration from the legacy app

- Read `search.last_a` and `search.combinations` into `SearchSession`.
- Discard all `solution*` columns and the `adrese` ranges.
- Delete the old database after a successful migration.
- Migration is idempotent.

### Fixture keys (test-only, not in the shipped asset)

The shipped asset carries only still-dormant addresses, so the six fixture addresses
(`zero_at_snapshot`) are excluded. They are used with fake repositories in tests.

Generated with an independent reference implementation (Node/OpenSSL secp256k1), not with
`CryptoClass`, and re-verified before merging.

| # | Private key | Compressed public key | Uncompressed public key |
|---|---|---|---|
| 1 | `d9eb4e2909d646dd931bb306a2b492475cff04097a2456416ec9b7dba5ac98b8` | `02a5fe7ad8999438b82de225d877c0979e1a75bc3553b89adcaad75c2f12bad213` | `04a5fe7ad8999438b82de225d877c0979e1a75bc3553b89adcaad75c2f12bad2138b6d8fe02e75b62a651b0ff8386d05637dd2fa09c2ef32ffad4e39df1e0e24d0` |
| 2 | `b7777a6bd712f26654e64bc6b609f311c252360f045f98b661704eb4c57b3772` | `031aa26e10eb28a01ff29846e1b5466e293c921bc42679f89bbf407ae1f9bd10b4` | `041aa26e10eb28a01ff29846e1b5466e293c921bc42679f89bbf407ae1f9bd10b4f0f4331cdc22b55ec43b7b3b2fd97182f001fb6ebdbeadd74097b2fb33ae5319` |
| 3 | `420e1776b2289ca21ce009195ebf67c514ce6259fc4be6036b6940cc545a7762` | `02ed7383f1a0cac4886da7b6cb21bf823010cd5c3cdcc3041ab1879e394e51d8c5` | `04ed7383f1a0cac4886da7b6cb21bf823010cd5c3cdcc3041ab1879e394e51d8c547b389376fc972e4664a7addaa87169ba1836251e4f1eeb07fbe171fd0b91e80` |

| # | Compressed address | Uncompressed address | WIF (uncompressed) | WIF (compressed) |
|---|---|---|---|---|
| 1 | `185iZWp5JuBnK2tUWMB39WagHuPmJvRBRW` | `19MoUVhGQ7kKCoWk8UuYAqqqnCYdw96ASi` | `5KUG2vf5iPiKGJmRymYEyirfBZS5CuA4U1fFnMhx93HTTLkzTtZ` | `L4XKMVq5ohFMcZR7FZathMkBXc9fZXCzJDurFCPkNV5dmBfHK8z6` |
| 2 | `1g4yqipc6uTfrdn14AfLhxdUePexjgvnq` | `1DotngYyJFzqdZ43DS1ZLFHB59sLYRDNVM` | `5KD5zWmCci7vt4xpjXLRqnQttZe67XDDokLJFNrcGgtZk7HmVJm` | `L3NM2rN6GcVapQVPggtkeKSU2b4hZnLwXLGB8TTxgF4bX5P38Heh` |
| 3 | `1Q5o8PCPxKEwPPV2MzWWjSRgQYgnohnsmF` | `14v2rFnEYpjz4gpUTR7dMifMfpgEZ5Vg6s` | `5JKNsMYRLsnMnaikiFtS9Gi7dDo4CTpBbyFxZnuprxmHVGQP2P5` | `KyS7XFfgSj9CkBqHw6i1yWLUEsUH7bMFoMK2FjeAFX8TmfXM1Nxp` |

All six addresses were verified absent from the current 92,077-row database.

---

## 7. Behavior specification

### Stats

- Shows keys checked, duration, and key frequency.
- START/STOP toggles the foreground search.
- Banner ad at the bottom (debug: test ad unit).
- On a hit: stop the search and show **"Address guessed!"** with the matched address.
- Top-bar overflow contains a single "Privacy options" action.

### Solver

- 64-hex private-key input with length and character validation.
- "Set Private Key" validates the key, derives compressed and uncompressed addresses, and checks
  the database immediately.
- If a match is found: show **"Address guessed! This private key controls `<address>`"** exactly
  once, even if both address variants are present.
- If no match: keep the normal flow.
- "Random" uses `SecureRandom`.
- "Generate Public Keys" shows both addresses; "Wallet Key" shows both WIFs.
- Copy actions mark the clipboard as sensitive.
- Info sheet explains how to import a WIF into a wallet.

### Search

- Sequential 32-byte increment with carry across all bytes.
- The entered/start key is checked first, before any increment.
- Batched derivation and lookup; no allocation in the hot loop.
- UI state updates are conflated to approximately 4 Hz.
- Checkpoints persist combinations, last key, and elapsed time.
- Stop reasons: user stop, limit reached, address found, invalid key.
- A hit always carries the exact private key and matched address.

---

## 8. Security and privacy

- Private keys and search progress are encrypted with Tink using an Android Keystore keyset.
- `FLAG_SECURE` on screens that display private keys.
- Clipboard entries use `ClipDescription.EXTRA_IS_SENSITIVE`.
- No private keys in logs, analytics, or crash reports.
- `allowBackup=false` (or backup rules excluding sensitive files).
- Remove hardcoded test device IDs from release; configure them only in debug.
- UMP consent form on first launch plus a reachable privacy options entry point.
- `android:allowBackup="false"` plus `dataExtractionRules` excluding the database and DataStore,
  so keys cannot leave the device through cloud backup or device-to-device transfer.
- Service is not exported; only `MainActivity` is exported.

### 8.1 Android permissions

| Permission | Type | Why | Required |
|---|---|---|---|
| `android.permission.FOREGROUND_SERVICE` | Normal | Run the search as a foreground service | Yes |
| `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | Normal | Permission for the `specialUse` service type (API 34+) | Yes |
| `android.permission.POST_NOTIFICATIONS` | Dangerous (API 33+) | Show the progress/stop notification | Yes, requested at runtime when the user starts a search |
| `android.permission.WAKE_LOCK` | Normal | Keep the CPU running while the screen is off during a user-started search | Yes, with a scoped partial wake lock released on stop |
| `android.permission.INTERNET` | Normal | AdMob ad requests | Yes |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | AdMob network checks | Yes |
| `android.permission.VIBRATE` | Normal | Optional notification vibration on a hit | Optional |
| `com.google.android.gms.permission.AD_ID` | Normal | Advertising ID; declared automatically by Mobile Ads SDK 20.4+ | Merged by SDK; declare data-safety usage |
| `android.permission.WRITE_EXTERNAL_STORAGE` | Dangerous | Legacy only | **Remove** |

No location, camera, microphone, contacts, storage, `QUERY_ALL_PACKAGES`,
`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `RECEIVE_BOOT_COMPLETED`, or exact-alarm permissions.

### 8.2 Manifest and Play Console declarations

- Service declaration:

```xml
<service
    android:name=".search.SearchService"
    android:exported="false"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="User-initiated, long-running local cryptographic key-space scan that must continue while the app is in the background." />
</service>
```

- Start the service only from a visible Activity (Android 12+ background-start restriction).
- Implement `Service.onTimeout(int, int)` defensively even with `specialUse`.
- Play Console: declare the foreground-service use case under **App content**; expect review.
- Play Console: complete the **Data safety** form for the Advertising ID and ad-related data.
- Target API 36 is mandatory for updates after 2026-08-31.
- If `POST_NOTIFICATIONS` is denied, still start the service but show an in-app explanation that
  progress is visible only in the system Task Manager.

---

## 9. Testing strategy

### Layers

| Layer | Tools | Runs |
|---|---|---|
| Unit (JVM) | JUnit5, Truth, MockK, Turbine, coroutines-test, Kotest property tests | CI |
| Android integration | Robolectric, Room in-memory | CI |
| Compose UI | Compose UI Test, AccessibilityChecks, screenshot tests | Local |
| Instrumented | ServiceTestRule, ActivityScenario, BenchmarkRule | Local |
| Performance | Macrobenchmark (startup), device benchmark (keys/s) | Local, physical device |

### Crypto tests (`:core:crypto`)

- Known-answer vectors, including private key `1`:
  - compressed address `1BgGZ9tcN4rm9KBzDn7KprQz87SZ26SAMH`
  - uncompressed address `1EHNa6Q4Jz2uvNExL497mE43ikXhwF6kZm`
  - WIF `5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreAnchuDf`
  - compressed WIF `KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qYjgd9M7rFU73sVHnoWn`
- All three fixture keys match their hardcoded public keys, addresses, and WIFs.
- Invalid keys rejected: zero, curve order `n`, values above `n`, 31/33-byte input, non-hex.
- Base58Check leading-zero handling and checksum rejection.
- WIF round trips preserve the compression marker.
- Property test: 1,000 random keys round-trip without loss.

### Guessed-key flow tests

`CheckPrivateKeyUseCaseTest` (pure JVM, fake repository seeded with the six fixture addresses):

| Case | Expected |
|---|---|
| each fixture private key | `Guessed(key, exact address, variant)` |
| compressed-only match | detected |
| uncompressed-only match | detected |
| both variants present | one result, no duplicate |
| unknown valid key | `NotGuessed` |
| invalid key | `InvalidKey`, no repository call |
| spaces/uppercase in input | normalized before lookup |

`SolverViewModelTest`:

- Entering a fixture key and invoking `onSetPrivateKey()` emits exactly one `guessedAddress`.
- Unknown key emits none.
- Invalid input emits an error and performs no lookup.

`SolverScreenTest` (Compose):

- Dialog with "Address guessed!" and the matched address appears exactly once.
- Dismiss works.
- No dialog for unknown keys.

`SearchEngineTest`:

- Starting at a fixture key already in the DB reports a hit with zero increments.
- Starting three keys before a fixture reports it after exactly three increments.
- Both variants present produce one hit.
- Absent address produces no hit.
- Limit reached before the hit reports the limit, not a hit.
- Checkpoint/restart resumes at the identical key.
- Bloom false positive is verified against SQLite and is not reported as a hit.

`AddressAssetContractTest` (Robolectric):

- Opens the shipped `addresses.db` and asserts a still-dormant address is present.
- Asserts the row count is 82,086 and that a ghost, an emptied address, and a fixture address are absent.

### Data tests (`:core:data`)

- DAO batch lookup: exact hit, miss, empty input, duplicates, 1,000 addresses.
- Asset validation: P2PKH format, no duplicates, expected count.
- Bloom filter: zero false negatives for every DB address; measured false-positive rate within
  budget on 100k non-members.
- Legacy migration: fixture legacy DB migrates combinations/last key, discards solutions and
  ranges, deletes the old DB, and is idempotent.
- Tink encryption round trip; corrupted ciphertext fails safely.
- DataStore persistence across restart.

### Search and service tests

- Increment rollover `0x...FF` to `0x...0100` across all 32 bytes; no skipped or duplicated keys.
- Stop, limit, found, and invalid-key paths.
- Duration and speed math with a fake clock.
- `startForeground` within timeout; notification channel and Stop action; throttled updates.
- Process-death resume verified locally.

### UI tests

- Stats initial state, START/STOP label, counter updates, hit dialog.
- Solver validation, random, address/WIF generation, sensitive copy, guessed dialog.
- Navigation preserves state.
- Accessibility: content descriptions, 48dp targets, contrast, 200% font scale.
- Screenshot regression for dark/cyan theme in portrait and landscape.

### Performance

- Cold-start budget via macrobenchmark.
- Keys-per-second benchmark with a regression threshold.
- StrictMode in debug to catch main-thread I/O and crypto.

### Coverage gates

| Module | Minimum |
|---|---|
| `:core:crypto` | 90% |
| `:core:data` | 80% |
| Engine + ViewModels | 80% |
| Overall | 60% |
| Found/dedup paths | 100% |

---

## 10. Milestones

| # | Work | Acceptance |
|---|---|---|
| M0 | Tag current APK, record parity checklist, obtain upload keystore | Current behavior documented |
| M1 | New Gradle project, version catalog, JDK 21 toolchain, Hilt/KSP, Detekt/Ktlint, CI, gitignore `.gradle-local/` and `.tools/` | `assembleDebug` and CI green |
| M2 | `:core:crypto` with real secp256k1, Base58Check, WIF, fixtures, vectors | All crypto tests pass |
| M3 | DB generator, Room entities, Bloom filter, legacy migration, asset contract test | 82,086 rows, still-dormant set matches the audit, migration idempotent |
| M4 | `CheckPrivateKey` use case, `SearchEngine`, `SearchService`, notification-permission flow, remove puzzle branch | Fixture key reported immediately; search survives minimize/process death; FGS starts only from visible UI |
| M5 | Compose Stats and Solver, guessed dialog, theme, privacy action | UI parity, accessibility, portrait/landscape/tablet |
| M6 | UMP + AdMob, FLAG_SECURE, sensitive clipboard, encrypted session, backup disabled | No test ad IDs in release |
| M7 | R8, full test run, signed AAB, Play data-safety form | Release candidate |

Estimated effort: **11–14 dev days**.

---

## 11. CI and local verification

### CI (Bitbucket Pipelines, every push/PR)

```powershell
./gradlew detekt ktlintCheck lintDebug testDebugUnitTest koverVerify
./gradlew assembleDebug assembleRelease
```

### Local device gate (before release)

```powershell
# API 24 emulator (minSdk floor)
./gradlew connectedDebugAndroidTest

# Current API emulator/device (API 36)
./gradlew connectedDebugAndroidTest

# Macrobenchmark and throughput (physical device only)
./gradlew :benchmark:connectedCheck
```

Manual release checklist:

- 30-minute foreground-service run with progress and Stop action.
- Battery and thermal observation.
- Confirm the guessed-address dialog with a debug-injected repository; the fixture keys are not in the shipped still-dormant asset.
- Confirm consent flow and privacy options entry point.
- Verify no private keys appear in logcat or crash reports.

---

## 12. Definition of done

- Entering any fixture private key in Solver, with a debug-injected fixture repository, immediately shows the guessed-address dialog.
- Starting a search at a fixture key reports it without scanning.
- Starting three keys before a fixture finds it after exactly three increments.
- Bloom filter has zero false negatives; false positives never produce a hit.
- Search resumes identically after process death.
- All crypto vectors pass and the old dummy derivation cannot pass them.
- CI is green and the signed AAB passes the local device matrix.

---

## 13. Risks and blockers

| Risk | Mitigation |
|---|---|
| Upload keystore missing from the repo | Required before publishing an upgrade; obtain from the Play Console owner |
| Legacy DB has no Room metadata | Regenerate the asset at build time; never load it directly with `createFromAsset` |
| Foreground-service Play policy | Use `specialUse` with a clear manifest justification, user-initiated start, visible notification; declare it in Play Console |
| `dataSync` 6-hour timeout (Android 15+) | Avoid `dataSync`; use `specialUse` and implement `onTimeout()` defensively |
| `POST_NOTIFICATIONS` denied | Keep the service running; explain the Task Manager fallback in the UI |
| Battery and thermal limits during long searches | Measure on device, throttle UI updates, expose speed, allow pause/stop |
| Fixture rows in the production DB | Removed: the asset ships only still-dormant addresses; fixtures are test-only |
| `minSdk` 21 to 24 bump | Small legacy user loss; required for modern libraries |
| Instrumented tests not in CI | Enforced by the local release checklist and pre-release sign-off |

---

## 14. Immediate next steps

1. Create the new project scaffold and version catalog (M1).
2. Implement `:core:crypto` and the fixture/vector tests first (M2).
3. Build the address database generator and asset contract test (M3).
4. Implement the immediate guessed-key check, engine, and service (M4).
5. Build the Stats and Solver Compose screens (M5).
