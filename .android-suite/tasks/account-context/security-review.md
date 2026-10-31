# ACCOUNT-CONTEXT-01 — Independent security implementation review

Date: 2026-10-31. Reviewer: `/root/auth_design_review`, native `security-reviewer`.
Final source-review result: passed, after correction of one confirmed P1 and a focused late-reauthentication publication concern. This records the actual independent reports; the coordinator wrote this file. Build/runtime/device outcomes are separate.

## Findings and accepted corrections

P1: hashing raw JWT text allowed equivalent signature encodings to escape the presented-token denylist. Reviewer reproduced both trailing `=` and unused pad-bit variants using synthetic in-memory RS256 credentials; both cryptographically verified with PyJWT but changed the raw SHA-256 digest. Returned to backend owner before runtime deployment.

Corrected production FirebaseAuth verifies signature/claims then requires all three compact JWT segments to be unpadded, valid base64url and exact decode/re-encode round trips. Actual production-verifier regressions cover padding and unused bits, plus a warmed-principal revocation bypass. Independent re-review closed P1. No real credentials were used.

Late reauthentication concern: cancellation/signout could allow an SDK restore-and-notify callback before stale cleanup. Core owner added a UID/generation publication barrier, preserving normal READY and preserving current identity for cancellation of the password operation alone. Restore-plus-listener repository and repository/ViewModel regressions establish no post-logout identity/context publication. Independent re-review accepted the correction; actual execution belongs to the final JVM evidence.

## Reviewed boundaries

- Administrative subject blocking preserved and checked before principal-cache access, alongside the separate token denylist. Retained cache, bounded token expiry and fail-closed errors retained.
- Token UID/generation checks and forced-refresh serialization; context epochs; immediate visible logout cleanup followed by bounded server attempt and guaranteed local Firebase cleanup.
- No redirects, cookies, account HTTP cache, connection retries, bearer/body logging or TLS bypass. DTO/UUID validation and decompressed response bounds precede content acceptance.
- Protected navigation requires READY, verified identity and nonnull context; UID/readiness dispose old account state. Password values remain unsaved memory and clear on lifecycle stop/disposal.
- Debug HTTP only for exact 127.0.0.1. Release runtime/packaging validation requires HTTPS; main network configuration denies cleartext.

## Accepted source hashes

| Source | SHA-256 |
|---|---|
| Backend infrastructure/auth/firebase_auth.py | 78731602e38677c302e6685f75c367e43d4b36b6809a5957ae1118d0a907a917 |
| Backend framework/auth/revocation.py | 190238464f56a0e8b958a4dd82104813aeeb103d4c41b3c5b8f7076b03c3ee73 |
| Backend api/middleware/auth.py | 5afa670ef11ecba1405c0acfd41380aedc7e54591a205d8c20f35e9f3fe7f372 |
| Backend tests/unit/test_firebase_auth.py | b9e8f2033671a52aa933ca1f156ed079cf5daa5dc6d14b94947f5669d24d28e5 |
| Android data/auth/FirebaseAuthRepository.kt | 1402bc6edd4a29832e6ae54cad8e62a55fc33e9dadb75fce2a691cf5500a5f8e |
| Android feature/auth/AuthViewModel.kt | a16529936d5440aee6dc12e70e3a22fa530dc0ac80a0e498e814757e00d19f58 |
| Android data/session/HttpAccountContextRepository.kt | 98f3086e0810f62061a9fdeb8b08e14e237971476ff18f2ba42517d6c3fa8a71 |
| Android navigation/LocalFoundationApp.kt | 4adbc51da4595a7c975611a11f6e8374173792ac19ab1ed8e390929dfe7dfa82 |
| Android feature/account/PasswordScreen.kt | 8870861d3514301cbef213a23d55754620bba4087e4c4e13edebe70b5f9d5d15 |
| Android app/build.gradle.kts | dbc98d9feb474f53006a434ee63a932ba839fc9195cfed5962c02a7e3f5bcdb2 |

## Limits

No dependency vulnerability/license scan, comprehensive audit, production deployment or merged release artifact was verified by this reviewer. Presented-token logout does not revoke refresh-token families, every device token or established sockets. Both independent reports were read-only; synthetic cryptographic reproduction is distinct from live authentication.
