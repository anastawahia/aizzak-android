# Compose build handoff — design-conformance

## Status and scope

- Stage: `build`
- Workflow: `ui-redesign`
- Status: **passed for Compose implementation and JVM screenshot recording**
- Screens: `SCR-HOME-01`, `SCR-ACCT-01`, `SCR-AUTH-01`, `SCR-AUTH-02`, `SCR-AUTH-03`, `SCR-AUTH-04`, `SCR-LOAD-01`, `SCR-CTX-01`, `SCR-SET-01`, `SCR-ACCT-02`
- Input source baseline supplied by the director: `263d1b66d746b3e3959f9255a7e77dffb19fafc2bb3cf354b4af4df61766440c`
- Evidence source hash reported by the final screenshot suite: `17ce8b283f4076968f785bebe9adbec0e775d20945b8367e7413d58f6c799238`
- No backend, ViewModel, repository, `AppContainer`, `MainActivity`, auth/data/network contract, approved design artifact, or lifecycle-record edits were made by this role.

## Approved inputs used

- `docs/design/UI-PREVIEW.html` — `d20dc95814303301c5dfb0146f8ec25ea717d4ecca64e28d5e4ac6e125f98d0c`
- `docs/design/UI-DESIGN.md` — `1f74b0a82357a560443045864f89cc27685a58af2a6b3999e0ec702f7b51e7ab`
- `docs/delivery/tasks/design-conformance/journeys-wireframes.md` — `741060e3dafc26a20342bec72e16e3e184dd210aa1abf2027cb68beedc09d0c9`
- `docs/delivery/tasks/design-conformance/design-tokens.md` — `1fdff12ca5cf83c6bfb7f6f3a2dd98dad0b9b16d71c43afa545dd234adf6b521`
- `docs/delivery/tasks/design-conformance/screen-inventory.json` — `89fef11c5447da7f501ccf463da98cc2e1f4023f00f42726ad788b4f30f8093a`

## Changed implementation paths

- `app/src/main/java/android/aizzak/com/core/designsystem/AizzakTheme.kt`
- `app/src/main/java/android/aizzak/com/core/designsystem/Components.kt`
- `app/src/main/java/android/aizzak/com/feature/home/HomeScreen.kt`
- `app/src/main/java/android/aizzak/com/feature/account/AccountScreen.kt`
- `app/src/main/java/android/aizzak/com/feature/account/PasswordScreen.kt`
- `app/src/main/java/android/aizzak/com/feature/auth/AuthScreen.kt`
- `app/src/main/java/android/aizzak/com/feature/auth/AuthSessionScreen.kt`
- `app/src/main/java/android/aizzak/com/feature/settings/SettingsScreen.kt`
- `app/src/main/java/android/aizzak/com/navigation/LocalFoundationApp.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-ar/strings.xml`
- `app/src/test/java/android/aizzak/com/DesignScreenshotTest.kt`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`

## Implemented coverage

- Fixed AIZZAK brand bar and fixed, non-dynamic light/dark palettes.
- Main authenticated shell keeps only Home, Account, and Settings destinations; bottom navigation below `600dp`, navigation rail from `600dp`.
- Settings supports main-destination and pushed/back variants.
- Correct RTL/LTR layout handling, including LTR identity/password values and mirrored start accents.
- Scrollable branded forms and adaptive card grids remain reachable at `1.5x` font scale.
- Interactive targets use at least `48dp` semantics/touch sizing; headings, merged descriptive-card semantics, roles, and test tags were added where applicable.
- Home agent cards are descriptive and non-clickable, with icon blocks, status dots, and start accents.
- Account, password, sign-in, sign-up, reset, email verification, loading, context-error, and settings states follow the approved inventory order and hierarchy.
- Sign-out remains a local callback only; no `/api/v1/me/logout` invocation was introduced.

## Screenshot coverage and evidence

- Roborazzi records exactly 40 deterministic PNGs: all 10 SCR IDs × 4 configurations:
  - `en / light / 390dp / 1.0x`
  - `en / dark / 840dp / 1.0x`
  - `ar / dark / 390dp / 1.0x`
  - `ar / light / 840dp / 1.5x`
- Working output: `app/build/outputs/roborazzi/`
- Archived evidence: `docs/delivery/evidence/screenshots/1793485748/`
- Manifest: `docs/delivery/evidence/screenshots/1793485748/manifest.json`
- Manifest SHA-256: `bc36f23dcd0cd3604799651a58a8de5988a6f86f8db51901ba3aca2a18a43aed`
- Representative dark, RTL, compact, expanded, bottom-navigation, and rail captures were inspected visually after the final recording.

## Verification commands

| Command | Exit | Result |
|---|---:|---|
| `.\gradlew.bat :app:compileDebugKotlin` | 0 | Final Kotlin compilation passed in 8s. |
| `.\gradlew.bat :app:recordRoborazziDebug` | 0 | Final recording passed in 55s; 40 PNGs recorded. |
| `python .codex/android-software-house/runtime/scripts/suite.py screenshots --project C:\Users\anast\Desktop\aizzak-android --execute` | 0 | `passed`; 56 unit/JVM tests, 0 failures, 0 errors, 0 skipped; manifest archived. |
| `.\gradlew.bat :app:lintDebug` | 0 | Passed in 1m31s; report at `app/build/reports/lint-results-debug.html`. |

The initial implementation required bounded targeted recovery for a Compose lambda compile error, parameterized-runner/Robolectric incompatibility, and final screenshot output/content-color correction. The final commands above are the post-recovery evidence. Detekt was not run because this project does not expose/configure a Detekt gate in the in-scope build setup.

## Deviations, findings, and limitations

- Proposed deviations from approved inventory: **none**.
- Open implementation blockers: **none**.
- Remaining independent gates: design-conformance review must be performed by a role that did not implement these screens.
- JVM/Robolectric screenshots do not replace physical-device checks for TalkBack traversal, IME behavior, or OEM font rendering; those device checks remain a QA follow-up.
- No signing, publishing, backend access, or lifecycle progression was performed.

## Next handoff

Hand the archived manifest and current source to the independent design-conformance reviewer, then to Android QA for device accessibility/navigation checks. The orchestrator owns any lifecycle update.

---

## Independent-review repair round 1

### Status

- Review findings addressed: **P1 androidTest journey drift**, **P2 deterministic exact screenshot set**, and **P2 duplicate decorative-symbol semantics**.
- Repair status: **passed for compilation, JVM tests, screenshots, and lint**.
- Final suite source SHA-256: `1d75cf2ed0bcb1991e1e14fd8f03247bc822a95e468914923a3a1a1792d83fcb`.
- Device execution status: **blocked** because no Android device/emulator is connected; androidTest compilation and APK assembly both pass.

### Repair files

- `app/src/androidTest/java/android/aizzak/com/LocalFoundationUiTest.kt`
- `app/src/androidTest/java/android/aizzak/com/AuthUiTest.kt`
- `app/src/androidTest/java/android/aizzak/com/AccountContextUiTest.kt`
- `app/src/test/java/android/aizzak/com/DesignScreenshotTest.kt`
- `app/src/main/java/android/aizzak/com/core/designsystem/Components.kt`
- `app/src/main/java/android/aizzak/com/feature/home/HomeScreen.kt`
- `app/src/main/java/android/aizzak/com/feature/auth/AuthScreen.kt`
- `app/src/main/java/android/aizzak/com/feature/auth/AuthSessionScreen.kt`
- `app/src/main/java/android/aizzak/com/feature/settings/SettingsScreen.kt`

### Finding resolution

- Signed-out Preferences is now tested from Sign in and returns to Sign in. Auth verification, loading, denied-context, and unavailable-context journeys assert the approved new titles through stable tags.
- Auth, resend, sign-out, Settings back, language, and theme actions expose stable test tags. Authenticated Settings is verified as a main destination without a back action.
- `DesignScreenshotTest` now uses `ParameterizedRobolectricTestRunner`: all 40 configurations are distinct JUnit test cases. Recording deletes only the 40 approved target names within `app/build/outputs/roborazzi`; it never removes other paths. The final parameter asserts the exact 40-name PNG set and rejects missing or extra PNGs.
- Decorative `i`, `!`, envelope, and agent symbols have cleared semantics so their notice/card content is announced once.

### Final verification

| Command | Exit | Result |
|---|---:|---|
| `.\gradlew.bat :app:compileDebugKotlin` | 0 | Passed in 13s. |
| `.\gradlew.bat :app:compileDebugAndroidTestKotlin` | 0 | Passed in 4s. |
| `.\gradlew.bat :app:assembleDebugAndroidTest` | 0 | Passed in 5s; androidTest APK assembled. |
| `.\gradlew.bat :app:testDebugUnitTest` | 0 | Passed in 35s after bounded recovery. |
| `.\gradlew.bat :app:recordRoborazziDebug` | 0 | Passed in 59s; exact 40 PNG set recorded. |
| `python .codex/android-software-house/runtime/scripts/suite.py screenshots --project C:\Users\anast\Desktop\aizzak-android --execute` | 0 | `passed`; 95 tests, 0 failures, 0 errors, 0 skipped. |
| `.\gradlew.bat :app:lintDebug` | 0 | Passed in 33s; `app/build/reports/lint-results-debug.html`. |
| `connectedDebugAndroidTest` | not run | Blocked: no connected device/emulator. |

Two targeted recovery iterations were used: the first moved the exact-set assertion out of Robolectric's per-parameter `@AfterClass`; the second gated destructive target cleanup and exact-set validation to `roborazzi.test.record=true`, keeping ordinary unit-test runs non-destructive.

### Round-1 evidence

- Screenshots: `docs/delivery/evidence/screenshots/1793486425/`
- Manifest: `docs/delivery/evidence/screenshots/1793486425/manifest.json`
- Manifest SHA-256: `84ef6b32fa4d2d0759174d9fb222be603f16fdb1798afd0070a410e8dac73970`
- Screenshot count: `40`
- Test result: `95 / 95 passed`, with `0` failures, errors, or skips.
- Proposed deviations: **none**.
- Backend, ViewModels, repositories, approved artifacts, and `lifecycle.json` remain untouched by this repair round.
