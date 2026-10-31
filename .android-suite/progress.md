# Active delivery

2026-10-31 AUTH-LOGIN-01 completed locally: replaced disabled/no-op submit with real Firebase Email/Password client actions, signup verification, reset, restoration, refresh/token renewal and signout. Explicit verified-account context gate retained. Build + 25 JVM cases + lint (0 errors/24 warnings) + UI compilation + 9 device Compose cases passed. Three independent-review P2 findings corrected and re-reviewed; final independent review and implementation gate passed for fingerprint85dc70f2a9583432acbc977dbffcc2fbb16e8bedce45c7876a9ad002bc960a28. Updated APK installed without deleting data and MainActivity resumed on Samsung API35. No live Firebase credentials or backend authorization tested. Details: [AUTH-LOGIN-01](tasks/auth-login/status.md). Remaining: account-context API and authenticated account/Home integration.

2026-10-31: active task [FIREBASE-PACKAGE-01](tasks/firebase-package/status.md), user-authorized package rename to `android.aizzak.com` to match the newly supplied Firebase configuration. Debug uses the same ID. Build, 10 JVM tests, lint (zero errors/22 warnings), instrumentation compilation and focused independent review passed. APK metadata confirms the new package and launcher. A physical device is now available, but no device install/runtime tests were performed in this scoped change; previous foundation outcomes below are historical.

2026-10-30: design approved by user «اعتمد». Package com.aizzak.android confirmed; Firebase/API not ready, user explicitly requested foundation/local parts. See [FOUNDATION-01](tasks/foundation/status.md) for approval hashes, dependencies, evidence and next action.

Local foundation delivered: scaffold and official checksummed wrapper, persistent theme settings, AppCompat language selection/initialization, approved fixed palette/logo, localized signed-out forms/preferences and stateless Home/Account previews. No production authentication bypass.

Final suite all passed (build + 10 JVM tests, zero failures/errors/skips + lint zero errors/20 documented warnings); UI test compilation passed (3 authored cases, no device execution). Two early failures retained and corrected: selectable overload, then Windows JVM test storage configuration. Independent reviewer verified system-bar contrast fix and test-only storage correction; final fingerprint reconciliation is recorded in the active task. Debug APK path/hash and evidence links are in that record.

Overall integrated foundation remains partial: Firebase/API unavailable by user confirmation; actual authentication/session/account flows require those inputs. No connected device/AVD means runtime locale/recreation/accessibility validation outstanding. No PR/push/release/publication performed.

Final local implementation gate passed (exit 0, zero gate errors) with matching build/test/UI-compile/review source fingerprint and exact report hashes. Independent reviewer accepted current files and documented all residual lint/device limitations. Local delivery complete; resume point is real integration/device validation once required inputs/target become available.


2026-10-31 FIREBASE-PACKAGE-01 follow-up: implementation gate passed. Resolved Android Studio stale package launch by Sync Project with Gradle Files followed by Run app. New package android.aizzak.com is running with MainActivity resumed on connected Samsung SM-A155F. Startup smoke check only; no authentication/device instrumentation tests performed.


2026-10-31 AUTH-LOGIN-01 running: confirmed disabled submit and absent FirebaseAuth integration. User confirmed Email/Password enabled. Resume existing approved auth/verification/context journeys, preserve context boundary, implement genuine Firebase actions and isolated regression tests. See tasks/auth-login/status.md.

