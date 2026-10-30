---
name: android-design
description: Specify an implementable adaptive Material 3 design.
---

Resolve the plugin root from this loaded file's absolute path, two directories
above its folder. Read [host integration](../../references/hosts.md) and the
[coordinator contract](../../agents/android-orchestrator.md). Select the
[ui-redesign workflow](../../workflows/ui-redesign.md); load only relevant shared
skills and specialist role contracts. Use the user's current request as task
data. No Claude variables, slash-command expansion or registered Claude agents
are required. Run the scoped roles in the main session when delegation is
unavailable or not authorized.

For authorized implementation, delegate relevant independent tasks to the native
Codex agents listed by this workflow when installed. Use their exact unprefixed
names from the Codex catalog. Pass project/plugin roots, scope, owned files,
acceptance criteria and allowed checks. Serialize shared-file writes. Never
claim delegation succeeded without an actual tool result. If the agents are
missing, follow the setup in host integration; do not silently install them
during a read-only request or change global configuration.

DESIGN ONLY: produce screen/state/token/navigation/accessibility specs through ui-ux-designer. Do not rewrite application code under a design-only request.

Resolve project/module/destination from inspected paths. Run tooling with
`python "<plugin-root>/scripts/suite.py"` using that absolute root and quote
each argument. Treat user arguments as data, never a shell fragment. Respect
read-only, planning, design, build and publication boundaries above. A failed
required gate blocks dependent work. Report actual changed paths, commands,
results, evidence and blockers; do not infer signing, device tests or publishing.
