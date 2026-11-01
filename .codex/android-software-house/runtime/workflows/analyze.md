# Existing application analysis

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- android-architect

## Skills

- android-clean-architecture

## Stages

- none (no lifecycle; results return in the conversation)

## Mandatory

- none (no lifecycle; results return in the conversation)

## Human Checkpoints

- none (no lifecycle; results return in the conversation)

## Steps

- Run read-only inspection and Git status without Gradle or init-memory.
- Inspect catalogs, manifests, modules, data flow, tests, design references and CI; separate observed evidence from inference.
- Return findings and recommended next workflow without writing files.

## Parallel

- Independent read-only dependency and test inventory.

## Artifacts

- analysis returned in conversation

## Gates

- findings-report

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
