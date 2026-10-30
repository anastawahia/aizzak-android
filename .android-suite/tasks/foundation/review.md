# FOUNDATION-01 — independent local implementation review

Date: 2026-10-30 (Asia/Baghdad)
Reviewer: native android-code-reviewer, independently dispatched; did not implement or modify reviewed source.
Status: **passed for the inspected local slice**, with no remaining actionable findings identified. This is code review, not build, device, accessibility or integration certification.

## Scope and baseline

User approved the reviewed design, confirmed `com.aizzak.android`, and explicitly directed continuation of foundation/local parts while Firebase/API inputs are unavailable. Reviewed actual new app sources, JVM/instrumentation test sources and Gradle/wrapper/security defaults against V1-AGREED-SCOPE.md, TECHNICAL-DESIGN.md, UI-DESIGN.md and the active foundation record. Existing documentation/installed resources were preserved.

Reviewed source fingerprint (suite.source_fingerprint): `3ce87ead9e99e6aae7c9d335e96a53847efe69e05ac7f925d1bc1ce30b33a9b8`.

## Findings and resolution

- **P2 resolved — match system-bar contrast to the chosen application theme.** Initial MainActivity called default `enableEdgeToEdge()` once while Compose could force a different theme. On light-system/Dark-app or dark-system/Light-app, bar icon contrast could disagree with the app background. MainActivity now resolves the effective saved/system theme and reapplies `SystemBarStyle.auto(transparent, transparent) { dark }` for status and navigation bars in `SideEffect`. Source inspection confirms both appearance sources now agree and the API26+ AndroidX path receives the effective dark flag. Runtime appearance remains untested without a device.
- **Scoped compiler correction accepted:** SettingsScreen preference-row `selectable` now uses named `selected`, `enabled`, `role` and `onClick` arguments to resolve the intended overload. Reviewer read the current file and compared every reviewed artifact hash; this was the only changed reviewed source/config file. Radio selection semantics, disabled-state behavior and callback remain as intended. No implementation edits or Gradle execution by reviewer.
- **Scoped JVM fixture correction accepted:** DataStorePreferencesRepositoryTest now builds its real file-backed DataStore through public `OkioStorage(FileSystem.SYSTEM, PreferencesSerializer)` and the same public preferences factory. Reviewed the preserved first failure XML: the second update failed renaming a temp file over an existing Windows file. The correction retains all ten cases and their assertions, both real writes, cancellation/join of the first store, full reopen, official serializer, unknown-key non-rewrite checks and IO-error propagation. Hash comparison confirmed only this reviewed test file changed; production adapter/dependencies were unchanged. This is meaningful repository/serialization persistence coverage with a host-compatible storage fixture, not proof of the Android default `preferencesDataStore` filesystem path. The latter remains explicitly pending device QA; the new suite result must be taken from build-owner execution evidence. No test was disabled or assertion weakened.
- No other actionable P0–P3 findings identified within this local scope.

## Inspected behavior

- DataStore stores stable theme keys and locale-initialization metadata only, propagates failures, and does not rewrite unknown future theme keys. AppCompat/platform remains authoritative for language.
- Settings ViewModel serializes writes, preserves confirmed preferences on save failure, retains separate read/write failures, supports retries and propagates coroutine cancellation.
- Startup awaits preferences and locale-initialization persistence; English is set only for an uninitialized install without an explicit AppCompat locale. Subsequent system/app locale choices are not reset by that branch.
- Production navigation registers only SignIn, SignUp, Reset and Preferences. Home/Account are stateless preview/test content, not authenticated bypass destinations. Auth submissions are disabled with localized unavailability notice; no fake identity, token, context or permissions is supplied.
- Password inputs use non-saveable `remember`; no manual token/password storage or logs. Manifest disables backup/cleartext and exposes only launcher Activity plus nonexported disabled AppCompat metadata service. No unnecessary permissions.
- Approved logo SHA-256 matches `119462BB78EB240A65C869FC067EE599639B3CB5A41953F25C07B17D2A8C7E0F`. Palette values align with approved core light/dark tokens; resources include English and Arabic; inputs specify LTR text, forms scroll, selection rows carry radio semantics, agent cards have no click callback.
- 10 authored JVM cases exercise DataStore persistence/defaults/errors and ViewModel retry/serialization/cancellation; 3 authored Compose cases exercise auth gating/preferences navigation and callbacks/nonclickable agents. Authored test source is not execution evidence.
- Wrapper distribution checksum is pinned; debug ID is `com.aizzak.android.debug`, release ID is `com.aizzak.android`; release signing requires external provider inputs.

## Commands and limits

Reviewer used read-only Get-Content/Get-FileHash/rg/git diff --check and Python source hashing. `git diff --check` returned 0 (line-ending warning only). Reporting file is the only reviewer write. No Gradle, device tests, service calls, screenshot or TalkBack checks were run by this reviewer. Build engineer owns execution evidence and any compiler fixes require scoped re-review.

Missing Firebase/API prevents real auth/account acceptance by agreed scope, not a defect in this local slice. No connected device/AVD is available per build-engineer inventory; actual locale persistence/recreation, bar contrast, keyboard, resizing, font scaling and accessibility/device behavior remain outstanding. The current Compose tests do not invoke MainActivity's real locale persistence path; do not claim that coverage from callback assertions.

## Final evidence reconciliation

After build/test completion, rehashed every source/config item in the reviewed artifact table: all were unchanged. Native and external suite implementations both independently returned current fingerprint `3ce87ead9e99e6aae7c9d335e96a53847efe69e05ac7f925d1bc1ce30b33a9b8`, matching the actual final all/ui-compile reports. The earlier review fingerprint is superseded. Its precise discrepancy cannot be reconstructed without a then-current full input manifest; no reviewed-source change was found. `__pycache__` is excluded by suite hashing. `.kotlin` is not excluded and may hold transient compiler session files during a run (it was empty at this final check), so build timing is a possible, unproven explanation. Do not claim a proven hidden source change or a proven bytecode cause.

A full current fingerprint input manifest is retained as [review-inputs.json](review-inputs.json), SHA-256 `d48bd9f9a2bf7ea80e80d595c41a931aef02427d198b6b2c532b32e14067f3d5`, to support exact later reconciliation.

Inspected actual `.android-suite/evidence/foundation-all.json` and `foundation-ui-compile.json`: both status passed, final command exit 0, current fingerprint matches. The all report contains 10 JVM tests, zero failures/errors/skips; UI instrumentation APK compilation passed. These are build-engineer execution results inspected by the reviewer, not reviewer-run commands. No device UI execution is established.

Inspected actual lint text: 0 errors, 20 warnings. Sixteen dependency/AGP update notices do not invalidate the deliberately pinned compatibility baseline or establish a vulnerability. `localeConfig` API33 warning is expected alongside the AppCompat fallback for API26–32. `dataExtractionRules` suggestion is a future integration/privacy hardening item: current persistence contains only non-sensitive theme/initialization metadata, backup is disabled, and no account data is present; revisit device-transfer exclusions before storing sensitive account data. Unused generated `ic_app.xml` is minor scaffold cleanup, not a behavior failure. Square launcher-logo shape is a design-quality warning for the approved byte-identical bitmap; launcher/adaptive packaging should be revisited for release without altering the approved source logo. These residual warnings do not block acceptance of this local slice; no warning was suppressed and no failed automated check was relabeled as passing.

## Reviewed artifact hashes

| Path | SHA-256 |
|---|---|
| `.gitignore` | `511ae577b131072937fd6a64ec27da0eef9104613067bcd424fc25bd6b0f831c` |
| `app/build.gradle.kts` | `4edd44ba02c7d909d36398b94e827000c85f3bd8f72788388ce544e532c07efb` |
| `app/proguard-rules.pro` | `ace14dc77b12cd8e81c563f2c312315eab615055efe5c6934af42f5af08184fe` |
| `app/src/androidTest/java/com/aizzak/android/LocalFoundationUiTest.kt` | `3c3f9c9054777bc6cd736f8d72a2166f21bc370527a1399478f3616ffc5f1571` |
| `app/src/main/AndroidManifest.xml` | `176c1cbed3210dcd3a96bb43ee4b0f75a8fbf273e7f704bcaa98102df80a7037` |
| `app/src/main/java/com/aizzak/android/core/designsystem/AizzakTheme.kt` | `d4f501fa998e31e439e88701c4b270b577e950f9deadaf085a34484256926380` |
| `app/src/main/java/com/aizzak/android/core/designsystem/Components.kt` | `e0b6bc00b5ab44726e8df9b9c28ea215c8e0a24c5e8580ebcb65e7f63f07bc6b` |
| `app/src/main/java/com/aizzak/android/core/model/AppPreferences.kt` | `a9e722ea78cc1eef29576f89f61b873e19a3a005156e26b13d5b2f09cd1f5543` |
| `app/src/main/java/com/aizzak/android/data/preferences/DataStorePreferencesRepository.kt` | `2d6945b873323bedd177c0fe31a5c00fa35809c6149faffea2d14475e385429c` |
| `app/src/main/java/com/aizzak/android/data/preferences/PreferencesRepository.kt` | `9549fa67e5fc14021876cb2358f540aa974d6e6aaab2b6f3aa18d3d38fbf8568` |
| `app/src/main/java/com/aizzak/android/feature/account/AccountScreen.kt` | `452320f6ce0cd25a673170d88df60c7128e4fd63edd01f72a9e81c8ef7d5f7b4` |
| `app/src/main/java/com/aizzak/android/feature/auth/AuthScreen.kt` | `7c61a3002b27a79377de9c066ea7206f5c22a11c696ee4e902ada72054c153e3` |
| `app/src/main/java/com/aizzak/android/feature/home/HomeScreen.kt` | `7c5943c39215d7103e1fd186e1c1339a8f62b080d687f5e9f52e62bf3c7d0e33` |
| `app/src/main/java/com/aizzak/android/feature/settings/SettingsScreen.kt` | `d599eed8789f8d434fa7f5931cf51f9af9dd19658e17c5089d0bfce2daeba23d` |
| `app/src/main/java/com/aizzak/android/feature/settings/SettingsViewModel.kt` | `e340e6aa9a07be8081f65663e93249884ac484a183a7ecb9abb95be98320c78d` |
| `app/src/main/java/com/aizzak/android/MainActivity.kt` | `4ec1bec1de543f3b1afb441c050b142cfeb87766a813af4728deca2e6e3f4129` |
| `app/src/main/java/com/aizzak/android/navigation/LocalFoundationApp.kt` | `0ec03ada2f9766f7568a35e0e743b8b8343b25006709b1305a57cc2dd1c6c87d` |
| `app/src/main/res/drawable/ic_app.xml` | `a66c338418d6b9f0fbf6f7e172cdc67f9cc05c7f265d1b225ff8b5dd7a5398c7` |
| `app/src/main/res/drawable-nodpi/applogo.png` | `119462bb78eb240a65c869fc067ee599639b3cb5a41953f25c07b17d2a8c7e0f` |
| `app/src/main/res/values/strings.xml` | `106b1aa4e1c35cdfe80fc4c1140f018d31dc3cd0bd44a450361d32ec8025cf23` |
| `app/src/main/res/values/styles.xml` | `3407f706442c745897476a72883264736d0ea9030d31a64a48065d954708d693` |
| `app/src/main/res/values-ar/strings.xml` | `c2a70735cc9c15e0e90b1f8e4316c68628ef9a50ac3a8dace956c466e2b9ed4f` |
| `app/src/main/res/xml/locales_config.xml` | `299c994548f1040a90e3c5ea6e0a24287befd9facc50560cd6cc874fc32ba2d0` |
| `app/src/test/java/com/aizzak/android/data/preferences/DataStorePreferencesRepositoryTest.kt` | `cc871edafe23e2e4a65afca5afd4ef4ad84cd348e2d5daf624842c14811d0448` |
| `app/src/test/java/com/aizzak/android/feature/settings/SettingsViewModelTest.kt` | `fa17acf94629453a0ad4470f7b502bb92edb47dd0002e1021768ad2106001f3e` |
| `build.gradle.kts` | `a8d5c43966b4283945f3a1410b2ff3d0cbfa6ca74d1d4e92d2ceb31a95b2dc23` |
| `gradle/libs.versions.toml` | `81dfc4a575f6a4aa5f60c982fc8821d392577437ac48715fc891b3abc491b2fc` |
| `gradle/wrapper/gradle-wrapper.jar` | `81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f` |
| `gradle/wrapper/gradle-wrapper.properties` | `9efb8ec1dee8428f1ef68f54e54011b7aa4ebbee4336992908f3e4c21f268978` |
| `gradle.properties` | `678c35a27eeda302d6a960484179d118d08fb1edf05c1b40745bc7aed6e88f03` |
| `gradlew` | `c76f16038b4e70b84ed0b3b9e08e82a8ecaf66ec0efdfdecdece92520fa3cecb` |
| `gradlew.bat` | `0f3ed8f03b50934cb8c48b15a470d5c20a30a5385825e48b55bcc8ea3d8f8e18` |
| `settings.gradle.kts` | `a46a651cf3454be30b7313e3df5415ab4f8c35050b1de740e1988b2002c0a493` |
