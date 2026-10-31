# FIREBASE-PACKAGE-01 — focused independent review

Date: 2026-10-31 (Asia/Baghdad)
Reviewer: native android-code-reviewer, read-only implementation review.
Status: **passed for inspected package/configuration migration**, no actionable P0–P3 findings identified. Build/runtime results are separate.
Baseline: HEAD `ea5b95d`; inspected tracked deletions and all new/untracked replacement Kotlin files as well as current Gradle/manifest/README/configuration.

## Scope and observations

User explicitly requested the Firebase client package `android.aizzak.com`. Current `namespace` and `applicationId` both match it. Debug suffix is removed, so the supplied single Firebase Android client also matches the debug variant. Parsed only the package-name field and file hash for review; no raw configuration keys or identifiers are reproduced here.

Uploaded `app/google-services.json` SHA-256 remains `5adb468765528182bdde1b51c161516a11a481f7ca87fc954b9f835b6e55329a` as required. Existing user edits for Google Services plugin 4.5.0 and Firebase BOM 35.1.0/Analytics remain present. The Android application plugin is currently applied once via its version-catalog alias, with no duplicate explicit application-plugin declaration. Root Android plugin alias remains apply-false.

An exact normalized-text comparison of each of the 15 previous Kotlin files from `git show ea5b95d:<old-path>` against its moved replacement found **all 15 match after only replacing `com.aizzak.android` with `android.aizzak.com`**. Main, JVM-test and androidTest directory/package/imports all align. No old identifier remains in active source/Gradle/README. Existing behavior and all test assertions were preserved by the rename.

Manifest launcher `.MainActivity` resolves to `android.aizzak.com.MainActivity` because the module namespace and application ID now agree. Source class and resource imports use that namespace. No hardcoded old manifest/provider authority or ProGuard class reference was found requiring migration. Actual merged manifest/test APK verification belongs to the coordinator's current build evidence.

## Package/debug tradeoff

Debug and release intentionally now share an application ID to use the one supplied Firebase client. They cannot be installed side by side; replacing an installation also requires compatible signing. Changing from the previous `com.aizzak.android.debug`/`com.aizzak.android` identity creates a distinct Android application identity, so existing local preferences are not automatically migrated. No uninstall/data deletion is requested or performed here. If separate development/release installs are needed later, register a distinct Firebase debug client before restoring a suffix.

## Verification and limits

Reviewer ran read-only Git diff/status/show, rg, content reads, exact Python text comparison/config-package parsing/hash generation and `git diff --check ea5b95d` (exit 0; CRLF warnings only). No Python suite import, bytecode artifacts, implementation edits, Gradle command or service/runtime request was performed. This report is the only reviewer write.

Build/unit/lint and androidTest compilation are running under the coordinator; no passing result is inferred here. Firebase authentication dependency/flows and backend integration are outside this package-only change. Matching a Firebase client and processing its configuration does not prove sign-in, Analytics delivery, Firebase environment correctness, API readiness or device behavior. No release/push/publication performed.

## Reviewed hashes

| Path | SHA-256 |
|---|---|
| `app/src/androidTest/java/android/aizzak/com/LocalFoundationUiTest.kt` | `730dc6f372d5811500d92b66791f6c092669e6ef9ced817a2cc42e10d79e8495` |
| `app/src/main/java/android/aizzak/com/core/designsystem/AizzakTheme.kt` | `a1d8e854e65270009ebdbbe6cf0df3f022de2cb40a186f8ecc5d6edc0e11dd01` |
| `app/src/main/java/android/aizzak/com/core/designsystem/Components.kt` | `a45ccc6d68974fcd4e9e298c14f18eea051ca3de0f5c84b8d32ee453a068f7c6` |
| `app/src/main/java/android/aizzak/com/core/model/AppPreferences.kt` | `018f3767867650188f15e6fc151266d8b30ec3f4ff77cba0e516d38f82657225` |
| `app/src/main/java/android/aizzak/com/data/preferences/DataStorePreferencesRepository.kt` | `6ccbe390a5a74a58dfef19de5e124d2682ea9f0a32fd7558fb89b3ef29698137` |
| `app/src/main/java/android/aizzak/com/data/preferences/PreferencesRepository.kt` | `8497c806cd23f24e72f8b401ccc9a55d4a1802c3e89ae1cbab17514cb65eadef` |
| `app/src/main/java/android/aizzak/com/feature/account/AccountScreen.kt` | `0c235912e0fc33d72f571be45c465df72c19d24661f6d21d57f8861cf301df13` |
| `app/src/main/java/android/aizzak/com/feature/auth/AuthScreen.kt` | `0f3320a92ccfc614ce74bf7ea33e370241ad7070a10cf7609d87aff369d70b33` |
| `app/src/main/java/android/aizzak/com/feature/home/HomeScreen.kt` | `aa32f34ea0d1ccafaacc75979ff479f2337585bea8bf184f8d31c7e7ecb5da59` |
| `app/src/main/java/android/aizzak/com/feature/settings/SettingsScreen.kt` | `90302f015580047d9a801bc2f9775436c53bdb988a4765c98d3721207a6aafdd` |
| `app/src/main/java/android/aizzak/com/feature/settings/SettingsViewModel.kt` | `7260dcec76a3c77cf5380264317adc4232e61efe3aa8bc704b8c361705710d0d` |
| `app/src/main/java/android/aizzak/com/MainActivity.kt` | `34880e7c76e2cf2b22239bd3ed46965bf8edf09975dd18270602a2d15d989030` |
| `app/src/main/java/android/aizzak/com/navigation/LocalFoundationApp.kt` | `f831eb3f6f54375e7e5e62c9dcd2cecf1286872b9d8b93e6a0724279047ff8a4` |
| `app/src/test/java/android/aizzak/com/data/preferences/DataStorePreferencesRepositoryTest.kt` | `b6f57b567f54c6657e9166ac954c4c6611168b2884f4929ca04e5eeabca86fd1` |
| `app/src/test/java/android/aizzak/com/feature/settings/SettingsViewModelTest.kt` | `a0275126fe0e3e61920eb032c25a3e9138a3a88e523b26d6a545137a13134fdf` |
| `app/build.gradle.kts` | `6abcb5e0c3819f9498a3c15ff853f0bf8982a731586b1244e80c17a2f2e674b0` |
| `build.gradle.kts` | `6a9adad9d45b00f13743c06993e7780e1185b5366a0e299a5a125d0e21d898e1` |
| `app/src/main/AndroidManifest.xml` | `176c1cbed3210dcd3a96bb43ee4b0f75a8fbf273e7f704bcaa98102df80a7037` |
| `README.md` | `ec96950b506ce4265b4461f353b972587e9557c18828ef3fec725e556dd7ee11` |
| `app/google-services.json` | `5adb468765528182bdde1b51c161516a11a481f7ca87fc954b9f835b6e55329a` |

## Final verification evidence — frozen review

The earlier pending-build paragraph records the first review stage. Actual coordinator execution reports are now available and were independently read by this reviewer:

- [firebase-package-all.json](../../evidence/firebase-package-all.json): `kind=all`, `status=passed`, final exit 0, 10 JVM tests with zero failures/errors/skips. Output includes the successful `processDebugGoogleServices` task. Test report class names use `android.aizzak.com`.
- [firebase-package-ui-compile.json](../../evidence/firebase-package-ui-compile.json): `kind=ui-compile`, `status=passed`, final exit 0. This establishes instrumentation APK compilation only; no device tests ran.
- Both reports carry the same source fingerprint: `dd82aafef9b1c1eb2a56d7386ac9d88b7de919d8540e3d4b8d5908d452867742`. Reviewer rehashed every artifact in the reviewed table; **all reviewed source/configuration hashes remain unchanged**. No suite import or implementation write was used to attach this execution fingerprint.
- Actual lint XML contains 0 errors, 0 fatal issues, 22 warnings: 16 dependency/AGP update notices, the previous localeConfig/API33, data-extraction-rules, unused scaffold icon and launcher-shape warnings, plus 2 `UseTomlInstead` suggestions for the preserved user inline Firebase dependencies. These two style suggestions do not require changing the user's dependencies for this package-only scope. Existing residual device/release-hardening limits remain as described in the foundation review.

Read-only `D:\SDK\build-tools\36.0.0\aapt.exe dump badging` of the actual debug APK returned package `android.aizzak.com` and launcher `android.aizzak.com.MainActivity`, confirming actual packaged manifest resolution. Command exit was 0. Actual file hashes were independently checked:

| Artifact | SHA-256 |
|---|---|
| Debug APK | `8e6fd1fa30e9da67cb0bd311af8044caec2e7f907cdfa979605976b939d0febc` |
| Fresh androidTest APK | `5da9e93e10cbe6e61762e8b62302401aadcece341085d4915e2d208789f70234` |
| Unchanged supplied Firebase JSON | `5adb468765528182bdde1b51c161516a11a481f7ca87fc954b9f835b6e55329a` |

The all report's artifacts list also contains a historical androidTest APK hash because the all tasks do not compile instrumentation tests. Use the later ui-compile report and actual fresh androidTest APK hash above for that artifact; do not treat the earlier entry as current or as device execution evidence.

Final focused review remains **passed**, with no actionable P0–P3 findings. Firebase config processing/package matching and compile/JVM/lint results are established; authentication, Analytics delivery, API correctness, device runtime and release/publication are not established by these checks.

Packaged identity/hash/lint observations are also retained in [firebase-package-apk-metadata.json](../../evidence/firebase-package-apk-metadata.json), read by the reviewer. Coordinator reports a currently authorized Samsung SM-A155F, with no AVD; no installation/device tests were performed for this package-only scope. Runtime verification is unexecuted, not declared impossible because of device absence.
