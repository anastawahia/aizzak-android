# Single-contact Android delivery

Supporting instruction resource, not a background workflow engine. Apply the
[single-contact coordinator protocol](../references/single-agent-delivery.md).

## Preconditions

- User outcome and inspected project/destination; infer missing routine choices.
- Repository instructions, Git changes and any prior task/checkpoint state read.

## Agents

- android-orchestrator owns routing, acceptance, checkpoints and consolidation in the main session.
- Select only relevant specialists from the scoped workflows; do not dispatch the full catalog.

## Skills

- requirements-engineering and feature-planning when relevant.
- Select engineering skills from each scoped workflow on demand.

## Steps

- Classify the request or reconcile unfinished work; select scoped workflows and acceptance criteria.
- Prepare applicable requirements and design artifacts and pause for human review before dependent implementation.
- Dispatch ready tasks, accept real results, return rework to its owner and pass stable outputs to the next specialist.
- Validate implementation and independent review; prepare and pause at the PR checkpoint when in scope.
- After explicit PR approval execute the shown push/open actions, verify the URL and report; otherwise finish the requested local scope.

## Parallel

- Independent research and disjoint-file tasks after stable contracts; never cross an unapproved dependent checkpoint.

## Artifacts

- Task status, scoped requirements/design documents, accepted handoffs, verification/review reports and PR packet when applicable.

## Gates

- Applicable scoped workflow evidence gates plus human requirements, design and PR checkpoints.

## Failure

- Stop dependent work on failed evidence, rejected handoff or pending human approval; record the resume point.
- At most three targeted repairs per incident before reporting a blocker.

## Approval

- Review packets are concrete before approval is requested; only the user's actual decision can approve a human checkpoint.
- Checkpoint applicability, artifact versions and approval references persist across turns; changed decisions reopen affected approvals.
- Preserve separate release/publication/destructive-action authorization and any explicit user overrides.

## Report

- Scope, routes, accepted handoffs, actual checks, human checkpoint decisions, artifact/PR links, limitations and next action.
