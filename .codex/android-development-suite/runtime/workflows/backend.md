# Backend integration

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- integration-engineer
- android-core-developer
- android-qa-engineer
- security-reviewer

## Skills

- api-integration
- local-persistence
- offline-first-sync

## Steps

- Read the real contract and auth/privacy requirements.
- Define DTO/domain/repository/error contracts and ownership.
- Implement client, storage and justified sync behavior.
- Run contract, failure, auth-expiry and migration tests; perform scoped security review.

## Parallel

- Contract test design and data implementation after interfaces; security review of auth design.

## Artifacts

- API contract notes
- data source
- fixtures
- test evidence

## Gates

- integration
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
