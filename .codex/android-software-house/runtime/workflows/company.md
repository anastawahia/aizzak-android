# Android software house delivery

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director

## Skills

- requirements-engineering
- feature-planning

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
- build
- verification
- acceptance

## Human Checkpoints

- discovery
- requirements
- design
- acceptance
- publish

## Steps

- Classify the request and select the scoped workflow below; open its lifecycle.
- Run each stage with its owner, accept real outputs and record them with suite.py.
- Stop at every human checkpoint with a complete packet in the owner's language.
- Report only what `suite.py lifecycle` shows as passed; completion requires acceptance.

## Parallel

- Independent research and disjoint-file work after contracts stabilize; never across an open dependent checkpoint.

## Artifacts

- lifecycle record
- checkpoint packets
- accepted handoffs
- gate and conformance reports
- delivery report

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
