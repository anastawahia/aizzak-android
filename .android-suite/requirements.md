# Requirements and feature inventory

Governing requirements: [agreed scope 1.1](../V1-AGREED-SCOPE.md). Approval and original document hashes: [FOUNDATION-01](tasks/foundation/status.md).

Current authorized slice: bootstrap and local foundation, explicitly requested while Firebase/API inputs are unavailable. Namespace/application ID: com.aizzak.android (debug variant adds .debug).

- V1-LOCALE-01: English on first launch even on an Arabic device; explicit Arabic selection persists via AppCompat/platform locale APIs, RTL follows locale, input identifiers remain LTR. Do not override a restored explicit language.
- V1-THEME-01: follow system initially; persist System/Light/Dark using DataStore; read/write failures remain visible and retryable.
- V1-QUALITY-01..02: explicit loading/error states, no plaintext secret persistence/logging, touch/semantics/font/resize acceptance per approved UI design.
- V1-HOME-01..05: three descriptive unavailable agents; list-driven rendering and no agent action. Authenticated destinations cannot be reached through a production bypass.
- V1-AUTH-01..03 / V1-ACCOUNT-01..02: real behavior remains blocked until service inputs exist. Local forms/content do not prove registration, login, recovery, verification, context, password change or logout integration. Never fabricate an identity or permissions.

No new agent/workspace/conversation operations, remote admin catalog, release, push or publication are authorized by this local slice.
