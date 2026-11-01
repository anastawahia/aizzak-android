# Idea discovery and requirements

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- product-manager
- business-analyst

## Skills

- product-discovery
- requirements-engineering

## Stages

- intake
- discovery
- requirements

## Mandatory

- intake
- discovery
- requirements

## Human Checkpoints

- discovery
- requirements

## Steps

- Interview the idea and research comparable apps from cited sources.
- Write the brief and present the discovery checkpoint.
- Write requirements and present the requirements checkpoint.

## Parallel

- Market research and constraint gathering.

## Artifacts

- product brief
- requirements

## Gates

- requirements

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
