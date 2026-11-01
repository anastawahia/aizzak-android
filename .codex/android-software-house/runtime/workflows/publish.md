# Google Play publication

Supporting instruction resource loaded by the company director. Stages are enforced by `suite.py lifecycle`; see [the company protocol](../references/company-protocol.md).

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Open task lifecycle read when resuming; existing user work preserved.

## Agents

- company-director
- build-release-engineer
- store-listing-specialist

## Skills

- play-store-publishing
- store-listing-aso

## Stages

- intake
- release-prep
- store-readiness
- publish

## Mandatory

- intake
- release-prep
- store-readiness
- publish

## Human Checkpoints

- publish

## Steps

- Verify current policies, eligibility and the release gate for the frozen artifact.
- Dry-run the exact bundle/package/version/track/rollout; owner publish checkpoint.
- Execute the approved submission once and record its reference.
- Check Play Console review/serving status; never claim public availability early.

## Parallel

- Listing and policy readiness review before authorization.

## Artifacts

- approval reference
- API result
- Console verification

## Gates

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
