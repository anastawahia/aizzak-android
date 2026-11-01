# Architecture refactoring

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- android-architect
- android-core-developer
- android-code-reviewer
- qa-engineer

## Skills

- android-clean-architecture
- dependency-injection
- unit-testing

## Stages

- intake
- architecture
- build
- verification
- acceptance

## Mandatory

- intake
- architecture
- build
- verification
- acceptance

## Human Checkpoints

- acceptance

## Steps

- Capture current behavior and dependencies before editing.
- Record the ADR and compiling migration slices.
- Move one boundary at a time while preserving public behavior.
- Regression checks, dependency review, owner acceptance.

## Parallel

- Read-only architecture review and regression inventory.

## Artifacts

- ADR
- module map
- incremental diffs
- regression evidence

## Gates

- architecture
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
