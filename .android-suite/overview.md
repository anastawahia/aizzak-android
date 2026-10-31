# AIZZAK Android

Native Kotlin / Jetpack Compose / Material 3 foundation, approved 2026-10-30. Minimum Android API 26. Governing scope: ../V1-AGREED-SCOPE.md; reviewed design: ../DESIGN-REVIEW.md. Active record: [AUTH-LOGIN-01](tasks/auth-login/status.md); historical foundation: [FOUNDATION-01](tasks/foundation/status.md).

Foundation includes authentication/session, account, settings and Home with three descriptive unavailable agents. Agent functionality/workspaces remain deferred. Firebase/API production readiness is not established.

Delivered foundation slice: single :app scaffold, localized forms, working language/theme preferences, shared approved design, stateless Home/Account preview content. Original foundation checks apply to its historical source. Current Firebase client/session repair and verification are tracked below; backend account integration remains outstanding.

Completed package task: [FIREBASE-PACKAGE-01](tasks/firebase-package/status.md). On 2026-10-31 the user supplied Firebase configuration and explicitly requested the package `android.aizzak.com`, superseding the original package. Both debug and release now use this ID to match the supplied registration. Its build, 10 JVM tests, lint and UI test compilation passed; independent review found no actionable defects. Firebase client implementation follows in AUTH-LOGIN-01; backend API integration remains unavailable.


Completed current task: [AUTH-LOGIN-01](tasks/auth-login/status.md), real Firebase email/password client journeys after the disabled foundation submit report. Debug build, 25 JVM cases, 9 isolated device UI cases, UI compilation, independent review and implementation gate passed. Lint 0 errors / 24 documented warnings. New APK installed and MainActivity resumed on the connected Samsung. No live Firebase credentials submitted. Account context API remains unavailable; no Home bypass. Next integration: backend account context and related account contract.

