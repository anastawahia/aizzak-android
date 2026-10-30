# Existing application analysis

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- android-architect

## Skills

- android-clean-architecture

## Steps

- Run read-only inspection and Git status without executing Gradle or init-memory.
- Inspect catalogs, manifests, modules, data flow, tests and CI; distinguish observed evidence from inferred architecture.
- Return findings, constraints and recommended next steps without writing files.

## Parallel

- Independent read-only dependency and test inventory when needed.

## Artifacts

- analysis returned in conversation

## Gates

- findings-report

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
