# Release preparation

Supporting instruction resource, loaded explicitly by the coordinator. No automatic
workflow registration or background execution is implied. Partial execution is allowed.

## Preconditions

- Explicit project root and scope; repository instructions and Git changes inspected.
- Trusted wrapper required only for execution; existing user work preserved.

## Agents

- build-release-engineer
- android-qa-engineer
- security-reviewer
- android-code-reviewer

## Skills

- gradle-build-management
- android-ci-cd
- android-security

## Steps

- Freeze artifact inputs/version and review current Play requirements.
- Run required tests/lint/security checks; resolve critical findings.
- Build release AAB using approved signing provider; verify signature/hash and upload certificate.
- Complete listing/policy/testing/rollout checklist and prepare a concrete submission plan without publishing.

## Parallel

- QA, independent review, listing readiness and security review of frozen source.

## Artifacts

- AAB
- hash/certificate
- release evidence
- rollout plan

## Gates

- implementation
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
