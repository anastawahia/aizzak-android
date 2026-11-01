# Post-release incident handling

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- app-operations-engineer
- android-core-developer
- integration-engineer
- qa-engineer
- build-release-engineer

## Skills

- crash-monitoring-maintenance
- feature-planning
- unit-testing

## Stages

- intake
- build
- verification
- acceptance
- release-prep
- publish

## Mandatory

- intake
- build
- verification
- acceptance

## Human Checkpoints

- acceptance
- publish

## Steps

- Triage impact and affected versions with sanitized telemetry.
- Reproduce; propose halt, forward fix or rollback within version/data constraints.
- Implement and test the smallest remediation; owner acceptance.
- Owner approval before any production rollout change; verify recovery and record the incident.

## Parallel

- Telemetry and backend investigation; regression test design.

## Artifacts

- incident timeline
- reproduction
- fix
- health evidence

## Gates

- implementation
- release

## Failure

- Capture the actual error and affected REQ/SCR IDs.
- Apply at most three targeted corrections, rerunning failed/affected checks.
- Stop dependent stages when a required gate fails; report infrastructure blockers separately.

## Approval

- Human checkpoints pass only by the owner's recorded decision bound to the packet hashes.
- Explicit owner approval immediately before destructive or consequential external actions.
- Publishing approval binds package, artifact hash, version, track and rollout; never self-authorize.

## Report

- Scope, REQ/SCR IDs and the `suite.py lifecycle` result.
- Exact commands, exit codes, environment and evidence paths.
- Gate and conformance results, approved deviations/exceptions, residual risks and next action.
