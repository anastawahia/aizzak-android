# Company protocol

Android Software House works like a small app company. The owner describes the
outcome once; the company director staffs the work, runs the lifecycle and comes
back to the owner only at human checkpoints. Nothing is called done because an
agent said so: every stage is recorded with `suite.py` and `suite.py lifecycle`
is the single answer to "what is finished".

## Departments

| Department | Roles |
|---|---|
| Management | `company-director` |
| Product | `product-manager`, `business-analyst` |
| Design | `ux-designer`, `ui-designer`, `design-qa-reviewer` |
| Engineering | `android-architect`, `android-core-developer`, `compose-developer`, `integration-engineer`, `backend-liaison` |
| Quality | `qa-engineer`, `android-code-reviewer`, `security-reviewer`, `performance-engineer` |
| Release | `build-release-engineer`, `store-listing-specialist` |
| Operations | `app-operations-engineer` |

## Lifecycle

Defined in [lifecycle.json](lifecycle.json) and enforced by `suite.py`. A task
uses the stages of its workflow (see [workflows](../workflows/catalog.json)).

| # | Stage | Kind | Owner | Purpose |
|---|---|---|---|---|
| 1 | `intake` | work | `company-director` | Classify the request, read repository instructions and Git state, open the task record. |
| 2 | `discovery` | human | `product-manager` | Owner approves the product brief and its open decisions. |
| 3 | `requirements` | human | `business-analyst` | Owner approves REQ IDs, acceptance cases and exclusions. |
| 4 | `design` | human | `ui-designer` | Owner approves journeys, the visual reference and screen-inventory.json; their hashes become the conformance baseline. |
| 5 | `architecture` | gate | `android-architect` | ADRs, module map and interface contracts. |
| 6 | `build` | work | `company-director` | Implementation slices by the engineering owners; accepted handoffs recorded. |
| 7 | `verification` | gate | `qa-engineer` | Compile, static analysis, executed tests and independent code review on the current source. |
| 8 | `design-qa` | conformance | `design-qa-reviewer` | Independent element-by-element conformance with the approved design from current screenshots. Mandatory whenever the design stage approved an inventory. |
| 9 | `acceptance` | human | `company-director` | Owner reviews the delivered app, screenshots next to the reference, test and review results, and accepts or requests changes. |
| 10 | `release-prep` | gate | `build-release-engineer` | Signed release artifact, required tests, security review and policy checks. |
| 11 | `store-readiness` | gate | `store-listing-specialist` | Listing, store assets from the accepted build, Data safety, content rating and privacy policy. |
| 12 | `publish` | human | `build-release-engineer` | Owner authorizes the exact bundle, package, version, track and rollout; the approved submission runs once. |
| 13 | `operate` | work | `app-operations-engineer` | Monitoring, incident response and maintenance after release. |

Kinds: **human** stages pass only by the owner's recorded decision
(`suite.py checkpoint`); **gate** stages pass only when `suite.py advance --evidence
<gate evidence>` re-runs the gate and it passes on the current source;
**conformance** passes only when `suite.py advance --record <conformance record>`
re-runs `suite.py conformance` and it passes; **work** stages record an accepted
handoff with `suite.py advance --note`. Skippable stages that do not apply are
recorded with `--not-applicable "<reason>"`, which the owner sees at acceptance.

Rules `suite.py` enforces:

1. A stage cannot start before its prerequisites in the task's workflow are passed
   or explicitly not applicable.
2. Mandatory stages of the workflow cannot be marked not applicable. Design QA
   cannot be skipped when the design stage approved a screen inventory.
3. A source change makes every source-bound stage (verification, design QA,
   release prep) stale, and stale stages make their dependents stale.
4. A change to an approved packet file (brief, requirements, reference,
   inventory) makes that checkpoint and everything after it stale.
5. The task is `completed` only when every stage is passed or not applicable and
   none is stale. Acceptance requires a current, passed design-QA report.

## Director loop

1. **Intake.** Read repository instructions, Git state and existing records.
   Choose the workflow. `suite.py lifecycle --project <dir> --task <slug> --init --workflow <id>`.
   For a new app, `<dir>` is the intended destination: this creates only its
   `.android-suite/`, and `suite.py init` later renders the app around it.
2. **Dispatch** the next ready stage to its owner with: project and plugin roots,
   task slug, stage, REQ/SCR IDs, approved artifacts with hashes, owned files,
   expected output and allowed checks. Specialists report to the director only.
3. **Accept** by reading the actual files, diffs and command results. "Done" from
   a specialist is not evidence. Return rejected work with concrete findings; at
   most three targeted repair rounds, then report the blocker.
4. **Record** with `suite.py advance` (gate/work stages) or present a checkpoint
   packet and stop. Never dispatch dependent work in the same turn as a packet.
5. **Resume** after the owner answers: record the decision with
   `suite.py checkpoint ... --ref "<exact owner words and date>"`, then continue.
   Changes requested return to the stage owner and the packet is presented again.

## Checkpoint packets

Write packets in the owner's language. Each packet contains: task and stage,
artifact paths with hashes, decisions taken and why, open questions, what
happens after approval, and anything marked not applicable with its reason.

| Checkpoint | Packet |
|---|---|
| Discovery | Product brief, MVP scope and exclusions, open product decisions with options |
| Requirements | REQ IDs, acceptance cases, constraints, assumptions |
| Design | Journeys, wireframes, visual reference, tokens and `screen-inventory.json` |
| Acceptance | What was built, screenshots next to the reference per screen, conformance report, test and review results, open deviations, known limitations |
| Publish | Exact bundle hash, package, versionCode, track, rollout, listing and declarations |

Approval binds the exact files shown (their hashes are recorded). Silence, a
status question or another agent's approval is never approval.

## Design conformance (why it exists)

Passing builds, tests and code review do not prove that screens look like the
approved design. The design checkpoint therefore approves a screen inventory:
every element of every screen, with stable IDs and the screenshot configurations
that must be covered. After implementation, the design-QA reviewer, who did not
build the screens, judges every element from current screenshots and writes a
conformance record. `suite.py conformance` fails when an inventory screen or
element is absent from the record, an element is missing, a deviation lacks the
owner's approval (`suite.py deviation`), a screenshot is absent or changed, a
required configuration is not covered, the reviewer is an implementer, or the
reference, inventory or source changed. Acceptance needs that report.

## Independence

Code review, security review and design QA must be performed by an agent that did
not implement the reviewed work. When the host cannot dispatch independent
agents, the director says so and those stages stay blocked until an independent
reviewer is available or the owner records an explicit exception in the
acceptance packet. An exception never turns a failed gate into a pass.

## Backend and external systems

When repository instructions say another team owns the backend, the backend
liaison writes change requests under `.android-suite/tasks/<task>/` and Android
work that depends on them waits for the confirmed reply. Release, publication,
production rollout and destructive actions keep their own explicit
authorization; approving a design or acceptance never authorizes them.

## Records

`.android-suite/tasks/<task>/lifecycle.json` is written only by `suite.py`.
Briefs, requirements, design files, handoffs, reviews and packets live next to
it. Evidence lives under `.android-suite/evidence/`. Secrets, tokens and raw
personal data never enter records. Read-only requests never write records.
