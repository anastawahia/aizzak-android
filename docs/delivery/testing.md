# Testing policy

Current account-context live acceptance is now user-confirmed: «وصلت للرئيسية ونجحت إعادة الدخول» after final installation and the verified sign-in/logout/re-login request. This is human runtime evidence, separate from the passing 55 JVM/15 isolated device cases. No credentials were submitted by the coordinator. See [acceptance](tasks/account-context/local-logout-acceptance.md); pending live-user notes below are historical.

Latest Android-only account-context continuation: `account-context-local-logout-all.json` passes build/55 JVM cases/Lint (0 errors/27 warnings); `account-context-local-logout-ui.json` passes 15 device cases with the retention options below. Fingerprint `d35135fd4664397e22a247b2fc44d58c40d2a03f34e8a1f8015945fcd6aec06f`, independent local-logout review and implementation/isolated integration gates passed. Current runtime is the backend owner's master, not the previously tested/unmerged branch. Live user account acceptance remains separate from isolated checks. Details: [verification](tasks/account-context/verification.md).

Physical-device preservation: connected UI checks must keep compatible installed APKs and must not remove an incompatible installed package as automatic recovery. Use `:app:connectedDebugAndroidTest -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true -Pandroid.experimental.testOptions.uninstallIncompatibleApks=false --console=plain --no-daemon`, capture command/output/JUnit evidence, and verify the target remains installed. No clear-data commands/options. See current account-context verification for inspected AGP/UTP evidence. The plain suite UI command below does not pass these options; use the explicit wrapper command for a user's device.

Current ACCOUNT-CONTEXT-01 verification supersedes older inventory/pending statements below: build + 54 JVM cases + lint (0 errors/27 warnings) + 15 isolated device UI cases passed on Samsung/API35. Source fingerprint `470826ab677410a518606c0bf45ef936100103bb14c7bde77bf8f7d3cf03870a`; independent security/correctness and implementation/isolated integration gates passed. UI fixtures now supply English resources locally, independently reviewed, without changing the device language. Backend focused 232 cases passed. Exact attempts, hashes, failed-run diagnoses and final installation are recorded in [verification](tasks/account-context/verification.md). Real service acceptance remains blocked by Vault AppRole login; these fake/isolated cases do not prove it.

Local slice verification owner: build-release-engineer (Gradle execution); independent reviewer checks correctness and acceptance coverage. Avoid concurrent Gradle invocation while source owners are integrating.

Required local checks after source freeze:

- `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` using suite.py verify --kind all --execute and actual execution JSON under evidence/.
- `:app:assembleDebugAndroidTest` using --kind ui-compile; this compiles instrumentation only.
- Independent read-only source review; actionable findings returned to their implementation owner and reviewed again after correction.
- Machine implementation gate references current source fingerprint and exact report hashes. It does not replace device accessibility/behavior checks or real service integration acceptance.

Core authored tests: DataStore file recreation persistence, initial defaults, unknown future preference value, I/O read failure, ViewModel read/save retry, write ordering, independent read/write errors, dismissal/retry and lifecycle cancellation. See [local state handoff](tasks/foundation/local-state.md); authored cases do not imply execution.

Device matrix outstanding: API 26 and recent API, English/Arabic first run and explicit restored locale, RTL/LTR fields, System/Light/Dark after restart, 320/600/840dp width, landscape/IME, fonts 100/150/200%, TalkBack/focus. adb and AVD inventory currently empty; do not claim these were run. No Firebase/Auth Emulator or live API tests are possible with the current missing environment.

Build diagnostics while source changes are historical only; final reports must fingerprint the stable current source. See [active status](tasks/foundation/status.md) for executed outcomes.

## Executed local outcomes

Final [all execution](evidence/foundation-all.json) exit 0: 10 JVM tests passed with zero failures/errors/skips; debug build passed; Android Lint zero errors and 20 warnings (documented in active status). [UI test compilation](evidence/foundation-ui-compile.json) exit 0. Source fingerprint for both: `3ce87ead9e99e6aae7c9d335e96a53847efe69e05ac7f925d1bc1ce30b33a9b8`.

Real-file JVM tests use official cross-platform OkioStorage because the Android FileStorage SDK_INT stub falls into a Windows-incompatible legacy branch. Production Android DataStore default remains unchanged. The test still performs multiple durable writes and store closure/reopening. Details/source references and first failed result are preserved in [core handoff](tasks/foundation/local-state.md) and evidence/foundation-all-attempt1.json. This test is not execution of Android's default storage on a device.

[Device inventory](evidence/device-inventory.json) confirms no usable device. Three UI tests compiled only; actual platform locale persistence, process recreation, TalkBack and visual/device checks remain unexecuted. Real service integration tests remain blocked by missing configuration.
