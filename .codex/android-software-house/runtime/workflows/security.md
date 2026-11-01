# Security and privacy audit

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- security-reviewer

## Skills

- android-security
- privacy-data-safety

## Stages

- none (no lifecycle; results return in the conversation)

## Mandatory

- none (no lifecycle; results return in the conversation)

## Human Checkpoints

- none (no lifecycle; results return in the conversation)

## Steps

- Inspect threat/data boundaries, manifest, auth, networking and secret handling.
- Review dependencies, SBOM, licenses and data declarations.
- Report severity-ranked findings without editing by default.

## Parallel

- Independent manifest, dependency and auth reviews.

## Artifacts

- findings
- SBOM
- scan evidence

## Gates

- integration

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
