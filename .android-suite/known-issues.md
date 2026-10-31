# Known issues

Integration input blocker (updated 2026-10-31): Firebase Android configuration is supplied and its matching package rename is tracked in [FIREBASE-PACKAGE-01](tasks/firebase-package/status.md). Email/Password is enabled per user confirmation. Firebase client implementation and checks are tracked in [AUTH-LOGIN-01](tasks/auth-login/status.md). API URL/account-context semantics remain outstanding: verified identity stays at account-services unavailable, without fabricated roles or Home access. No live Firebase credential test has been performed.

Device verification (2026-10-31): user reconnected Samsung SM-A155F / API 35 after an initial empty adb inventory. AUTH-LOGIN-01 passed all 9 isolated Compose cases on the phone, and the new APK was installed preserving data and launched successfully. These checks do not submit real Firebase credentials. TalkBack, live mail delivery and full locale/device matrix remain unverified. See [auth task](tasks/auth-login/status.md).

Build/lint/test/review findings must be linked from [active status](tasks/foundation/status.md) after execution; this file does not assert a clean audit.

2026-10-31 resolved development issue: after package migration Android Studio retained com.aizzak.android.debug in its Gradle model. Sync Project with Gradle Files then Run app refreshed the model and successfully launched android.aizzak.com/.MainActivity on the connected phone. No source workaround or data deletion required.
