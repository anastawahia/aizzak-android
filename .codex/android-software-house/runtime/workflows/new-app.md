# New app from idea to Google Play

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- product-manager
- business-analyst
- ux-designer
- ui-designer
- android-architect
- build-release-engineer
- android-core-developer
- compose-developer
- integration-engineer
- qa-engineer
- android-code-reviewer
- design-qa-reviewer
- security-reviewer
- store-listing-specialist

## Skills

- product-discovery
- requirements-engineering
- ux-journeys-wireframes
- design-handoff-inventory
- android-project-bootstrap
- compose-ui-patterns
- screenshot-testing
- design-conformance-review

## Stages

- intake
- discovery
- requirements
- design
- architecture
- build
- verification
- design-qa
- acceptance
- release-prep
- store-readiness
- publish
- operate

## Mandatory

- intake
- discovery
- requirements
- design
- architecture
- build
- verification
- design-qa
- acceptance

## Human Checkpoints

- discovery
- requirements
- design
- acceptance
- publish

## Steps

- Discovery: brief, MVP and open decisions; owner checkpoint.
- Requirements with REQ IDs and acceptance cases; owner checkpoint.
- Journeys, wireframes, visual reference and screen inventory; owner design checkpoint binds their hashes.
- Architecture ADRs, then scaffold with suite.py init and bootstrap the wrapper.
- Build screen by screen from the inventory with screenshot tests; core and data in parallel on disjoint files.
- Verification gate, then independent design QA from screenshots, then owner acceptance.
- If the owner wants to ship: release prep, store readiness, publish checkpoint, then operations.

## Parallel

- Competitor research with environment inspection; core/data and UI implementation on separate files after contracts.

## Artifacts

- product brief
- requirements
- design reference and screen inventory
- ADRs
- source and tests
- screenshots and conformance record
- release and store evidence

## Gates

- requirements
- architecture
- implementation
- design_conformance
- release
- store

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
