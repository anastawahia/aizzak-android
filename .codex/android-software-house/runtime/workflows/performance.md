# Performance optimization

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- performance-engineer
- android-core-developer
- qa-engineer

## Skills

- performance-optimization

## Stages

- intake
- build
- verification
- acceptance

## Mandatory

- intake
- build
- verification
- acceptance

## Human Checkpoints

- acceptance

## Steps

- Define journey, budget and benchmark environment.
- Collect a baseline and locate a measured bottleneck.
- Apply a narrow optimization with regression coverage.
- Repeat comparable measurements; owner acceptance of the measured result.

## Parallel

- Profiles of separate subsystems under controlled conditions.

## Artifacts

- baseline
- profile
- patch
- comparison

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
