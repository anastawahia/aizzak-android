# ACCOUNT-CONTEXT-01 — Android verification

Date: 2026-10-31 (Asia/Baghdad).
Owner: `/root/android_verification` (`build-release-engineer`).
Status: complete for local integration. Current Android local-only logout passed build/JVM/lint, device UI, independent review and scoped evidence gates; final APK installed/launched. User confirmed live Home access and successful re-login after logout. Earlier modified-backend outcomes below are historical branch evidence.

## Scope

### Android-only continuation after backend reply

Final human acceptance: user confirmed «وصلت للرئيسية ونجحت إعادة الدخول» following the request to test verified sign-in, logout and immediate re-login. This closes the pending real-account journey; it is user-reported runtime acceptance, not an automated credential test. [Acceptance record](local-logout-acceptance.md).

Backend owner reports expired Vault secret renewed, master `4bc6417` restored and unmerged branch changes not approved. Coordinator confirmed Windows HTTP 200 on `/health` and `/health/ready` plus HTTP 401 on unauthenticated `/api/v1/me/context`. No backend file, configuration, branch, container or deployment was modified in this continuation. User's ownership restriction is persisted in root AGENTS.md.

Core specialist changed ordinary signout to `remote=false` and authored meaningful zero-server-call/local cleanup/immediate re-login regressions. Independent native `/root/local_logout_review` passed with no actionable findings; [review](local-logout-review.md) records exact source hashes.

`account-context-local-logout-all.json`: actual suite `all` execution exit 0, 40.438 seconds. Source fingerprint `d35135fd4664397e22a247b2fc44d58c40d2a03f34e8a1f8015945fcd6aec06f`; debug build and all 55 JVM cases pass with zero failures/errors/skips. Lint passes. Current APK SHA-256: `2adc0213ba726e6acc61176f947be8dda0c791ee0dab67a27f48b2cd495940c8`.

Device tests use the actual Gradle wrapper with explicit `-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true` and `-Pandroid.experimental.testOptions.uninstallIncompatibleApks=false`; report captures the full command and actual outputs. The suite's standard verify command has no extra-argument option, so its trusted wrapper/run/fingerprint/report helpers are used directly without altering suite tooling. Compatible replacement installation is used; incompatible-package installation must fail rather than uninstall. No data-clear option or command is configured. Read-only explorer confirmed these options in cached official AGP8.13.2 source and UTP31.13.2 bytecode; source archive SHA-256 `0693AA79C9231FFD5917BE45CA8C5E25049BBA3D9503CE60ADA8D56C7B9ED1B4`. This is configuration evidence, not a guarantee of all live-session persistence.

`connect-local-api.ps1 -SkipStackStart -AdbPath D:/SDK/platform-tools/adb.exe` succeeded: health probes and USB forwarding only, with no backend service lifecycle action.

Current device result: `account-context-local-logout-ui.json` passed all 15 cases, zero failures/errors/skips, matching source fingerprint above. Report contains actual full wrapper command with the two preservation options. Checked `pm path` afterward: target remained installed. Final `adb install -r` succeeded and MainActivity cold launch returned status ok, 1412 ms. New privacy-filtered `account-context-local-logout-smoke.json` shows sign-in and no Home/unavailable/verification/denial marker; no personal UI strings were retained. Temporary device XML removed. The user must enter their own credentials to complete live acceptance; none were read or submitted by the coordinator.

Current implementation and isolated integration gates both passed with zero errors, using `local-logout-gate-evidence.json` and reports `account-context-local-logout-implementation-gate.json` / `account-context-local-logout-integration-gate.json`. This evidence excludes the rejected backend branch as a claim about current runtime. No backend code tests, file/config changes or lifecycle commands were executed in this continuation.

Debug build, JVM tests, Android Lint, instrumentation compilation and connected Compose UI tests. No release signing/publishing, manual credentials or final application launch. Coordinator owns final installation/runtime smoke verification.

## Execution record

1. `suite.py --report .android-suite/evidence/account-context-all-attempt1.json verify --project <Android root> --kind all --execute --timeout 900`.
   - Exit 1; 14.797 seconds.
   - Source SHA-256 `6f14a0841c21bccd038366ff6c087b5c9732f647b4c0760eafc8e621c9144060`.
   - Gradle Kotlin script compilation failed at `app/build.gradle.kts:74`: `java.net.URI` resolves through the Gradle `java` extension, producing `Unresolved reference: net`.
   - No current application compilation, tests or lint completed. The report also inventories pre-existing APKs and 25 old JUnit cases; those are stale outputs and are **not** success evidence for this implementation.
   - Coordinator notified; repair belongs to source owners. Verification agent changed no implementation files.

2. `suite.py --report .android-suite/evidence/account-context-all-attempt2.json verify --project <Android root> --kind all --execute --timeout 900` after coordinator refreeze.
   - Exit 1; 104.734 seconds.
   - Source SHA-256 `7402b38a999d1b6511e0941cd90addeb14f514917ffec334d1b5a079a2deb730`.
   - Debug assembly completed. JVM execution reported 42 cases, one failure: JUnit refused `HttpAccountContextRepositoryTest` because `oversizedAndWrongContentTypeAreRejected()` inferred a non-void return type from its expression-body coroutine.
   - The 41 other reported cases passed. HTTP contract scenarios in the rejected class did not execute. Final lint task did not complete; no full verification success is claimed.
   - Coordinator notified for the transport-test owner to correct the JUnit method shape; no production behavior change required for this test-registration failure.

## Pending

- Rerun affected configuration/build checks after coordinated fixes and source freeze.
- Run UI compilation and connected UI execution after required prior checks pass.
- Inspect debug/release network resource boundaries and URL-policy test evidence. Existing release signing validation must not be bypassed to obtain a manifest or artifact.

## Accepted final source checks

3. `account-context-all-attempt3.json`: exit 0, source fingerprint `966b22abe5e0dca898f3198a11b8db9dfb4c9fb03aa86baf7930db5943afa80d`. Debug assembly, all 54 JVM cases (zero failures/errors/skips) and Android Lint passed. XML inspection found zero errors and 27 warnings; dependency-update notices and platform compatibility notices remain advisory. The transport test owner corrected the JUnit coroutine return shape before this successful execution.

4. Coordinator ran `suite.py verify --kind ui-compile --execute --timeout 900`: exit 0, 98.922 seconds, same source fingerprint. Report: `account-context-ui-compile.json`. This compiles all instrumentation tests; it does not establish device execution.

Debug APK SHA-256: `0c2aedecac007f715789767cd58f8999c2d04900eb78e5bccb8a3751c81dd914`. Test APK SHA-256 after compilation: `961c0bda2f700d009e7b6358b373a3a64e25e5d27b50b2e432ccef82d17bef98`.

Main manifest retains `usesCleartextTraffic=false` and references the network-security XML. Main XML denies cleartext. Only debug XML permits exact `127.0.0.1`, without subdomains. Executed URL-policy JVM tests reject release HTTP, non-loopback debug HTTP, userinfo, query, fragment and the wrong API path. No release artifact was built and no signing validation was bypassed.

## Backend and resumed runtime

Final focused backend JUnit evidence contains 232 passing cases with zero failures/errors/skips, copied to `account-context-backend-unit.xml` (SHA-256 `FA71904F8E1C5FC66CA1E17589ACB3FE6844841D2A6CA617F312160C80ADAD5D`). Backend owner also completed Ruff format/check, mypy over 490 files, nine import contracts and diff checks. Broad live tests were not run against the shared services.

The corrected development image was built and local app container recreated. Runtime source hash for `/app/src/app/infrastructure/auth/firebase_auth.py` matches independently accepted `78731602e38677c302e6685f75c367e43d4b36b6809a5957ae1118d0a907a917`.

On resumption the stack had stopped. Named local dependencies were restarted and app recreated using current Compose configuration. App still fails during Vault AppRole login (`Forbidden` / invalid role or secret ID); health probes therefore fail. No secret values, `.env` content or Vault initialization volume were read. Human credential renewal was requested under backend `CLAUDE.md` Secrets instructions. Earlier HTTP 200 checks predate this stop and are historical, not current service readiness.

The phone was initially absent and is now connected and authorized. ADB forwarding `tcp:8080` to host `tcp:80` succeeded. Connected UI execution is in progress; authenticated context success is not yet established.

5. Connected UI attempt 1 executed all 15 cases; 14 failed (zero errors/skips). The failures searched for English strings while resource configuration was Arabic. Kept all assertions and introduced test-composition-only English resources using copied configuration and localized context; no app/device locale mutation. All three UI test classes now use this helper. Independent follow-up review passed; see correctness-review.md.

6. Reran `--kind all` for the test-only source change: `account-context-all-final.json`, exit 0, 62.813 seconds, current fingerprint `470826ab677410a518606c0bf45ef936100103bb14c7bde77bf8f7d3cf03870a`. Build/lint and the 54 JVM cases pass; JVM task is up-to-date because production and JVM sources are unchanged. Production APK hash is unchanged. Connected UI attempt 2 is running.

Stopped only the failing local app service while awaiting human AppRole renewal, avoiding repeated failed logins; dependencies remain running. No volume was removed.

7. Connected UI attempt 2 passed: `account-context-ui-attempt2.json`, exit 0, 103.437 seconds, 15/15 cases on Samsung SM-A155F / Android 15 (API 35), zero failures/errors/skips. Final test APK hash: `d709a7782425f0fc0086d6a7e6bc2dbd77392cde51aa7a7c0654041300e93b6e`. The same source fingerprint as step 6 was recorded before execution. All assertions remain intact; language fixture correction is independently accepted.

8. Initial evidence gates detected fingerprint drift from a transient generated `.kotlin/sessions/kotlin-compiler-*.salive` marker during test compilation. No product source changed. Kept failed gate reports. After compilation settled, reran all checks (`account-context-all-reconciled.json`, exit 0, 29.750 seconds, up-to-date outputs) and confirmed fingerprint returned to `470826ab677410a518606c0bf45ef936100103bb14c7bde77bf8f7d3cf03870a`. No cache was deleted and suite tooling was not altered. Final implementation/integration gates each passed with zero errors; reports have `-gate-final.json` suffix. Gates prove the scoped source/isolated evidence, not live service readiness.

9. After device tests, installed final debug APK using `adb install -r`: Success. `am start -W -n android.aizzak.com/.MainActivity`: Status ok, cold launch, 1509 ms total. Process/package existence checked. Privacy-filtered UI XML matched sign-in; no user text or credentials were retained. Temporary device XML removed. This establishes startup only. The test runner had left the package uninstalled before final installation; session/data persistence across its cleanup was not verified and must not be claimed.

User requested a backend-developer handoff for the remaining issue. Full Arabic explanation and scoped change summary: [backend-handoff.md](backend-handoff.md). No external communication was sent.
