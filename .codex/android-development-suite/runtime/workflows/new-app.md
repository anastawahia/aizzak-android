# New application development

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- product-analyst
- android-architect
- ui-ux-designer
- compose-developer
- android-core-developer
- android-qa-engineer
- build-release-engineer

## Skills

- requirements-engineering
- android-project-bootstrap
- compose-ui-patterns
- unit-testing

## Steps

- Inspect destination/host and define app identity, supported devices and essential acceptance cases.
- Confirm architecture and implementable screen specs.
- Render a new project, initialize memory and bootstrap wrapper.
- Implement initial journey and tests; run compile/unit/lint and available device checks.

## Parallel

- Requirements research and environment inspection; design and test planning after requirements stabilize.

## Artifacts

- PRD
- ADR
- source
- test reports
- knowledge

## Gates

- requirements
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
