# Testing and regression validation

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- qa-engineer

## Skills

- unit-testing
- compose-ui-testing
- integration-e2e-testing
- screenshot-testing

## Stages

- intake
- verification

## Mandatory

- intake
- verification

## Human Checkpoints

- none (no lifecycle; results return in the conversation)

## Steps

- Map acceptance criteria and changed behavior to a test matrix.
- Add missing focused cases and inspect host/device availability.
- Run selected JVM, lint, compile, screenshot and device checks without treating skipped work as passed.
- Report failures, skips, coverage gaps and recovery actions.

## Parallel

- Independent JVM modules when Gradle resources permit.

## Artifacts

- test matrix
- reports
- environment evidence

## Gates

- implementation

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
