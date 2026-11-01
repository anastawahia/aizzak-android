# Defect repair

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- android-core-developer
- compose-developer
- qa-engineer
- android-code-reviewer
- design-qa-reviewer

## Skills

- feature-planning
- unit-testing

## Stages

- intake
- build
- verification
- design-qa
- acceptance

## Mandatory

- intake
- build
- verification
- acceptance

## Human Checkpoints

- acceptance

## Steps

- Capture the actual failing behavior and environment.
- Write a failing regression case, then the smallest fix.
- Rerun failed/affected checks and independent review; design QA when a screen changed.
- Owner acceptance of the repaired behavior.

## Parallel

- Reproduction research and regression test design.

## Artifacts

- reproduction
- regression test
- fix
- execution evidence

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
