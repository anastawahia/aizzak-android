# FIREBASE-SETTINGS-01

Date: 2026-10-31 (Asia/Baghdad).
Status: completed — Android setup, matching remote configuration and enabled Email/Password provider verified.

User request: set up Firebase in this app and verify settings. Existing approved Firebase registration and Email/Password implementation are already present. This task verifies those settings without changing the approved product, architecture or journeys.

## Scope and ownership

- Coordinator: Firebase plugin environment/project/app/config comparison, build verification and documentation.
- Independent android-code-reviewer: read-only client configuration review, accepted with no actionable findings; see [review.md](review.md).
- Requirements/design checkpoints: not applicable; existing approved package and authentication contracts remain unchanged.
- PR checkpoint: not applicable to this local configuration verification; no push or PR requested.

## Verified settings

- Firebase CLI 15.34.2 is available and authenticated; Firebase plugin access to the existing project succeeded.
- Active Firebase plugin project set to `aizzak-agent`, matching the previously supplied app config and package approval.
- Remote project `aizzak-agent` is ACTIVE; project number `987003822796`.
- Remote Android app is ACTIVE: `1:987003822796:android:d785ab43fb63755bfb1869`, package `android.aizzak.com`.
- Firebase plugin SDK configuration retrieval was compared with the local JSON; the parsed JSON objects are identical, including config identifiers, API key and OAuth client entries. Local configuration bytes preserved, SHA-256 `5adb468765528182bdde1b51c161516a11a481f7ca87fc954b9f835b6e55329a`.
- Google Services plugin 4.5.0, Firebase BoM 35.1.0, Auth and Analytics are already configured. Generated debug resources and merged manifest confirm automatic initialization and network permissions.
- No firebase.json is required for these Android client SDKs. No additional Firebase services or deployments were introduced.
- After the user signed in to the separate browser session, Firebase Console for `aizzak-agent` showed Email/Password **Enabled**. Google also showed Enabled; the current Android client implements Email/Password only. No provider settings were changed.

## Recorded verification before account-context integration

Executed through the Android suite verifier: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain --no-daemon`.

[Build/test/lint report](../../evidence/firebase-settings-all.json): passed, exit 0, 11.234 seconds. Source fingerprint `85dc70f2a9583432acbc977dbffcc2fbb16e8bedce45c7876a9ad002bc960a28`. Gradle incremental results accepted because application source and inputs are unchanged. 25 JVM cases, zero failures/errors/skips; lint zero errors and 24 existing warnings. The artifact scan also includes the existing instrumentation APK; this task did not rerun device tests.

Debug APK SHA-256: `b8a424ca28cfc46ddd29cc928d342726dc1e2dd881960538ba59e28ab94fdfa8`.

## Remote provider check and limitations

Existing AUTH-LOGIN-01 records the user's confirmation that Email/Password is enabled. Current Firebase plugin tools and CLI auth commands do not expose provider configuration reads. Browser fallback initially reached Google sign-in; the user completed sign-in and reported `تم تسجيل الدخول`. Direct inspection of [the project's Sign-in method page](https://console.firebase.google.com/project/aizzak-agent/authentication/providers) then confirmed Email/Password Enabled. No credentials were collected or entered by automation.

At the time of this settings check, live sign-in/email delivery and Analytics event delivery were not tested, and backend account-context integration was still pending. Certificate/API restriction checks were not performed. No remote Auth settings, accounts, billing, application code, release or deployment changed in this settings task.

## Subsequent delivery and documentation

ACCOUNT-CONTEXT-01 subsequently completed the Android API integration and local-only logout. Its final source passed build, 55 JVM cases, 15 device UI cases and Lint (0 errors/27 warnings); the user confirmed actual Home access and successful re-login after logout. See [current acceptance](../account-context/local-logout-acceptance.md). The 25-case report above remains historical evidence for this earlier settings check, not a new run against the final account-context source. No Firebase provider state was rechecked during documentation-only work.

The user then requested «قم بالالتزام وتوثيق كل شيء», authorizing inclusion of this settings record, independent review, existing execution evidence and README/progress references that were intentionally excluded from Android integration commit `5fb15cf`. This follow-up changes documentation/evidence only and does not modify Firebase or backend configuration.
