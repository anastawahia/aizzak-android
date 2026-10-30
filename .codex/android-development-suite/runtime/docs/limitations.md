# Known limits and outstanding verification

- Instructions and deterministic routing do not guarantee autonomous agent
  quality. Native protocol fixtures verify dispatch and handoffs; authenticated
  real-model sessions are still needed to assess end-to-end specialist work.
- Workflow resources are explicitly loaded plans. There is no invented Claude
  workflow schema, scheduler or guarantee of nested/parallel agent availability.
- Codex discovers 38 skills through its compatibility manifest. Twelve native
  specialist agents require the explicit project/user installer and a new chat
  in a trusted project. Native registration, exact child instructions, spawning
  and completed handoffs are tested with the real CLI and a localhost fixture.
  Real-model Android task completion is not tested by that protocol fixture.
  Disabled agents or unavailable delegation require an explicitly reported main
  session fallback. Reinstall after upgrading the plugin or moving the target
  project. See [host compatibility](compatibility.md).
- Codex hooks require host trust and use a separate adapter. `ask` is unsupported
  in Codex PreToolUse, so reviewable operations return `deny`. The probe checks
  hook discovery and tests payload handling; it does not grant trust or prove a
  hook executed during an authenticated model session.
- The canonical plugin name produces `/android-development-suite:*`; `/android:*`
  aliases are not registered. Twenty-four skills plus fourteen command entry
  points appear as thirty-eight skills in Claude's component inventory.
- Python 3.10+ and a `python` command are required for the hook. Linux/WSL logic is
  portable but native Linux/WSL execution needs separate environment evidence.
- Template versions form a reproducible API 36 baseline, not current universal
  Play compliance. Existing apps need independent compatibility inspection.
  Device-specific Wear/TV/Auto/native/KMP/AGP 9 configurations need adaptation.
- The feature renderer creates an independently testable UI module; navigation
  and host settings integration remain explicit, reviewed implementation work.
- Automated OpenAPI generation supports JSON OpenAPI 3.0, local object DTOs and
  parameterless GETs only. YAML, OpenAPI 3.1, unions/maps/parameterized endpoints
  need manual implementation through the integration skill. Generated repositories
  still need product-specific domain mappings, auth wiring and behavior tests.
- Hooks and log masking are best-effort heuristics. They do not scan all files,
  Git history, encoded secrets or every shell/MCP mutation path. Use dedicated
  secret, vulnerability and license scanning for release assurance.
- Source/evidence hashes establish identity, not truthful results. Local approval
  records are not tamper-proof human attestations. Session authorization and
  semantic review are still required; sensitive inputs are intentionally excluded
  from source hashes and need environment/certificate verification.
- The optional isolated Detekt CLI does not have Kotlin 2.3 type resolution and
  may not parse future syntax. Validate it against actual project syntax before
  treating it as a blocking compiler-aware analyzer.
- Google Play integration is a real but limited v3 edit client. It does not
  provision accounts/apps/listings, submit policy declarations, use service-account
  private keys directly, support resumable uploads above 512 MiB, or implement
  automatic promotion/rollback. OAuth provisioning belongs to the approved provider.
- No live Play mutation, production credential use, production rollout or monitoring-provider
  installation was authorized or performed. CI templates are not claims of hosted
  workflow execution. No connected device means UI runtime/accessibility/performance
  checks remain outstanding unless separately documented in the test report.
  Local release signing used a disposable fixture key that was deleted afterward;
  it provides no Play upload-certificate evidence.

See [test report](test-report.md) for the exact checks executed in this checkout.
