# Bug investigation and repair

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- android-core-developer
- android-qa-engineer
- android-code-reviewer

## Skills

- feature-planning
- unit-testing

## Steps

- Capture the actual failing behavior and environment.
- Trace the root cause and create a failing regression case.
- Apply the smallest correction and rerun the failed/affected checks.
- Review residual risk and record the repaired acceptance case.

## Parallel

- Read-only reproduction research and regression test design.

## Artifacts

- reproduction
- regression test
- fix
- execution evidence

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
