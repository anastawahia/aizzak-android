# Performance optimization

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- performance-engineer
- android-core-developer
- android-qa-engineer

## Skills

- performance-optimization

## Steps

- Define journey, budget and representative benchmark environment.
- Collect baseline and locate a measured bottleneck.
- Apply narrow optimization with behavior regression coverage.
- Repeat comparable measurements and report uncertainty and remaining budget.

## Parallel

- Read-only profiles of separate subsystems if measurement conditions remain controlled.

## Artifacts

- baseline
- profile
- patch
- comparison

## Gates

- implementation

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
