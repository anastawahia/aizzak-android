# Backend integration

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- integration-engineer
- backend-liaison
- android-core-developer
- qa-engineer
- security-reviewer
- android-code-reviewer

## Skills

- api-integration
- backend-change-requests
- local-persistence
- offline-first-sync

## Stages

- intake
- requirements
- architecture
- build
- verification
- acceptance

## Mandatory

- intake
- build
- verification
- acceptance

## Human Checkpoints

- requirements
- acceptance

## Steps

- Read the real contract, ownership rules and auth/privacy requirements.
- Write backend change requests for anything the backend must change; block only dependent work.
- Implement client, storage and justified sync behavior against confirmed contracts.
- Contract, failure, auth-expiry and migration tests; scoped security review; owner acceptance.

## Parallel

- Contract test design and data implementation after interfaces; security review of auth design.

## Artifacts

- contract notes
- backend change requests
- data source
- fixtures
- test evidence

## Gates

- integration
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
