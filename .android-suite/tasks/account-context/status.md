# ACCOUNT-CONTEXT-01 — Account services integration

Date: 2026-10-31 (Asia/Baghdad).
Stage: complete_local — Android-only correction verified; user confirmed live Home access and immediate re-login after logout.
Route: backend + scoped continuation of approved foundation auth/account/Home.

## Current handoff

Live acceptance received: user explicitly confirmed «وصلت للرئيسية ونجحت إعادة الدخول» after the final APK installation and USB connection. Local integration is complete for this scope. This is a human-verified real-account journey, distinct from isolated automated tests; no credentials or personal context payload were handled by the coordinator. See [acceptance](local-logout-acceptance.md). Earlier awaiting-user labels below are historical. No production delivery or backend modification is included.

### Final Android-only result after backend reply

Ordinary signout now uses local Firebase cleanup only (`remote=false`), with no `/me/logout` request. Context/identity cleanup and stale-result guards remain intact; the dormant HTTP adapter is retained per backend reply. One new immediate verified re-login regression and strengthened cleanup tests are included. Core and independent native review handoffs accepted; [local-logout-review.md](local-logout-review.md) records reviewed hashes. No backend file, setting, branch, container or deployment was changed in this continuation. Root [AGENTS.md](../../../AGENTS.md) permanently records the user's Markdown-only backend-request rule.

Current source fingerprint `d35135fd4664397e22a247b2fc44d58c40d2a03f34e8a1f8015945fcd6aec06f`: debug build, 55 JVM cases and Lint (0 errors/27 warnings) pass; 15 isolated Compose device cases pass with zero failures/errors/skips. Explicit retention/no-incompatible-uninstall options kept the target package installed after the tests. Final implementation and isolated integration gates pass with zero errors. Evidence prefix: `.android-suite/evidence/account-context-local-logout-`.

Final APK SHA-256 `2adc0213ba726e6acc61176f947be8dda0c791ee0dab67a27f48b2cd495940c8`; `adb install -r` succeeded, MainActivity cold launch status ok (1412 ms). Initial privacy-filtered UI observed sign-in. Host health/readiness returned 200; unauthenticated context returned 401; USB forwarding configured without starting/stopping backend services. User then confirmed actual Home access and immediate re-login without sharing credentials; see acceptance above. Earlier Vault blocker and modified-backend evidence below are historical and superseded by the backend owner's reply.

2026-10-31 backend owner replied in [backend-reply.md](backend-reply.md): Vault secret had expired and owner renewed it; previous backend changes were NOT approved, were retained in local commit `5835ca3` on `codex/account-context`, and were not merged. Running image is from master `4bc6417`. Treat all prior backend-change acceptance/evidence below as historical branch evidence, not the current deployed contract. Current Windows probes confirm `/health` and `/health/ready` each return 200; device is connected.

User now explicitly prohibits any backend modification from this project. Recorded durable instructions in root AGENTS.md: any needed backend change must be requested in Markdown for its developer. No backend file/config/branch/deployment change will be performed in this continuation. Backend reply directs ordinary Android signout to local Firebase cleanup only because master `/me/logout` subject-blocks every device and fresh login. This supplied decision supersedes the earlier presented-token logout implementation design for the current integration; no repeated approval question is required. Correct Android/tests/docs only, keep dormant HTTP adapter, and validate again.

## Documentation and commit authorization

User subsequently requested «قم بالتوثيق والالتزام». Android commit `5fb15cf` on `codex/android-account-context` contains the account-context implementation, local-only logout, regression tests, ownership instructions, backend handoff/reply and verification/acceptance evidence. Prior Firebase-settings task files and their shared-document additions were initially excluded. The user's following request «قم بالالتزام وتوثيق كل شيء» authorizes committing all remaining settings documentation and evidence in a follow-up documentation commit. No backend repository action, push or production delivery is included. Existing test results apply to the verified application source; documentation/index bookkeeping does not require another device run. The recorded source fingerprints describe the verified working tree, including pre-existing shared-document content, rather than claiming an identical final commit tree hash.

## Earlier handoff before backend reply (historical)

User requested an explanation for the backend developer and a summary of backend changes, rather than supplying runtime secret values. Prepared [Arabic backend handoff](backend-handoff.md), with the exact startup errors, confirmed versus suspected causes, affected files, change rationale and recovery acceptance. No message was sent externally.

Final Android source fingerprint: `470826ab677410a518606c0bf45ef936100103bb14c7bde77bf8f7d3cf03870a`. Debug build, 54 JVM cases, 15 isolated Compose device cases, lint (zero errors / 27 warnings), independent security and scoped correctness reviews passed. Final implementation and isolated integration evidence gates passed with zero errors. APK installed and MainActivity launched successfully; a privacy-filtered UI check observed sign-in. No real credentials were submitted or real-account context success claimed. See [verification](verification.md).

Backend focused 232 cases and static/architecture checks passed. Corrected image built; runtime verifier hash matches accepted source. On resumption, local API could not boot because Vault rejected AppRole credentials, including after current-config app recreation. App was stopped to avoid repeated failed logins; dependencies and volumes retained. No secret content or initialization volume was read. Current nginx is unhealthy without a ready app upstream. Production deployment, commit, push and PR remain outside this local task.

Resume after backend owner repairs AppRole and proves API readiness: recreate app, verify health/auth guard, re-establish USB forwarding, then test the real verified-account context/Home/logout journey. Historical design and intermediate execution records below are retained; their awaiting_human and pending stage labels are superseded by approval and these actual final results.

## Request and baseline

User reported successful login followed by account-services unavailable, asked for the next step, invoked android-orchestrate and supplied `api-documents` plus WSL Ubuntu-24.04 `/home/AIZZAK`.

Android HEAD `6bca34d3c7764a14c57c244466af3a5043c36033`. Existing modifications before this task: `.android-suite/progress.md`, `README.md`, untracked `.android-suite/evidence/firebase-settings-all.json` and `.android-suite/tasks/firebase-settings/`. Preserve them. Backend HEAD `9438cee`; unrelated changes in `docs/delivery/electrical-drawings/status.md` and untracked `docs/delivery/electrical-drawings/docs/lld-inc-1.md` are out of scope and preserved.

## Scope and inherited decisions

Implement V1-AUTH-02/03, the approved context-to-Home transition, identity-only Account, ordinary logout and the existing approved password-change journey when Account becomes reachable. Agents remain descriptive and unavailable; workspace creation/agent operations are deferred. No production deployment, real test-account creation, commit, push or publication is included.

Requirements: passed, inherited unchanged foundation approval dated 2026-10-30, exact user decision «اعتمد». Existing document hashes rechecked this turn:

- V1-AGREED-SCOPE.md: `C805DEA51CBA54C54DE636852EA1FF0948DE63DDFB45D4934F0179ACF640683C`.
- TECHNICAL-DESIGN.md: `6D186D62495917D269C64483991901290BBBBE58DC19182F2B16807A8E720D60`.
- UI-DESIGN.md: `1F74B0A82357A560443045864F89CC27685A58AF2A6B3999E0EC702F7B51E7AB`.

Existing architecture/UI approvals survive. New environment and server-logout policy decisions require scoped design review; they were explicitly unresolved in TECHNICAL-DESIGN.md section 9. Design: awaiting_human; packet [design.md](design.md), version 1.0, plus [contract-notes.md](contract-notes.md). User approval has NOT been received. Blocked transition: any implementation dependent on these decisions, service startup and device installation.

Reviewed packet SHA-256:

- design.md v1.0: `2F132B85442A6A4903B41072411D3D2AB53AC69BC16EC505F3EEA945152BFE51`.
- contract-notes.md v1.0: `722905F509D414A5739BA34DFC0BAC2978F14FAC98F69628F9C26305C41B28EB`.
- security-design-review.md: `905F399553707EAB1DF239231038A554B0DB00FBCC6568F5E21B905579814FD3`.

PR: not_applicable — local delivery requested. Production/server deployment and live user-credential testing require separate scope/authorization.

## Actual specialist handoffs

- `/root/api_contract` integration-engineer: accepted read-only OpenAPI analysis and typed interface/session proposal; required context fields and missing origin/logout semantics identified. No code or tests claimed.
- `/root/backend_evidence` android-architect: accepted read-only source/deployment/runtime inspection. Context shape matches docs; local stopped app uses matching public Firebase project setting. Existing logout blocks the Firebase subject, including fresh sign-in. Local containers stopped. See contract notes.
- `/root/auth_design_review` security-reviewer: accepted actual scoped design review, recorded in [security-design-review.md](security-design-review.md). Proposed presented-token digest revocation and exact-loopback debug transport are acceptable only within documented limitations and user approval. No implementation security audit, tests or full per-device session revocation claimed.

## Dependency and ownership plan

| Stage | Owner | Owned files/responsibility | Dependency | State |
|---|---|---|---|---|
| Research | integration-engineer / android-architect | API docs and backend read-only evidence | supplied paths | passed |
| Review packet | coordinator / security-reviewer | this task's documents / independent auth design review | accepted research | passed |
| Design decision | user | reviewed development environment and ordinary logout semantics | concrete packet | awaiting_human |
| Core session | android-core-developer | token/context contracts, FirebaseAuthRepository, AuthViewModel, approved password behavior and JVM tests | approved design; frozen interfaces | pending |
| Transport | integration-engineer | data/session client, DTOs, contract tests, dependency catalog/build file | shared interfaces | pending |
| Backend logout | integration-engineer or scoped backend worker | approved ordinary-logout change, design docs and focused tests; retain operator block | explicit policy approval | pending |
| Compose | compose-developer | navigation, context states, Account/password, resources, UI tests | stable core contracts | pending |
| Wiring | coordinator | AppContainer/MainActivity, dev build/network configuration, task evidence | accepted core/data/UI | pending |
| Verification | android-qa-engineer / build-release-engineer | isolated API fixtures, JVM, lint, build, available device UI; no live-service flushing tests | frozen source | pending |
| Independent review | security-reviewer / android-code-reviewer | read-only current changes and auth boundary review | actual implementation/checks | pending |

Shared-file writes must be serialized. All workers must preserve unrelated user work. No broad full backend live integration run: backend CLAUDE.md warns those tests can flush shared services; only isolated harness or authorized unit/architecture/eval scope.

## Commands and observations

- `suite.py route backend --small`: exit 0, scoped workflow selected.
- `suite.py init-memory --project <Android root>`: exit 0, created nothing (existing memory).
- `suite.py inspect --project <Android root>`: exit 0, read-only source inventory.
- `git status --short` in both projects: existing user changes captured; no source writes.
- WSL `docker ps` empty; `docker compose ps --all` shows stopped local stack. No service startup performed.
- WSL public health probes to documented `https://api.aizzak.com/health` and `/health/ready`: HTTP 000/curl exit 6, DNS failure. Windows `Resolve-DnsName api.aizzak.com`: DNS name does not exist. These establish lookup failure in this environment, not a successfully contacted or tested deployment.
- No Gradle, Android device install, real Firebase sign-in, authenticated context call or backend tests run during this design stage.

## Resume point

Independent design review accepted; present version 1.0 local-development + presented-token logout decisions to user. After approval, verify reviewed hashes, record exact user decision, freeze shared interfaces and dispatch implementation. Do not repeat unchanged foundation requirements/UI approval. No approval received as of this record.

## Approval and execution continuation

2026-10-31: user replied «قم بالربط» immediately after the version 1.0 packet and explicit approval question. This authorizes the shown local-development integration and presented-token logout change. Rechecked design SHA-256 `2F132B85442A6A4903B41072411D3D2AB53AC69BC16EC505F3EEA945152BFE51`; unchanged. Design checkpoint is now passed; earlier awaiting_human text records history. No production deployment or PR authorization inferred.

Coordinator froze shared contracts in core/network/IdTokenProvider.kt and feature/auth/AccountContextRepository.kt before developer dispatch. Backend implementation will use a new local codex/ branch from inspected master, preserving unrelated documents.

### Accepted implementation handoffs and current checks

- Android core and HTTP client authored and accepted for verification; Compose added protected Home/Account/Settings navigation, loading/denied errors and real password-change form. No UI/production bypass was introduced. All agent-written tests remain unclaimed until actual Gradle execution below.
- Backend master advanced externally to clean `4bc6417` before implementation; backend owner based local `codex/account-context` on that current state, preserving the now-merged user work. No commit/push.
- Local Compose startup completed; WSL and Windows `/health` and `/health/ready` each returned HTTP 200. Host unauthenticated `/api/v1/me/context` returned HTTP 401 and `application/problem+json`. ADB sees one authorized device and reverse `tcp:8080 -> tcp:80` is established. These are connectivity/auth-guard checks, not a real account-context success.
- Added `scripts/connect-local-api.ps1`; its `-SkipStackStart` path executed successfully against the healthy local stack and device. Debug network-security resource permits only `127.0.0.1`; main/release base configuration denies cleartext.
- Backend initial source passed 154 focused auth/revocation/Redis/API contract tests, Ruff format/check, strict mypy (490 files), nine import contracts. This result predates the security repair and is not the final accepted backend evidence.
- Independent security review found confirmed P1: raw-JWT digest allowed equivalent noncanonical signature encodings to bypass revocation. Reviewer reproduced `=` padding and unused signature-pad-bit variants using synthetic in-memory RS256 credentials. Returned to backend owner for production-verifier canonical compact-JWS validation and real cryptographic regression tests. Previous image build is NOT deployed.
- Security review also requested a focused late-reauth listener regression. Core owner is adding the publication guard and restore-and-notify tests; no success claimed yet.
- First suite all attempt failed at Gradle script compilation (`java.net.URI` package shadowed by Gradle DSL `java`): report `.android-suite/evidence/account-context-all-attempt1.json`, exit 1. Coordinator added explicit URI import and corrected missing Compose Text import. Older test XML/APK metadata in the failed report is historical and not current passing evidence. Build engineer paused until core repair freezes.

Current next action: finish both security-boundary regressions, rerun Android build/JVM/lint and Compose device tests, independently re-review fixes, rebuild/recreate only the local app container, and install/launch final debug APK preserving device data.
