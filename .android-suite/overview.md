# AIZZAK Android

ACCOUNT-CONTEXT-01 local integration complete: user confirmed real Home access and successful immediate re-login after logout on the final APK. Current build, 55 JVM cases, 15 device UI cases, Lint, independent review and scoped gates pass. Android logout is local-only per backend reply. Root AGENTS.md persists the user's prohibition on backend modifications and the requirement for Markdown requests to its developer. [Acceptance](tasks/account-context/local-logout-acceptance.md). Earlier awaiting-user/backend-blocker notes below are historical.

2026-10-31 continuation: backend owner repaired expired Vault credentials and restored master. Unmerged backend logout changes were not approved; runtime logout is subject-wide denial. Android now signs out locally per [backend reply](tasks/account-context/backend-reply.md), and passes build, 55 JVM cases, 15 device UI cases, Lint (0 errors/27 warnings), independent review and scoped evidence gates. Final APK installed/launched; live account acceptance awaits the user's check. User prohibits backend modifications; root AGENTS.md records that needed backend changes must be requested in Markdown. Earlier counts/backend blockers below are historical.

Native Kotlin / Jetpack Compose / Material 3 foundation, approved 2026-10-30. Minimum Android API 26. Governing scope: ../V1-AGREED-SCOPE.md; reviewed design: ../DESIGN-REVIEW.md. Active record: [ACCOUNT-CONTEXT-01](tasks/account-context/status.md); historical foundation: [FOUNDATION-01](tasks/foundation/status.md).

Current account integration is implemented: valid backend context opens Home/Account/Settings, with real Firebase password change, bounded logout and stale-account cancellation. Local build, 54 JVM cases and lint pass; independent security and final scoped correctness reviews pass. Local runtime is currently blocked by Vault AppRole credential rejection; human renewal is pending. Device UI and installation evidence are recorded in the active task. Production readiness and real-account success remain unestablished. Earlier missing-integration descriptions below are historical.

Foundation includes authentication/session, account, settings and Home with three descriptive unavailable agents. Agent functionality/workspaces remain deferred. Firebase/API production readiness is not established.

Delivered foundation slice: single :app scaffold, localized forms, working language/theme preferences, shared approved design, stateless Home/Account preview content. Original foundation checks apply to its historical source. Current Firebase client/session repair and verification are tracked below; backend account integration remains outstanding.

Completed package task: [FIREBASE-PACKAGE-01](tasks/firebase-package/status.md). On 2026-10-31 the user supplied Firebase configuration and explicitly requested the package `android.aizzak.com`, superseding the original package. Both debug and release now use this ID to match the supplied registration. Its build, 10 JVM tests, lint and UI test compilation passed; independent review found no actionable defects. Firebase client implementation follows in AUTH-LOGIN-01; backend API integration remains unavailable.


Completed current task: [AUTH-LOGIN-01](tasks/auth-login/status.md), real Firebase email/password client journeys after the disabled foundation submit report. Debug build, 25 JVM cases, 9 isolated device UI cases, UI compilation, independent review and implementation gate passed. Lint 0 errors / 24 documented warnings. New APK installed and MainActivity resumed on the connected Samsung. No live Firebase credentials submitted. Account context API remains unavailable; no Home bypass. Next integration: backend account context and related account contract.

