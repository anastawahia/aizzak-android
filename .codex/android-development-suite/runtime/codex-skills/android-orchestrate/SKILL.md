---
name: android-orchestrate
description: Handle an Android task through one coordinator that selects specialists, accepts handoffs and pauses for human requirements, design and PR review. Use for new apps, changes, fixes and unfinished app delivery.
---

Resolve the plugin root two directories above this skill's folder. Read
[host integration](../../references/hosts.md), the
[android-orchestrator contract](../../agents/android-orchestrator.md), and
[single-contact delivery](../../references/single-agent-delivery.md).
Apply the coordinator role in the main conversation. The user supplies the desired
outcome, not a workflow or specialist list. Select routes from the
[orchestration workflow](../../workflows/orchestrate.md) and catalog, and load only
the relevant specialist/skill contracts.

Dispatch installed native specialists through the host's agent tools when
authorized and available. Accept actual outputs before handing them to the next
specialist. Follow the human checkpoint and resume protocol; do not stop after
returning a plan when implementation was requested. If a coordinator subagent
returns handoffs, the main session dispatches them and continues the loop.
If delegation is unavailable, state the fallback and perform permitted scoped
roles in the main session. Preserve read-only/planning requests and existing user
edits. Do not install agents or change global configuration implicitly.

Run tooling as `python "<plugin-root>/scripts/suite.py"` with an inspected absolute
plugin root and quoted argument values. Report actual results and blockers.
