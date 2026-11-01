# Screen design or redesign

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- ux-designer
- ui-designer
- compose-developer
- qa-engineer
- design-qa-reviewer

## Skills

- ux-journeys-wireframes
- material3-design-system
- design-handoff-inventory
- compose-ui-patterns
- screenshot-testing
- design-conformance-review
- adaptive-ui-accessibility

## Stages

- intake
- design
- build
- verification
- design-qa
- acceptance

## Mandatory

- intake
- design
- build
- verification
- design-qa
- acceptance

## Human Checkpoints

- design
- acceptance

## Steps

- Inspect current behavior and preserve product contracts.
- Wireframes, visual reference and inventory; owner design checkpoint.
- Implement every inventory element with screenshot tests.
- Verification, independent design QA from screenshots, owner acceptance.

## Parallel

- Accessibility audit with design work; screenshots and semantic tests after implementation.

## Artifacts

- wireframes
- design reference
- screen inventory
- UI source
- screenshots
- conformance record

## Gates

- implementation
- design_conformance

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
