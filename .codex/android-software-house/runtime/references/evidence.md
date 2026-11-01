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

## Lifecycle, checkpoints and design conformance

Delivery follows [the company protocol](company-protocol.md) and
[lifecycle.json](lifecycle.json). `suite.py lifecycle/checkpoint/advance/deviation`
are the only writers of `.android-suite/tasks/<task>/lifecycle.json`. Human
checkpoints bind the owner's recorded decision to the packet file hashes; gate
stages re-run `suite.py gate` on the supplied evidence; design QA re-runs
`suite.py conformance` on the reviewer's record. Reports are saved under the task's
`reports/` folder and hashed into the lifecycle, so a changed report, packet or
source makes the stage stale. A passing gate is never approval to cross a pending
human checkpoint.

`suite.py conformance` verifies completeness against the approved inventory,
owner-approved deviations, screenshot hashes and configuration coverage,
reviewer independence and freshness. It does not measure visual similarity; the
element judgments are the independent reviewer's.

For a new app, open the lifecycle in the intended destination before anything
else (`suite.py lifecycle --project <destination> --task <slug> --init --workflow new-app`).
That creates only `<destination>/.android-suite/`; keep discovery, requirements
and design documents there. `suite.py init` later adopts a destination that
contains nothing but `.android-suite/` and renders the app around it.

## Final handoff

Report scope and requirement IDs; changed/reviewed files; decisions; commands and
exit codes; passed/failed/blocked gates; real artifacts with hashes; infrastructure
limits; approved exceptions; and the next action. Distinguish source generation,
compilation, test execution, signing, submission and public serving.
