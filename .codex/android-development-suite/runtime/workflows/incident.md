# Post-release incident handling

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- android-orchestrator
- android-core-developer
- integration-engineer
- android-qa-engineer
- build-release-engineer

## Skills

- crash-monitoring-maintenance
- feature-planning
- unit-testing

## Steps

- Triage impact and affected versions with sanitized telemetry.
- Reproduce and choose halt/forward-fix/rollback proposal based on version/data constraints.
- Implement/test the smallest remediation and prepare the signed artifact.
- Obtain approval before changing production rollout; verify serving/health recovery and record the incident.

## Parallel

- Read-only telemetry and backend contract investigation; regression test design.

## Artifacts

- incident timeline
- reproduction
- fix
- health evidence

## Gates

- implementation
- release

## Failure

- Capture actual error and affected REQ IDs.
- Apply at most three targeted corrections, rerunning failed/affected checks.
- Stop dependent stages when a required gate fails; report infrastructure blockers separately.

## Approval

- Existing authorization covers reversible in-scope edits and read-only checks.
- Explicit human approval required immediately before destructive or consequential external actions.
- Publishing approval binds package, artifact hash, version, track and rollout; never self-authorize.

## Report

- Scope/REQ IDs; completed/failed/blocked stages; changed paths.
- Exact commands, exit codes, environment and evidence paths.
- Gate results, approved exceptions, residual risks and next action.
