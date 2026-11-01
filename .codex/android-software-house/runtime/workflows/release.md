# Release preparation

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- build-release-engineer
- qa-engineer
- security-reviewer
- android-code-reviewer
- store-listing-specialist

## Skills

- gradle-build-management
- android-ci-cd
- android-security
- store-listing-aso
- privacy-data-safety

## Stages

- intake
- verification
- acceptance
- release-prep
- store-readiness

## Mandatory

- intake
- verification
- acceptance
- release-prep

## Human Checkpoints

- acceptance

## Steps

- Freeze artifact inputs and version; review current Play requirements.
- Required tests, lint and security checks on the frozen source; owner accepts the release candidate.
- Build and verify the signed AAB, hash and upload certificate.
- Store listing, assets from the accepted build, Data safety and content rating; no publishing.

## Parallel

- QA, independent review, listing work and security review of frozen source.

## Artifacts

- AAB
- hash/certificate
- release evidence
- store evidence

## Gates

- implementation
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
