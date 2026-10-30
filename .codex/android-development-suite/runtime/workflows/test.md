# Testing and regression validation

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- android-qa-engineer

## Skills

- unit-testing
- compose-ui-testing
- integration-e2e-testing

## Steps

- Map acceptance criteria and changed behavior to a test matrix.
- Create missing focused cases and inspect host/device availability.
- Run selected JVM, lint, compile and device checks without treating skipped work as passed.
- Report failures/skips/coverage gaps and precise recovery actions.

## Parallel

- Independent JVM modules may run separately when Gradle/resources permit.

## Artifacts

- test matrix
- XML/HTML reports
- environment evidence

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
