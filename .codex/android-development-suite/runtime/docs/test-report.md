# Execution report — 2026-10-30 (Asia/Baghdad)

This report separates implemented capabilities, structural/discovery checks,
executed tests and infrastructure-dependent work. It is not a claim of production
certification. [Machine-readable summary](verification.json).

This records the original Android/template validation. The 1.2.0 dual-host update
has separate [Codex/Claude compatibility evidence](compatibility.md), including
82 passing Python regression tests, native discovery on both hosts, and native
Codex specialist registration/invocation/handoffs in project and user scopes.

## 1.3.0 single-contact coordinator update

The new android-orchestrate entrypoint applies android-orchestrator in the main
session, routes scoped work, accepts specialist handoffs and pauses at applicable
human requirements/design/PR checkpoints. It records version-bound approval and
resume state, including pre-scaffold documents for a new destination.

On 2026-10-30, `python scripts/check_plugin.py --codex` passed structural validation,
all 83 Python tests, real isolated Codex discovery of 39 skills, and native
registration/invocation/handoffs for all 12 agents in project and user scopes.
The new skill also passed skill-creator's quick validator. See the
[compact execution summary](orchestration-verification.json).

An independent agent simulated new-app delivery, changed approved requirements
on resume, read-only inspection and a local typo fix. Identified instruction
issues were corrected and rechecked. This is instruction-consistency validation;
it does not prove model-powered end-to-end Android delivery or human checkpoint
enforcement. No Android build or external PR action was performed in this update.

## Implemented

- Installable plugin and local marketplace: 12 specialist agents, 24 reusable
  skills, 14 commands, 13 scoped workflows and repository-local knowledge.
- Portable inspection/diagnostics, non-overwriting app/feature/OpenAPI scaffolds,
  checksummed wrapper bootstrap, actual Gradle checks, bounded transport recovery,
  JUnit inspection, evidence gates, hook guards and deterministic packaging.
- Compose app/feature/API/state templates, documentation and CI templates.
- Signature auditing, disposable signing smoke test, and an optional real Play
  Developer API client guarded by release evidence and artifact-specific approval.

## Structurally validated and discovered

Claude Code **2.1.289** reported no errors/warnings for strict plugin manifest,
agent, skill, command and repository marketplace validation. The Python validator
checked metadata, catalogs, workflows, JSON and local references.

Actual loading through `--plugin-dir` discovered **12 agents**, **38 skill entries**
(24 reusable skills plus 14 commands), **one PreToolUse hook**, and no MCP/LSP.
The candidate ZIP loaded with the same inventory. The supported namespace is
`/android-development-suite:*`; no undocumented `/android:*` alias is registered.

## Executed successfully

| Check | Actual result |
|---|---|
| Python behavioral regression | **54 tests passed**, no failures |
| Python syntax | compileall passed for scripts, tools and tests |
| Wrapper | Official Gradle 8.13 JAR verified; distribution checksum pinned |
| Basic generated app | Debug build, unit tests and lint exited 0; one JVM test passed |
| Extended app | Debug APK and lint passed; **8 JVM tests passed**, none skipped |
| Feature module | Settings module compiled; **1 JVM test passed**, none skipped |
| API contract behavior | Real local HTTPS MockWebServer: success, 401, malformed JSON, 500 without unbounded retry |
| API error behavior | IO translation, cancellation propagation and safe diagnostic rendering passed |
| Kotlin analysis | Isolated Detekt task exited 0 |
| Device-test compilation | assembleDebugAndroidTest exited 0 and produced a test APK |
| Release mechanics | R8 release AAB build exited 0; jarsigner verified a disposable-key signature |
| Key cleanup | No disposable test keystore remained |
| Final template integrity | 27 source/resource/build files matched tested content, with line endings normalized |
| Packaging | Determinism, manifest placement, cache exclusion and overwrite refusal passed |

The nine extended Android JVM tests ran in actual Gradle/JUnit processes,
separately from the 54 Python tests. Local MockWebServer tests are not live backend
validation. Play protocol tests use mocked transport and prove no live publication.

## Failures found and repaired

- A regression test caught bearer text surviving a redaction pass. Masking order
  was corrected and quoted/environment-secret masking strengthened.
- The HTTPS fixture certificate covered localhost while its URL used 127.0.0.1.
  Added loopback IP SANs while preserving TLS hostname verification.
- Analysis found discarded network/serialization causes. Internal diagnostics
  are now retained while error string rendering exposes only safe kind/code.
- DTOs now receive matching filenames; support-file collisions are rejected.
- Preview visibility now reflects its IDE entry-point use.

Original failed reports remain in the ignored demo evidence directory. Assertions
and checks were preserved; no failure-hiding baseline or suppression was added
during recovery. Affected final build/test/lint/analysis checks passed.

## Retained warnings

Basic sample lint: **11 warnings**. Extended fixture lint: **24 warnings**.
Both had **zero errors**. Warnings concern newer available dependency/tool
versions, explicit backup extraction rules, and direct dependency declarations
in the extended test fixture. They were not suppressed. The app template itself
uses a version catalog. Review backup policy and current compatible dependencies
before a real product release.

## Not executed

- Device runtime, Compose interaction execution, accessibility/RTL/adaptive visual
  behavior and benchmarks: no connected device or configured AVD.
- Production signing/upload-certificate matching, current Play app/account
  eligibility, store declarations, rollout and live API submission: no production
  credentials or publication authorization. The local fixture AAB **does not pass
  the production release gate**.
- Live backend/auth and monitoring ingestion/deobfuscation: no project services.
- Hosted GitHub Actions and native Linux/WSL execution: this run used Windows.
- Authenticated Claude specialist behavior/evals and live hook event dispatch:
  CLI discovery was verified; model sessions were not launched.

## Known limits / incomplete automation

The automatic API renderer supports its documented JSON OpenAPI 3.0 subset;
advanced contracts need the integration skill. Store listing/forms, account
provisioning, resumable uploads above 512 MiB and automatic promotion/rollback
are not automated by the API client. Monitoring and optional stack integrations
are engineering workflows, not preconfigured live services.
See [limitations](limitations.md).

## Reproduce and locate evidence

From the repository root:

```powershell
python android-development-suite/scripts/check_plugin.py --claude
$env:GRADLE_USER_HOME = Join-Path (Get-Location) '.test-output/gradle-home'
python android-development-suite/scripts/demo.py --destination .test-output/fresh-demo --extended --execute
python android-development-suite/scripts/signing_smoke.py --project .test-output/fresh-demo --execute
```

Choose a new destination; scaffolding refuses overwrite. Raw records live in
`.test-output/plugin-checks.json`,
`.test-output/sample-app/.android-suite/evidence/` and
`.test-output/extended-demo/.android-suite/evidence/`. Module build directories
contain test XML and lint HTML. These are outside the distribution; a compact
summary is included with this report.

The local release fixture AAB SHA-256 is
`b7ed4e23d65d343dc607001a9077d6e0d6267c1c078afe6477e4cfd6ce2eedb4`.
Its private key was deleted; the artifact is for local verification only.
