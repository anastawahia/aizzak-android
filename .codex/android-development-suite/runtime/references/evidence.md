# Evidence, gates and project knowledge

All memory is ordinary repository-local files in `.android-suite/`. No extra
persistent agent memory service is assumed. `init-memory` creates missing notes
without overwriting existing content; use it only for authorized implementation.
Analyze, status and review-only commands never initialize or update memory.

Start with overview and progress; read other notes only for relevant work.
Use stable `REQ-001`, `TASK-001`, `ADR-001`, `TEST-001` and release version IDs.
Trace each acceptance criterion to implementation paths, tests and their execution
records. Keep history incremental, sanitized and reviewable. Do not store tokens,
keystores, service-account JSON or raw production telemetry in this directory.

## Machine-checked evidence

`suite.py verify` records command, attempt exit codes, sanitized output, timestamp,
source fingerprint and built artifact SHA-256. Save reports under the ignored-for-
fingerprinting `.android-suite/evidence/` so adding a report does not invalidate
its own source hash. Store public, sanitized evidence in Git when useful; keep
private scanner output outside shared project knowledge.

`suite.py gate --project <root> --stage <stage> --evidence <json>` requires every
check in `gates.json`, an exact current source fingerprint, and matching evidence
file hashes. It returns nonzero for missing, failed, skipped, stale or changed
evidence. It never infers test success from file existence.

Automated compile/static/test/release checks must reference a successful suite
execution JSON with the appropriate task kind, matching source and zero final
exit code. Test checks require actual nonzero JUnit counts and no failures,
errors or skips. Release builds require an existing hash-matched AAB. A manual
`passed` label cannot override a nonzero command result. Contract test coverage
still needs semantic review against the actual contract, not only test counts.

Build a gate record from actual reports, not an empty success stub. Its schema is:

```json
{
  "source_sha256": "SHA-256 returned by the successful verification run",
  "checks": {
    "compile": {
      "status": "passed",
      "evidence": [
        {"path": ".android-suite/evidence/build.json", "sha256": "SHA-256 of that actual report"}
      ]
    }
  }
}
```

This illustrates one check; an implementation record also needs the other checks
listed in `gates.json`. Compute report hashes with `Get-FileHash -Algorithm SHA256`
or `sha256sum`. Never edit a failed result to `passed`. Human checks reference a
dated review document naming the reviewer, scope and observed outcome. A gate
record is a provenance mechanism, not a cryptographic attestation or semantic
review engine: a local user can forge it. Claude must inspect the actual contents
and failed/skipped results, not trust a manually supplied label.

Required gates block automatic progression. A human-approved exception is recorded
with risk, owner, scope, reason and authorization reference in progress/release
notes. The CLI deliberately does not accept automatic gate bypasses. An exception
requires a manual reviewed path; it does not authorize publication implicitly.
Release approval is required even when earlier implementation was authorized.

## Bounded recovery

Capture the first actual failure and affected inputs. Classify host/toolchain,
transport, compiler, test, policy or permission failures. Apply the smallest safe
correction, rerun the failed check and any affected regression check, and record
the outcome. At most three targeted repairs per incident before escalation.
The CLI automatically retries only recognized transient transport failures, up
to the explicit `--attempts` bound. It never rewrites source or disables tests.

The source fingerprint excludes `.git`, build caches/outputs, project memory and
credential files. It covers ordinary project inputs, not changes in external
services or SDK contents; retain environment/version evidence separately.

## Human delivery checkpoints

Single-contact delivery also follows [the coordinator protocol](single-agent-delivery.md)
for requirements, design and PR review. These are separate user decisions; a
successful suite.py gate is not approval to continue past a pending human review.
The task record links reviewed document hashes, actual user decisions, accepted
specialist handoffs and the next action. Preserve these on resume and reopen
affected approvals after material changes. For a new nonexistent destination use
the protocol's pre-scaffold staging record until the renderer creates the project.

## Final handoff

Report scope and requirement IDs; changed/reviewed files; decisions; commands and
exit codes; passed/failed/blocked gates; real artifacts with hashes; infrastructure
limits; approved exceptions; and the next action. Distinguish source generation,
compilation, test execution, signing, submission and public serving.
