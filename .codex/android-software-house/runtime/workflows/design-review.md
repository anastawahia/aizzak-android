# Design conformance audit of an existing app

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- ui-designer
- design-qa-reviewer

## Skills

- design-handoff-inventory
- screenshot-testing
- design-conformance-review

## Stages

- intake
- design
- design-qa

## Mandatory

- intake
- design
- design-qa

## Human Checkpoints

- design

## Steps

- Locate the approved reference; derive or reuse its screen inventory and have the owner confirm it.
- Record current screenshots for every inventory configuration.
- Independent element-by-element review and suite.py conformance report.
- Report matched, deviation and missing elements; route fixes to a ui-redesign or bugfix task.

## Parallel

- Screenshot capture for separate screens.

## Artifacts

- screen inventory
- screenshots
- conformance record and report

## Gates

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
