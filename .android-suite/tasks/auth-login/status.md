# AUTH-LOGIN-01

Date: 2026-10-31 (Asia/Baghdad).
Status: completed — local Firebase client repair, verification, independent review and implementation gate passed.

User report: login button does not respond. Root cause: foundation AuthScreen submit uses enabled=false and an empty callback. Firebase configuration/Analytics and package migration alone did not implement Authentication. User confirmed Email/Password provider is enabled.

## Scope and checkpoints

Resume existing approved V1-AUTH-01/02/03 journeys from V1-AGREED-SCOPE.md 1.1, TECHNICAL-DESIGN.md 1.1, UI-DESIGN.md and DESIGN-REVIEW.md (user approval dated 2026-10-30). Requirements/design checkpoints remain passed for these unchanged contracts; no new visual design or API contract. Implement Firebase email/password login, signup and verification, reset email, restore session, refresh verification/token, and local signout, with validation/loading/typed localized failures.

No API origin/context integration is supplied. A verified Firebase identity remains at the approved account-context unavailable/retry/signout boundary. Never grant Home/agent access or fabricate roles. Do not add change-password/account/backend/agent scope to this button repair. No remote project edits, test-account creation, real credential collection, commit, push or release.

Baseline HEAD ea5b95d plus the user's Firebase additions and accepted uncommitted FIREBASE-PACKAGE-01 migration. Preserve all those changes and exact google-services.json bytes.

## Ownership

- Core developer /root/local_state: auth model/repository/Firebase SDK boundary, ViewModel and JVM tests.
- Compose developer: AuthScreen, verification/context screens, LocalFoundationApp/MainActivity bindings, localized resources and Compose regression tests, after core contracts freeze.
- Coordinator: dependencies/catalog, execution/evidence and project memory.
- Independent reviewer: focused correctness/security review after implementation.

## Acceptance / checks

1. Valid sign-in invokes genuine Firebase auth; input errors are visible, loading prevents duplicates and failures allow retry.
2. Signup sends verification once after successful account creation; a send failure never recreates identity. Reset completion is neutral about account existence.
3. Restored/created identities remain behind verification and context boundaries. Refresh reloads user and renews token after verification; no Home bypass.
4. Signout clears identity-dependent UI, stale account results are ignored, passwords are not saved/logged.
5. English/Arabic and preferences remain available. Meaningful JVM and Compose interaction checks, debug assembly/lint, focused independent review. Device test execution only with isolated test UI/fakes; no real Firebase account creation or credential submission.

## Accepted implementation

Core and Compose handoffs inspected and accepted. Real Firebase SDK Email/Password actions, typed failures, ephemeral form passwords, neutral reset completion, signup verification, restored sessions and verification/token refresh are wired through AuthViewModel. Account services stay unavailable until the API context contract is configured. AppContainer shares the Firebase adapter across ViewModel/Activity lifetimes; credential mutations serialize through late SDK completion, with stale identity cleanup. Existing Firebase config bytes and user's package/dependency changes preserved.

Independent reviewer raised three P2 issues; all corrected and reread: early signed-out before initial SDK callback, lost signup feedback after deferred session delivery, and repository lifetime across Activity replacement. Coordinator completed permitted scoped edits when specialist quota was temporarily unavailable; independent reviewer subsequently resumed and reviewed actual final code. See [review.md](review.md), with inspected file hashes.

## Actual verification (2026-10-31)

All source reports share fingerprint `85dc70f2a9583432acbc977dbffcc2fbb16e8bedce45c7876a9ad002bc960a28`.

- [all](../../evidence/auth-login-all.json): assembleDebug + testDebugUnitTest + lintDebug, exit 0. 25 JVM tests, zero failures/errors/skips (15 auth-specific, 10 existing). Lint 0 errors / 24 warnings: 17 dependency update notices, 3 catalog suggestions, 4 existing attribute/backup/icon warnings. No suppressions or toolchain upgrade.
- [UI compilation](../../evidence/auth-login-ui-compile.json): assembleDebugAndroidTest, exit 0.
- [UI execution](../../evidence/auth-login-ui.json): connectedDebugAndroidTest, exit 0. All 9 isolated Compose cases passed on Samsung SM-A155F / Android 15 (API 35), en-GB. Six auth interactions plus three retained local UI cases; no real Firebase credential submission or account creation.
- [Device launch](../../evidence/auth-login-device-launch.json): adb install -r succeeded, am start -W reported Status ok / cold launch, running process and resumed MainActivity confirmed. User data retained. No actual sign-in/mail-delivery success claimed.

Current debug APK: `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `b8a424ca28cfc46ddd29cc928d342726dc1e2dd881960538ba59e28ab94fdfa8`. Current test APK SHA-256 `702038fbeff4a847f0f0c58a16dfbe7007ee3686ae68bdd97b4027ba6fe5dc51`. The all report's AndroidTest artifact is a historical directory scan; only UI compilation/execution artifacts establish the new test APK.

Firebase config unchanged SHA-256 `5adb468765528182bdde1b51c161516a11a481f7ca87fc954b9f835b6e55329a`.

Remaining product integration: backend origin/issuer/context/logout contract, account permissions and authenticated Home. Live Firebase sign-in/email delivery, TalkBack and full locale/device matrix are outside this fake-test/startup verification. No commit, staging, push, PR, release or publication performed. PR checkpoint not applicable to requested local fix.

## Final gate and handoff

Final independent review SHA-256 `ae50cba824de7b8dbc691860c9d7fd76a24e8216abd4db521bbace897a1eb868`, includes actual JVM/UI execution evidence. [Implementation gate](../../evidence/auth-login-gate.json) passed exit 0 with zero errors; [gate input](../../evidence/auth-login-gate-input.json) binds the matching source fingerprint and exact report hashes.

Local requested repair delivered and updated app running on the phone. Next integration work is the genuine account-context API; do not open Home before that contract succeeds. User may now exercise their own existing Firebase credentials; none were collected or submitted by automation.
