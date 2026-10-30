# UI redesign

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- ui-ux-designer
- compose-developer
- android-qa-engineer

## Skills

- material3-design-system
- adaptive-ui-accessibility
- compose-ui-testing

## Steps

- Inspect current screen behavior and preserve product contracts.
- Specify state-by-state design, adaptive rules, tokens and accessibility.
- Implement the design in owned UI files and update semantic tests.
- Validate light/dark, RTL, large fonts and supported window sizes.

## Parallel

- Design and accessibility audit; screenshots and semantic tests after implementation.

## Artifacts

- screen specs
- UI source
- visual/semantics evidence

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
