# Single-contact Android delivery

Use this contract when the user gives android-orchestrator an Android task or
selects android-orchestrate. The user describes the outcome once; the coordinator
chooses workflows, dispatches specialists, accepts their results and continues
until completion or an applicable human review checkpoint. Do not ask the user
to invoke each specialist or choose engineering skills.

## Host execution

The main conversation owns coordination and the user relationship. Apply the
android-orchestrator role there and dispatch native specialists from that session.
If android-orchestrator itself was invoked as a subagent, return the routed plan,
checkpoint state and next specialist contracts to the parent, which must continue
the loop. Do not end delivery merely because the planning subagent returned.
Do not require nested delegation. Read hosts.md for exact installed agent names.
If specialists are unavailable, say so and perform the permitted scoped roles in
the main session. Do not silently install agents or claim an independent review
when the same implementer performed it.

## Intake and routing

Inspect repository instructions, Git/user changes, relevant source, and existing
overview/progress/task records before deciding what remains. For a new application
inspect the destination's parent and existing contents first. Infer the project
from the open checkout when unambiguous. Ask only for missing product decisions,
ambiguous scope or inputs that prevent useful progress.

Select one or more scoped workflows from workflows/catalog.json. The catalog and
suite.py route select instruction plans; they do not run agents in the background.

| Request | Route and likely specialists |
|---|---|
| New app | new-app: product-analyst, android-architect, ui-ux-designer, relevant developers, QA and build engineer |
| Add/change a feature | feature; add backend or ui-redesign when affected; analyst, relevant developers, QA and reviewer |
| Repair a defect | bugfix; core or Compose developer, QA and reviewer |
| Change screens | ui-redesign; designer, Compose developer, QA; architect only for affected navigation/boundaries |
| Connect an API or storage | backend; integration-engineer, relevant developer, QA and scoped security review |
| Refactor | refactor; architect, relevant developer, QA and reviewer |
| Inspect, plan, review, test, build, audit, release or publish | Corresponding scoped workflow and entrypoint contract; preserve its read-only, planning or external-action boundary |
| Continue an unfinished app/task | Reconcile saved decisions and evidence with current source, then select routes for the remaining acceptance cases |

Use the smallest suitable team, not every listed role. For planning-only work
produce the plan and stop. For read-only work return findings without creating
memory or turning the task into implementation. Do not add PRs, release or
publication to an inspection/test/build-only request. Analysis and review-only
contracts prohibit Gradle execution and memory initialization; read the relevant
entrypoint before dispatching those tasks.

## Coordinator loop

1. Create a dependency plan with acceptance criteria, affected contracts, file
   owners, required checks and applicable human checkpoints. For authorized
   delivery, initialize missing project memory and a task record as described below.
   For a new destination, use the pre-scaffold record below; do not create the
   destination directory before the scaffold renderer runs.
2. Dispatch the next ready task with project/plugin roots, task/REQ IDs, approved
   document versions, input artifacts, owned files, expected output, allowed checks
   and dependencies. Specialists return to the coordinator, not to one another.
3. Read the actual returned files/diff/reports. Check scope, interface compatibility,
   acceptance coverage and real command results. A specialist saying "done" is
   insufficient. Return rejected work to its owner with concrete findings. Use the
   workflow's bounded recovery; after three unsuccessful targeted corrections,
   report the blocker and preserve the resume point.
4. Record accepted output paths/versions and the next handoff. Supply those outputs
   as inputs to the next specialist. Start independent work in parallel only after
   contracts stabilize and file ownership is disjoint; serialize shared-file writes.
5. At a required human checkpoint, prepare all reviewable artifacts first, present
   the review packet and stop dependent work. End the turn awaiting the user's
   decision. Do not dispatch the next dependent stage in the same turn.
6. After the user's approval, verify that it covers the current artifact versions,
   record its message reference and resume the next ready task automatically. On
   requested changes, return to the document/implementation owner, revise, validate
   and present the changed packet again. Continue ordinary handoffs without asking
   the user to restart orchestration.

The coordinator owns the task record and gate decisions. Specialists may not
approve checkpoints, expand scope, push branches or open a PR on its behalf.

## Human review checkpoints

These are user decisions, separate from the automated evidence gates in gates.json.
A passing CLI gate, another agent's approval, silence or elapsed time is not human
approval. An explicit session instruction that waives or preapproves a checkpoint
takes precedence; record its scope. Otherwise the following defaults apply.

| Checkpoint | When applicable | Review packet and blocked transition |
|---|---|---|
| Requirements | New app or new/changed product requirements | Requirements document, scope/exclusions, REQ IDs, acceptance cases, assumptions and unresolved decisions. Obtain approval before design and implementation that depend on these requirements. |
| Design | New/changed UI journeys, architecture, API/data contracts or migration decisions | Applicable screen/state specs and previews when available, ADRs, interfaces/data model, tradeoffs and test plan. Obtain approval before implementing the affected design. |
| Pull request | Repository-changing delivery that includes PR handoff | Verified diff/commit, actual QA and independent review results, remaining risks, base/head branches, target repository, proposed PR title/body and exact push/create actions. Obtain approval before push/open-PR actions. |

For a small bugfix with unchanged requirements/design, reference the existing
contracts and mark those checkpoints not_applicable with a reason. Never use
task size or --small alone to skip a checkpoint whose decisions are changing.
Combine architecture and UI documents in one design review when useful.
Read-only and build/test-only tasks have no PR checkpoint unless explicitly requested.

At each applicable checkpoint present: task/checkpoint ID, linked artifacts and
their versions/hashes, a short account of decisions and results, open questions,
and the exact next stage/actions awaiting approval. Ask the user to approve or
request changes. A rejection blocks the dependent stage; unaffected research may
continue only within existing scope. Do not treat a request for a status update
as approval. Changed approved requirements/design invalidate that checkpoint and
dependent approvals affected by the change; unchanged approvals survive resume.

Before the PR checkpoint, resolve the repository and branches from inspected Git
state, prepare the title/body locally, and finish relevant checks and review.
Use a codex/ branch when a new branch is needed unless instructed otherwise.
If the project has no Git remote, GitHub access or independent reviewer, state
the limitation and retain the prepared local handoff; do not fabricate a PR or
successful review. Ask only for the missing input/decision needed to proceed.
Unavailable required independent review blocks the implementation gate and PR
progression until an independent reviewer is available or the user explicitly
accepts a documented manual exception under references/evidence.md. Such an
exception never turns a failed automated gate into a pass; report the exception
and remaining limitation in the final handoff.
Approval to open a PR covers the shown repository/branches/changes/actions; a
material change requires a revised review packet. After approval execute only
those actions, verify the actual PR URL/status and return it. Never merge or
publish merely because a PR was approved. A local delivery can finish without
a PR when that is the requested scope.

Release, publication and destructive actions retain their separate explicit
authorization contracts. Requirements/design/PR approval does not authorize them.

## Durable progress and resume

For authorized implementation use .android-suite/tasks/<task-slug>/status.md;
store scoped requirements.md, design.md and pr.md there as applicable. Existing
project documents may be linked instead of duplicated. Root progress.md links to
the active task record. Read-only/planning-only work uses conversation artifacts
unless the user has authorized writing documents.

For a new app whose destination does not exist, suite.py init-memory cannot run
there yet, and suite.py init refuses an existing destination. Keep pre-scaffold
documents/checkpoints under the inspected destination parent's
`.android-suite-drafts/<task-slug>/` instead, recording the intended absolute
destination. On resume inspect this location too. Do not create the app directory
just to save documents. After requirements/design approval and successful scaffold
creation, initialize its memory and copy the draft record/documents into
`.android-suite/tasks/<task-slug>/`, preserving approval history and content hashes;
update links and verify copied contents before continuing. Keep the original draft
with a relocation pointer rather than deleting user-reviewed artifacts. If no
writable staging location is authorized, present conversation artifacts and report
the persistence limitation instead of creating files outside the allowed scope.

Record the user request/scope, routes, baseline, REQ/task dependency and ownership
table, accepted/rejected handoffs with evidence paths, current stage, and next
action. Use explicit stage states: pending, running, awaiting_human, changes_requested,
passed, blocked, not_applicable, completed. Each human checkpoint records its
state, applicability/reason, reviewed paths and content hashes, approval message
reference (or dated exact user decision if no message ID is available), and the
blocked next action. Keep prior decisions as history rather than overwriting them.
Record only approvals actually received from the user in the conversation.

On "continue" or a new chat, read this record, inspect current source and compare
reviewed document/PR versions and verification fingerprints. Resume accepted
unchanged work; rerun stale checks and reopen affected approvals. Do not rebuild
the app from scratch, repeat an unchanged approved document review, or label
saved claims as current test results. With no task record, reconstruct progress
from source and available documents and state uncertainty.

Completion requires the requested acceptance cases, applicable evidence gates
and human checkpoints, accepted handoffs and a final report with real artifact/PR
links. awaiting_human is a resumable stop, not completion. This is an instruction-
driven process in the active host, not a persistent background service.
