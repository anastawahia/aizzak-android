# Google Play publication

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- build-release-engineer

## Skills

- play-store-publishing

## Steps

- Verify current policies, eligibility and release gate for frozen source/artifact.
- Dry-run exact bundle/package/version/track/rollout and present it for human authorization.
- Record explicit approval reference and execute the approved upload/edit once.
- Inspect API result and Play Console serving/review status; retain submission IDs without claiming public availability prematurely.

## Parallel

- Read-only listing and policy readiness review before authorization.

## Artifacts

- approval reference
- API result
- Console verification
- release history

## Gates

- release

## Failure

- Capture actual error and affected REQ IDs.
- Apply at most three targeted corrections, rerunning failed/affected checks.
- Stop dependent stages when a required gate fails; report infrastructure blockers separately.

## Approval

- Existing authorization covers reversible in-scope edits and read-only checks.
- Explicit human approval required immediately before destructive or consequential external actions.
- Publishing approval binds package, artifact hash, version, track and rollout; never self-authorize.

## Report

- Scope/REQ IDs; completed/failed/blocked stages; changed paths.
- Exact commands, exit codes, environment and evidence paths.
- Gate results, approved exceptions, residual risks and next action.
