# Code review: bitcoinlotto-2 (`app/` + `core/`)

## Scope
I reviewed all production Kotlin in `:app`, `:core:crypto`, and `:core:data`, plus the Gradle config, the manifest, and the tests.
The codebase is in good shape: it's modular, and it already includes FLAG_SECURE dialogs, sensitive clipboard copies, an encrypted checkpoint (Tink), a foreground service with `specialUse` type, and decent unit tests.
The findings below are ranked by impact.

---

## 🔴 High: correctness and data loss

### 1. A found key can be lost permanently
- `SearchService.handleEvent` on `Found` (`SearchService.kt:186-208`) stores the hit only in the in-memory `SearchStateHolder`. It then **clears the checkpoint** and stops the service.
- If the process is killed before the user opens the app, the winning key is gone.
- **Fix:**
  - Persist the hit (key, address, compressed flag) through the existing Tink-encrypted DataStore, for example `SearchHitStore`, before clearing the checkpoint.
  - Load the hit on startup and clear it only after the user dismisses the dialog.

### 2. The user is never notified about a hit while the app is backgrounded
- `finishSearch()` calls `stopForeground(STOP_FOREGROUND_REMOVE)` (`SearchService.kt:316-320`), which removes the only notification. So the "hit" title in `buildNotification` is never shown.
- **Fix:**
  - On `Found` and `LimitReached`, post a separate, non-ongoing, high-priority notification. Show the address only, never the key; tapping it opens the app.
  - Use `STOP_FOREGROUND_DETACH`, or post the notification with a different ID.

### 3. The address DB is copied on the main thread on first launch
- `AppContainer.checkPrivateKey` is built with `CheckPrivateKeyUseCase(addressRepository)` (`AppContainer.kt:26`). This forces `SqliteAddressRepository.open()`, which copies the **8 MB** asset.
- `AppViewModelFactory` (`AppViewModelFactory.kt:22`) triggers this on the main thread when `SolverViewModel` is created. That causes jank or an ANR on slow devices.
- **Fix:** make the use case take a `Lazy<AddressRepository>` or a provider. Alternatively, open the repository inside the IO `withContext`.

### 4. A stale result can overwrite the solver state
- `SolverViewModel.check()` (`SolverViewModel.kt:103-139`) launches a new coroutine on every tap and never cancels the previous one.
- If the user edits the input (`onInputChange` clears `derived`) and a slow check then finishes, the old key's addresses and "guessed" dialog reappear for the new input.
- **Fix:**
  - Keep a `checkJob` and cancel it in `check()` and `onInputChange()`.
  - Before applying a result, verify it still matches the current input.

### 5. Ads can load before consent and without the consent gate
- `BitcoinLottoApp.onCreate` calls `MobileAds.initialize()` before consent is gathered.
- `AdBanner` (`AdBanner.kt:46-48`) loads an ad without checking `AdsConsentManager.canRequestAds`, which is currently unused.
- For EEA/UK users this violates Google's UMP guidance and is a policy risk.
- **Fix:**
  - Initialize MobileAds and load the banner only once `canRequestAds` is true.
  - Expose consent state as a `StateFlow`.
  - Request consent in `onCreate` (once per process), not `onResume`. The `consentRequested` flag resets when the activity is recreated.

---

## 🟠 Medium: robustness and lifecycle

### 6. Stop races with in-flight events
- `stopSearch()` (`SearchService.kt:236-247`) cancels `searchJob` cooperatively, then immediately launches a second coroutine that persists the checkpoint.
- The first job may still be in `handleEvent` on `Dispatchers.Default`. That leads to:
  - concurrent `checkpointStore.save` and `clear` calls;
  - unsynchronized `session*` fields.
- **Fix:**
  - Use `job.cancelAndJoin()` before persisting.
  - Confine the service's state to a single dispatcher, for example `Dispatchers.Default.limitedParallelism(1)` for event handling. Alternatively, collect on Main while the engine runs `flowOn(Default)`.

### 7. Duplicated and redundant service paths
Simplify these into one `startSearch(startKeyHex?, limit?, resume)` path:
- `finishSearch()` and `stopSelfSafely()` are identical (`SearchService.kt:316-326`).
- `resumeFromCheckpoint()` duplicates `startSearch(resume=true)` (`SearchService.kt:89-123`).
- `stopSearch(userStopped)` is only ever called with `true`.
- `StatsViewModel.startOrStop` reads the checkpoint and passes `nextKeyHex` with `resume=true`. The service then re-reads the checkpoint anyway (`StatsViewModel.kt:38-44`).

### 8. The `onTimeout` path doesn't persist the checkpoint
- `onTimeout` (`SearchService.kt:425-433`) cancels the job without a forced checkpoint save, so up to 2 s of progress is lost.
- It also reports `Error` rather than a resumable `Stopped` state.
- **Fix:** reuse the stop path: force-persist, then set `Stopped` with a "time limit reached, tap to resume" message.

### 9. The release ad-ID guard is incomplete
- `app/build.gradle.kts:117-124` checks only `assembleRelease` and `bundleRelease`.
- `installRelease`, `build`, or Android Studio "Run" on the release variant produce an APK with an empty `APPLICATION_ID`. That crashes MobileAds at startup.
- **Fix:** validate in `androidComponents.onVariants { if (it.buildType == "release") ... }`, or use a `preBuild` dependency task for release.

### 10. Hardcoded, non-localized strings and formats
Move these to `strings.xml` and use locale-aware formatting:
- `PrivateKeyParser` and `SearchEngine` error messages are English literals that are shown directly in the UI.
- `formatCount` and `formatSpeed` force `Locale.US`.
- The notification shows `keysChecked` without grouping (`search_notification_progress`).
- The notification `setColor(0xFFB3FFFF)` (`SearchService.kt:392`) is a stray teal. It should come from a colour resource that matches the new indigo icon, for example `#FF736AFF`.

---

## 🟡 Performance: search engine (biggest user-visible win)

Each key currently costs:
- **2 full EC scalar multiplications** (compressed and uncompressed public keys are computed separately),
- **2 Base58Check encodings** using BigInteger division,
- **2 SQLite queries**,
- 4–5 redundant `isValidPrivateKey` BigInteger checks.

Proposed changes, each independently measurable:

### 11. Use incremental point addition
- Keys are sequential, so compute `P = k·G` once and then `P = P + G` per step. Point addition is roughly 100× cheaper than scalar multiplication.
- Derive both the compressed and uncompressed encodings from the same normalized point.

### 12. Compare hash160 values instead of addresses
- At load time, decode the 92,083 DB addresses into a `HashSet` of 20-byte hash160 values wrapped in a `ByteArrayKey`, which needs about 4 MB.
- Then check `hash160(pubkey)` directly. This removes Base58 and SQLite from the hot loop.
- Build a `BitcoinKey` only on a hit.

### 13. Remove redundant validation
- Validate once at the start.
- After incrementing, check only `key == n` by comparing against a precomputed `order` byte array, not with a BigInteger per iteration.

Add a JVM micro-benchmark test (keys/s before and after) to verify these changes.

---

## 🟢 Low: cleanup and hygiene

14. Delete the dead placeholder files:
    - `core/crypto/.../Placeholder.kt`
    - `core/data/.../Placeholder.kt`
15. Move `FixtureAddresses` from `core/data/src/main` into a test fixture (`src/testFixtures` or a shared test source). It currently ships in production.
16. `BitcoinCrypto` (`BitcoinKey.kt:41-60`) is only used by tests. Either remove it or make it the single public façade.
17. Remove unused `ByteArray.toHex()` and `String.hexToByteArray()` (`Hex.kt:30-32`). Remove the `increments` field from the `SearchEvent`s, because it is always `keysChecked - 1` and unused.
18. Key hygiene:
    - `BitcoinKey.toString()` and `WifKey.toString()` print the private key, and so does the data-class `KeyCheckResult.Guessed`. Any accidental log leaks it. Redact it in `toString`.
    - `BitcoinKey.privateKey` exposes a mutable array. Return a copy or keep it internal.
19. UI polish:
    - Replace the `"\u22EE"` `TextButton` overflow (`BitcoinLottoRoot.kt:75-80`) with an `IconButton` using a vector "more" icon and a `contentDescription`, for accessibility.
    - `BitcoinLottoRoot` creates a second `AdsConsentManager`. Share the one from `AppContainer`.
    - Drop the custom "Copied" toast on API 33+, where the system already shows clipboard UI.
20. Build config:
    - `gradle.properties` sets `android.builtInKotlin=false` and `android.newDsl=false`. These opt-outs are deprecated in AGP 9 and will be removed, so plan the migration: drop the `kotlin.android` plugin and use built-in Kotlin.
    - `compileSdk = 37` requires the API 37 platform, which isn't installed locally. Note this in the README, or align to the installed SDK.
21. Tests:
    - There are no tests for `SearchService` (start, stop, resume, found).
    - Add Robolectric `ServiceController` tests covering fixes 1, 2, 6, and 8.

---

## Proposed implementation order
1. Data-safety fixes first: hit persistence and the hit notification (#1, #2).
2. Main-thread DB copy and the solver race (#3, #4).
3. Consent gating (#5).
4. Service refactor (#6, #7, #8) with new service tests (#21).
5. Search-engine performance (#11–#13) with a benchmark.
6. Build guard and localization (#9, #10).
7. Cleanup (#14–#20).

## Notes
- I can't run a full Gradle build locally because the android-37 platform is missing. Validation will use JVM unit tests in `:core:crypto` and wherever else possible. Alternatively, install API 37 through the SDK Manager.
- Each group can be its own commit on the current branch, or a separate PR if preferred.
