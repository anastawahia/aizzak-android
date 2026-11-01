# Known limits and outstanding verification

## Enforcement is provenance, not proof

- `suite.py lifecycle/checkpoint/advance/deviation` make it impossible for an
  agent following the contracts to report a stage, design match or completion
  that the recorded evidence does not support. They are not tamper-proof: a
  local user, or an agent ignoring its contract, can edit records by hand.
  Human decisions are recorded as the director reports them; the owner should
  read the packet files listed in the record.
- `suite.py conformance` checks completeness against the approved inventory,
  approved deviations, screenshot hashes and configuration coverage,
  reviewer independence claims and freshness. It does not measure visual
  similarity. Element judgments are the design-QA reviewer's; a careless reviewer
  can still mark a missing element as matched. The owner's acceptance checkpoint,
  with screenshots shown next to the reference, is the final safeguard.
- Reviewer independence is a recorded claim (role and agent/session IDs). When a
  host cannot dispatch separate agents, independent stages stay blocked unless the
  owner records an explicit exception.

## Hosts and agents

- Instructions do not guarantee model quality. Native Codex dispatch and handoffs
  are checked with a localhost protocol fixture, not with real-model Android work.
- Codex native roles require `scripts/install_codex_agents.py` and a new chat in a
  trusted project; reinstall after upgrades or project moves. 1.x installations
  need `--migrate-legacy` once.
- Codex hooks require host trust; `ask` is unsupported in Codex PreToolUse, so
  reviewable operations return `deny`. Hooks and log masking are heuristics, not
  complete shell parsing or secret scanning.
- Workflows are instruction resources plus `suite.py` state; there is no
  background scheduler or guaranteed parallel agent availability.

## Android tooling

- Template versions form a reproducible API 36 baseline (see
  [versions.json](../references/versions.json)), not current universal Play
  compliance. Existing apps need their own compatibility review.
- Roborazzi screenshots render Compose on the JVM through Robolectric. They are
  layout evidence, not device rendering; device-specific issues need device checks.
  Robolectric downloads its Android runtime jars on first use.
- The feature renderer creates an independently testable UI module; integration
  into navigation and settings remains reviewed work.
- OpenAPI generation supports JSON OpenAPI 3.0, local object DTOs and
  parameterless GETs only.
- The optional Detekt CLI lacks Kotlin 2.3 type resolution.
- `publish_play.py` is a limited Android Publisher v3 edit client: no account or
  listing provisioning, no policy declaration submission, no resumable uploads
  above 512 MiB, no automatic promotion or rollback.
- Store listing, Data safety and privacy policy drafts are not legal clearance.

See [test report](test-report.md) for the checks executed on this version.
