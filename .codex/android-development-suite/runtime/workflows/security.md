# Security audit

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- security-reviewer

## Skills

- android-security

## Steps

- Inspect threat/data boundaries, manifest, auth, networking and secret handling.
- Review resolved dependencies/SBOM/licenses and approved scanner evidence.
- Report severity-ranked findings and verify targeted remediation without editing by default.

## Parallel

- Independent read-only manifest, dependency and auth reviews.

## Artifacts

- findings
- SBOM
- scan evidence
- retest status

## Gates

- findings-report

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
