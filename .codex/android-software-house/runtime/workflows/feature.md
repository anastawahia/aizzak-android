# Feature delivery

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- business-analyst
- ui-designer
- android-core-developer
- compose-developer
- qa-engineer
- android-code-reviewer
- design-qa-reviewer

## Skills

- feature-planning
- state-management
- compose-ui-patterns
- unit-testing
- screenshot-testing

## Stages

- intake
- requirements
- design
- architecture
- build
- verification
- design-qa
- acceptance

## Mandatory

- intake
- requirements
- build
- verification
- acceptance

## Human Checkpoints

- requirements
- design
- acceptance

## Steps

- Link scope to REQ IDs; owner approves changed requirements.
- For UI changes extend the reference and inventory; owner design checkpoint.
- Agree state/interface contracts and file ownership; implement core, data and UI slices.
- Verification gate, independent code review, design QA for UI changes, owner acceptance.

## Parallel

- Core and UI on separate files after contracts; QA test design while implementation proceeds.

## Artifacts

- requirements delta
- inventory delta
- source
- tests
- screenshots
- reviews

## Gates

- requirements
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
