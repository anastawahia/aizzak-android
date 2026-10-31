# FIREBASE-PACKAGE-01

Date: 2026-10-31 (Asia/Baghdad).
Status: complete; implementation gate, independent review and Android Studio startup smoke verification passed.

User request: change the Android package from `com.aizzak.android` to match the supplied Firebase Android registration, `android.aizzak.com`.

## Scope and approval

- Requirements/package decision: passed, explicitly authorized by the current user message. This supersedes the original package decision only; approved UI and authentication boundaries remain unchanged.
- Design: no new UI/data/API design. Namespace, Kotlin packages and application ID will consistently use `android.aizzak.com`.
- Debug and release use the registered application ID without `.debug`, so the supplied Firebase client matches the development build. Distinct debug/release installations are no longer available with this configuration.
- Preserve the user's Firebase plugin, BoM and Analytics additions and the exact uploaded configuration bytes. Remove the duplicate Android application plugin declaration.
- No authentication implementation, remote Firebase changes, dependency upgrades, signing/release changes, commit, push or PR.

## Baseline and ownership

Baseline HEAD: `ea5b95d`. Existing user edits: app/build.gradle.kts, build.gradle.kts, and untracked app/google-services.json.
Uploaded Firebase configuration SHA-256: `5adb468765528182bdde1b51c161516a11a481f7ca87fc954b9f835b6e55329a`.

- build-release-engineer (/root/bootstrap): app Gradle configuration, Kotlin package/import and source-tree rename, README package documentation.
- Coordinator: this task record, current project memory, suite execution and acceptance.
- Independent android-code-reviewer: focused read-only review after implementation.

## Acceptance and verification

1. Application ID and namespace match Firebase `android.aizzak.com`; Debug uses the same ID.
2. Main/test/androidTest Kotlin packages and imports are consistent with their directories; no old package remains in active source/configuration.
3. Firebase configuration and user dependencies remain intact; duplicate plugin removed.
4. Debug assembly, existing JVM tests and lint pass. Instrumentation APK compiles; no device runtime success is inferred.
5. APK metadata confirms the expected package and launcher activity. Independent review has no unresolved actionable defects.

## Accepted implementation and results

Build engineer handoff accepted: namespace/application ID changed, debug suffix and duplicate application plugin removed, all 15 Kotlin files moved and package/import prefixes updated. Exact text comparison with HEAD confirms no Kotlin behavior/assertion changes. README updated. Firebase configuration hash remains unchanged. No old package remains in active app sources/Gradle/README; historical approved documents remain intact.

- [Build + JVM + lint execution](../../evidence/firebase-package-all.json): exit 0, 175.313 seconds. `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`; 10 tests, zero failures/errors/skips. `:app:processDebugGoogleServices` succeeded.
- [Instrumentation compilation](../../evidence/firebase-package-ui-compile.json): exit 0, 22.844 seconds. `:app:assembleDebugAndroidTest`. Compilation only, no device test execution.
- Both execution reports use source fingerprint `dd82aafef9b1c1eb2a56d7386ac9d88b7de919d8540e3d4b8d5908d452867742`.
- [APK identity inspection](../../evidence/firebase-package-apk-metadata.json): aapt exit 0, installed application ID `android.aizzak.com`, launcher `android.aizzak.com.MainActivity`. Lint: zero errors, 22 warnings. Two additional `UseTomlInstead` suggestions concern the user's inline Firebase declarations; the other 20 categories match the documented foundation warnings. No suppressions or dependency changes added.
- [Independent source review](review.md): no actionable findings; verified all moved Kotlin files and preserved user configuration.

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `8e6fd1fa30e9da67cb0bd311af8044caec2e7f907cdfa979605976b939d0febc`.
Fresh instrumentation APK SHA-256: `5da9e93e10cbe6e61762e8b62302401aadcece341085d4915e2d208789f70234`.
The `all` report also inventories an older instrumentation APK left by the previous build; the separate UI compilation report is the authoritative current test APK evidence.

Current adb inventory finds an authorized Samsung SM-A155F; no AVD exists. Device install/runtime tests were not performed as part of this package/build change. Matching Firebase configuration and successful Gradle processing do not establish Authentication or live Analytics delivery. Firebase Authentication and backend remain outside this task.

Historical foundation reports remain evidence for the earlier source only. No source commit, push, device installation, release build or publication performed.

## Implementation gate

Implementation gate passed with zero errors: .android-suite/evidence/firebase-package-gate.json. Final independent review report SHA-256: c1825de110b590a068f87a7b13f263a2c85a5a8ea723375435877da08489294e.

## Android Studio launch follow-up

User reported a stale launch component combining old application ID com.aizzak.android.debug with new activity android.aizzak.com.MainActivity. Current APK inspection confirms application ID android.aizzak.com and launcher android.aizzak.com.MainActivity. Local run configuration already uses default_activity. Triggered Sync Project with Gradle Files in the existing Android Studio window to refresh its Gradle model.


Follow-up resolved: Gradle Sync completed and Run app succeeded from Android Studio. Device observations confirm the new process is running and android.aizzak.com/.MainActivity is topResumedActivity. Evidence: .android-suite/evidence/firebase-package-studio-launch.json. This follow-up installed/launched the debug app on the connected Samsung SM-A155F; earlier no-install statements describe the package/build verification phase only. No additional source changes, app-data removal or old-app uninstall were needed.

