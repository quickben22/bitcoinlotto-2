# App worklog — 20 November 2026

**Branch:** `master`
**Status:** implemented, tested, committed and pushed to `github/master`.
**Scope:** Solver/Stats behaviour, searched-range database, medium UX polish, then removal of the
Generate/Addresses button.

---

## 1. Requests and confirmed decisions

| Request | Decision |
|---|---|
| Solver “Set Private Key” must update Stats key | Yes, plus Random; Stats uses it as the next search start when no checkpoint exists |
| “Generate Public Keys” does nothing | Fix visible feedback, then **remove the button entirely** (final request) |
| Remove DURATION / KEY FREQUENCY cards | Cards removed; internal speed/duration kept for status + found dialog |
| New SQLite DB of searched keys | Columns exactly `id`, `from`, `to`; encrypted values; record **and** skip ranges |
| UX polish | Medium polish; **keep the background image** |
| Public key vs address question | App displays legacy P2PKH addresses, not raw public keys; explained, no raw pubkey cards added |

---

## 2. Functional changes

### Shared key handoff
- `SearchState.pendingStartKeyHex` added (`app/.../search/SearchState.kt`).
- `SolverViewModel.onSetPrivateKey()` / `onRandom()` call `publishKey()`:
  - writes `lastKeyHex` (Stats display) and `pendingStartKeyHex` into the shared `SearchStateHolder`;
  - sets `SolverUiState.message = R.string.solver_key_set`.
- `StatsViewModel.startOrStop()`:
  - checkpoint present → resume, pending key cleared;
  - no checkpoint → start at pending key if it is a valid secp256k1 key, otherwise secure random;
  - pending key consumed once.
- `AppViewModelFactory` injects `SearchStateHolder` into `SolverViewModel`.

### Solver addresses (button removed)
- `SolverScreen` now has:
  - input card with paste/clear icons, 64-char counter (green at 64), keyboard **Done** → Set key;
  - **Set key** full-width filled primary button;
  - **Random** + **Wallet** outlined buttons sharing the next row;
  - “ADDRESSES” results section with empty state and copyable compressed/uncompressed address cards.
- No Generate button and no `onGeneratePublicKeys` UI callback. The ViewModel method remains only for
  existing unit tests.
- Addresses are derived automatically on Set/Random (`SolverViewModel.check()`), so removing the button
  lost no functionality.
- Each successful derivation bumps `derivedVersion`; `SolverScreen` hides the keyboard and scrolls the
  result cards into view via `BringIntoViewRequester`.

### Stats cleanup
- Removed the two stat cards and the `duration` / `key_frequency` strings.
- KEYS is now a single hero card with “Checked this session” caption.
- Added status pill, reward progress bar toward 6M/60M unlock, total-searched line, copyable current
  key card, resume chip, icon Start/Stop button.

### Searched-ranges database
New in `core:data`:
- `SearchedKeyRangeStore.kt` — interface, `SearchedKeyRange`, `NoOpSearchedKeyRangeStore`.
- `SqliteSearchedKeyRangeStore.kt` — `searched_keys.sqlite`, table `searched_keys`:
  `id INTEGER PRIMARY KEY AUTOINCREMENT`, `"from" TEXT NOT NULL`, `"to" TEXT NOT NULL`.
  - `from`/`to` are Tink-encrypted Base64 (shared `CheckpointCipher` from `AppContainer`).
  - 64-char lowercase hex; half-open `[from, to)`; adjacent/overlapping ranges merged.
  - `endOfContainingRange()` returns the furthest end.
- `SearchCheckpointStore` constructor gained an optional `CheckpointCipher` parameter so the cipher is
  shared with the range store.
- `VisibleSearchController`:
  - new `searchedRanges` dependency (default `NoOp` for old tests);
  - records range on checkpoint save, limit reached, and found-key save;
  - fresh searches skip a containing range before decoding the start key; resumes never skip;
  - skipped keys do not inflate reward totals;
  - range-store errors are logged and never abort search.
- Data extraction rules already exclude databases from backup; `allowBackup=false` is unchanged.

### UX polish (background preserved)
- `Theme.kt`: hierarchy colors (cyan actions/values, pale body, muted labels, green running, amber
  locks) and custom typography weights/letter spacing.
- `AppBackground.kt`: vertical gradient scrim over the existing `background.jpg`.
- `Common.kt`: translucent `AppCard`, status chip, reward progress, empty state, improved value cards.
- `Dialogs.kt`: rounded dark surfaces, key on two monospace lines, copy-key and copy-address actions.
- `BitcoinLottoRoot.kt`: nav divider and more opaque bottom bar.
- New local vector drawables: play, stop, copy, paste, clear, check, key, wallet, info, random.

---

## 3. Tests and verification

```powershell
.\gradlew.bat :core:crypto:test :core:data:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug
```

All green at commit time. Notable tests:
- `SqliteSearchedKeyRangeStoreTest` — CRUD, merge, boundaries, encryption-at-rest, reopen persistence,
  corrupt rows.
- `VisibleSearchControllerTest` — range recorded on stop, skip on fresh start, resume ignores ranges,
  store failures non-fatal.
- `SolverViewModelTest` — Set/Random publish key; Generate/derive tests retained; wallet WIF flow.
- `SolverScreenTest` — taps **Set key** and asserts address cards appear (Robolectric Compose test).
- `StatsViewModelTest` — start uses pending key when no checkpoint.

`app/build.gradle.kts` gained `testImplementation(libs.androidx.compose.ui.test.junit4)` for
`SolverScreenTest`.

---

## 4. Notes / follow-ups

- No device or emulator was attached (`adb devices` empty), so manual on-device verification was not
  done. Automated tests cover the logic and Compose interaction.
- `createComposeRule()` in `SolverScreenTest` emits a deprecation warning; v2 API migration is
  optional.
- Raw public-key hex is **not** displayed (only addresses and WIFs). Addresses are legacy P2PKH
  (`1...`), not SegWit/Taproot.
- `SolverViewModel.onGeneratePublicKeys()` is now unused in production; safe to remove together with
  its three ViewModel tests if you want a cleaner API.
- Pre-existing untracked `analysis/` and `docs/` investigation files were deliberately **not** staged.
- Version bumped to `versionCode 17` / `versionName 1.1.0` after the release AAB was first built.
