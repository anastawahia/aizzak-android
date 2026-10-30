# New feature development

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- product-analyst
- android-core-developer
- compose-developer
- android-qa-engineer
- android-code-reviewer

## Skills

- feature-planning
- state-management
- compose-ui-patterns
- unit-testing

## Steps

- Inspect existing patterns and changed files; link scope to REQ IDs.
- Agree state/interface contracts and file ownership.
- Implement bounded core/UI/data changes and regression coverage.
- Run relevant compile/test/lint checks and independent diff review; update traceability.

## Parallel

- Core and UI on separate files after contracts; QA test design while implementation proceeds.

## Artifacts

- plan
- source
- tests
- review
- traceability

## Gates

- requirements
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
