# Architecture refactoring

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- android-architect
- android-core-developer
- android-code-reviewer
- android-qa-engineer

## Skills

- android-clean-architecture
- dependency-injection
- unit-testing

## Steps

- Capture current behavior and dependencies before editing.
- Record ADR and compiling migration slices.
- Move one boundary at a time while preserving public behavior.
- Run regression checks and review dependency direction/cycles.

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
