# API contracts

Reference: [OpenAPI 3.1 contract](../api-documents/openapi.yaml), API version 1.0.0, provenance documented in [API README](../api-documents/README.md).

Originally the user reported Firebase Android configuration and API test URL unavailable. On 2026-10-31 the user supplied app/google-services.json and explicitly requested the application package `android.aizzak.com` to match that registration; see [FIREBASE-PACKAGE-01](tasks/firebase-package/status.md). Google Services/Analytics package setup passed its recorded checks. The user confirmed Email/Password is enabled. [AUTH-LOGIN-01](tasks/auth-login/status.md) implements the Firebase client boundary and UI; current verification is recorded there. Absolute HTTPS API origin, accepted issuer, and /me/context and /me/logout semantics remain required for backend account integration.

Firebase sign-in, signup, verification mail, password reset, persisted-session observation, reload/token renewal, and local signout use the Android SDK. A verified Firebase identity remains at CONTEXT_UNAVAILABLE with retry/signout; it never supplies roles or opens Home without account context. Automated auth tests use isolated fakes, not real account credentials.

Required future behavior follows approved technical design: verification then account context; never default permissions; refresh at most once for a safe read on 401; distinguish 403; clean local identity and account work on logout despite server failure; ignore late results from a prior account. No real tokens, credentials or personal response payloads belong in project evidence.
