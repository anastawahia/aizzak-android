# AUTH-LOGIN-01 — focused independent review

Date: 2026-10-31 (Asia/Baghdad).
Reviewer: native android-code-reviewer; no implementation changes, Gradle/device/cloud actions or real credential submission performed by reviewer.
Status: **passed after corrections and inspection of actual build/JVM/lint/device-UI execution evidence**. No remaining actionable P0–P3 defect identified in this scoped review. See final execution appendix for the precise tested boundary.

## Scope and approved boundary

Repair the login button that previously had `enabled=false` and an empty handler, using the already approved Firebase Email/Password journeys. User confirmed the provider is enabled. Include real sign-in, signup/verification, reset, restoration, verification reload/token refresh and local signout. Backend account context remains unavailable; neither verified Firebase identity nor a successful SDK task grants Home/agent access. Preserve accepted package `android.aizzak.com`, current supplied Firebase configuration and user dependencies.

Read actual source, task record and existing approved technical/UI contracts; read Android suite Kotlin/architecture contracts and newly available Firebase Authentication Android guidance. No CLI/project/provider/cloud mutation was needed or performed. Existing BoM 35.1.0 remains in place; `firebase-auth` has no independent version and coroutines Task integration follows the existing 1.10.2 line.

## Review findings and accepted corrections

1. **P2 resolved — premature SignedOut during restore.** Initial repository session changes collector emitted a snapshot from its initial counter before the first SDK auth callback, potentially exposing an early-null user as SignedOut. Current code gates those change snapshots behind an atomic flag set by the first gateway observer callback. The delayed-callback regression asserts only Restoring before the callback and no intervening SignedOut when a user restores.
2. **P2 resolved — signup feedback erased by delayed session delivery.** Initial ViewModel recognized an own credential transition only while busy. A queued SignedIn emission arriving after the operation finished could reset the state and erase verification-send failure/success feedback. Current collector invalidates previous-account results when a nonnull previous UID changes; a null-to-created identity no longer clears own signup feedback. Both verification-send-failure and successful deferred-session regressions preserve their respective error/message.
3. **P2 resolved — credential cleanup lifetime across Activity/ViewModel replacement.** Initial MainActivity created a new repository per AuthViewModel despite using Firebase's process-wide SDK. Old canceled non-cancellable SDK work could clean up a newer repository's session. Current AppContainer owns one lazy process-scoped authRepository, and MainActivity injects that same instance into replacement ViewModels. The repository's mutex holds through late SDK completion/cleanup before admitting the next credential operation; a replacement-caller regression verifies the new identity survives and credential calls serialize.

No remaining actionable findings identified after rereading the actual corrections. No reviewer edits were made to implementation.

## Inspected implementation behavior

- MainActivity passes all seven genuine AuthViewModel actions to the UI; production AuthActions has no no-op defaults. Auth submit invokes entered credentials and is enabled when idle; busy state disables duplicate submissions and exposes localized progress/errors.
- Firebase SDK boundary invokes Email/Password sign-in/create/reset, verification email, reload and forced ID-token refresh using Task await. Firebase owns session/token storage; raw tokens are checked only in memory and never returned into UI state.
- Signup calls creation once and then sends verification only while its operation remains current. A send failure retains the created unverified identity rather than creating it again. Late canceled credential completion is hidden/cleaned before state exposure; cleanup and subsequent calls share the process-scoped mutation boundary.
- Session observation removes its SDK listener on cancellation. Reload checks generation and UID before/after token refresh; token refresh is requested only after verified reload. ViewModel generations suppress late prior-account results and signout clears identity UI immediately.
- Exception mapping emits typed/localized categories, not raw Firebase exception strings. Missing-user sign-in maps to invalid credentials at the operation boundary. Reset suppresses only the SDK missing-user case and uses a neutral localized completion message; network/service failures still show failures.
- SignedIn unverified identities reach verification; verified identities reach CONTEXT_UNAVAILABLE. Navigation registers only sign-in/signup/reset/preferences and routes other auth states to their session gates. No Home/agent route, fake backend context, role or token is created. Retry at the unavailable boundary refreshes identity and cannot bypass missing backend authorization.
- Passwords use non-saveable Compose remember, not SavedStateHandle/DataStore or diagnostic logs. Preferences remain reachable before/after authentication. Existing AppCompat locale/theme behavior is retained, and new feedback/session strings have English/Arabic resources.
- Supplied Firebase JSON hash remains `5adb468765528182bdde1b51c161516a11a481f7ca87fc954b9f835b6e55329a`; no raw project keys/config contents are reproduced here.

## Test-source review and limits

Auth-specific JVM sources contain 6 repository-gateway cases and 9 ViewModel cases. They cover delayed restore/listener removal, late canceled SDK completion and replacement serialization, verification/token conditions, restored-user gates, validation/duplicate dispatch, signup-send failure/success ordering, neutral reset, late signout and account switches. The 6 new Compose cases use explicit fake callbacks and synthetic sample data to exercise real submit/disabled/error/retry/signup/reset/session actions and no-Home boundaries; the prior 3 local UI cases were adapted to the required auth callback contract. Fakes are in tests only, and these tests do not submit live Firebase credentials.

This is authored-test inspection until actual coordinator execution evidence is appended. SDK gateway tests establish adapter coordination with a fake SDK seam, not compatibility of live Firebase environment/error responses/email delivery. Compose tests exercise isolated screens/routes, not a live sign-in to Firebase. Backend account context, real server logout, live auth/mail delivery, production Analytics and full device/TalkBack/locale matrix remain outside the claimed result.

Reviewer commands: read-only Git/status/rg/Get-Content and Python SHA-256 generation; `git diff --check` returned 0 with line-ending warnings only. No suite module import or bytecode creation. This report is the only reviewer write. Coordinator owns actual build/lint/JVM/UI compilation and any permitted device test execution; later implementation fixes require scoped re-review.

## Reviewed artifact hashes

| Path | SHA-256 |
|---|---|
| `app/src/main/java/android/aizzak/com/AppContainer.kt` | `e580de728b9edbec723ec984df617b6d8ad81dafe1098ff28e610eb9e97015b9` |
| `app/src/main/java/android/aizzak/com/MainActivity.kt` | `0fd7b98b612c76247d42aeb04313c4603b4466d6e743317d63c984da5e593045` |
| `app/src/main/java/android/aizzak/com/feature/auth/AuthRepository.kt` | `3de62af9cdcbe6bf8ee19261aebb4b44eb8d99078e23cad1ac5fcbc124ced2a7` |
| `app/src/main/java/android/aizzak/com/feature/auth/AuthViewModel.kt` | `67144b06845272bfa8c8a820893af8d3674620590e7e9f5c2ce5a25449240638` |
| `app/src/main/java/android/aizzak/com/data/auth/FirebaseAuthRepository.kt` | `2084c7b76e850dc70116e34e3e98e9240ccba124a800bfddc64051fa7b1ffa34` |
| `app/src/main/java/android/aizzak/com/feature/auth/AuthScreen.kt` | `27da70f0f9b0cbe6ca7bb1ecd12e7a5a9d7708232d187f85c983cb755d6a118b` |
| `app/src/main/java/android/aizzak/com/feature/auth/AuthSessionScreen.kt` | `747e9fef0fbe22a487de35442c93ba41e20e9d95e34f0c018992bfb147e989e3` |
| `app/src/main/java/android/aizzak/com/navigation/LocalFoundationApp.kt` | `92e271f5820b2a6e9b7bc931fbe769132ac5373ee0fea3cb824edc3ebd58c429` |
| `app/src/main/res/values/strings.xml` | `e76f6457ef0bc3b859e53c22f7a90aa75f1aa48eacc90f3ef179db21f18f3801` |
| `app/src/main/res/values-ar/strings.xml` | `e5a7d4cda749e74772f9a9b4bea3bbda9ae5e6474e54f793334a7944bfcef9ba` |
| `app/src/main/AndroidManifest.xml` | `176c1cbed3210dcd3a96bb43ee4b0f75a8fbf273e7f704bcaa98102df80a7037` |
| `app/src/test/java/android/aizzak/com/data/auth/FirebaseAuthRepositoryTest.kt` | `f5075feabfb101c072c1e325bf6fd1166a8ffb59d0453995fb417f66916b3420` |
| `app/src/test/java/android/aizzak/com/feature/auth/AuthViewModelTest.kt` | `78566f526038ec17192af9baf4597fda98cc421a6c372354645abfefc2626908` |
| `app/src/androidTest/java/android/aizzak/com/AuthUiTest.kt` | `489f8a1e69978cbfe7e409173dc8b7fa5dd02db4ee70c53d8309969338af0b4f` |
| `app/src/androidTest/java/android/aizzak/com/LocalFoundationUiTest.kt` | `cee33c25f4de021a9eaf3bcae4e5b54bbddea3104db0bd1c3c1e7986dcdf5382` |
| `app/build.gradle.kts` | `c546bea458041b2ef62dc7e13fe4310b5f24cbd1c84efc25f15633a20f9e262d` |
| `gradle/libs.versions.toml` | `93670c7fa92c89762e88c0dfc1d90f8d30d13748dc02a478c95245d39d3622b1` |
| `app/google-services.json` | `5adb468765528182bdde1b51c161516a11a481f7ca87fc954b9f835b6e55329a` |

## Final execution evidence and frozen acceptance

Reviewer read the actual coordinator execution reports and rehashed every item in the reviewed table: all reviewed implementation/test/configuration hashes remain unchanged. The three reports share source fingerprint `85dc70f2a9583432acbc977dbffcc2fbb16e8bedce45c7876a9ad002bc960a28`:

- [auth-login-all.json](../../evidence/auth-login-all.json): status passed, kind all, final command exit 0. Debug assembly, JVM tests and lint completed. Actual test summary is 25 tests with zero failures/errors/skips (15 auth cases plus 10 prior settings/persistence cases).
- [auth-login-ui-compile.json](../../evidence/auth-login-ui-compile.json): status passed, kind ui-compile, final command exit 0. Fresh instrumentation APK compiled.
- [auth-login-ui.json](../../evidence/auth-login-ui.json): status passed, kind ui, final command exit 0. Output and actual connected JUnit summary show 9 tests on Samsung SM-A155F / Android 15 (API 35), zero failures/errors/skips. The six auth and three local UI cases execute isolated Compose screens/routes using test-only fake callbacks and sample data. These device results establish the covered UI interactions, not live Firebase sign-in or production session restoration.

Actual lint XML: 0 errors/fatal issues, 24 warnings. Seventeen dependency/AGP update notices, 3 UseTomlInstead suggestions for inline Firebase dependencies, and 4 previous localeConfig/API33, backup-rule, unused scaffold icon and launcher-shape warnings. No new code error or auth-specific actionable defect was identified from them. Existing pinned dependencies/user additions are preserved; no check or test was disabled or result relabeled.

Actual APK files were independently hashed and match the fresh ui-compile/UI execution reports:

| Artifact | SHA-256 |
|---|---|
| Debug app APK | `b8a424ca28cfc46ddd29cc928d342726dc1e2dd881960538ba59e28ab94fdfa8` |
| Fresh androidTest APK | `702038fbeff4a847f0f0c58a16dfbe7007ee3686ae68bdd97b4027ba6fe5dc51` |

The all report also scanned a historical preexisting androidTest APK, since its task set does not compile instrumentation sources. Use the fresh ui-compile/UI artifact hash above; the earlier scanned test APK is not the current test artifact.

Final focused independent review is **passed** for AUTH-LOGIN-01. Production source is wired to genuine Firebase SDK operations and the reported disabled-button defect is removed. Actual live Firebase credentials/account creation/mail delivery were not exercised or asserted. Backend origin/account context is still absent and remains a mandatory non-bypass gate before Home. Full accessibility/locale/device matrix, real server logout, broader account features and release/publication remain outside this repair's evidence. Reviewer did not run Gradle, install apps, submit credentials or change remote services; the coordinator ran the recorded authorized checks.
