# API contracts

Reference: [OpenAPI 3.1 contract](../api-documents/openapi.yaml), API version 1.0.0, provenance documented in [API README](../api-documents/README.md).

User explicitly reported Firebase Android configuration and API test URL unavailable. No API/Firebase integration is implemented or verified in this local slice. Resolve correct Firebase package/variant configuration, Email/Password policy, absolute HTTPS API origin, accepted issuer, and /me/context and /me/logout semantics before real integration.

Required future behavior follows approved technical design: verification then account context; never default permissions; refresh at most once for a safe read on 401; distinguish 403; clean local identity and account work on logout despite server failure; ignore late results from a prior account. No real tokens, credentials or personal response payloads belong in project evidence.
