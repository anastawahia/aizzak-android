---
name: android-company
description: Hand any Android task to the software house: idea to Google Play, changes, fixes or unfinished work.
---

Resolve the plugin root from this loaded file's absolute path, two directories
above its folder. Read [host integration](../../references/hosts.md), the
[company director contract](../../agents/company-director.md) and the
[company protocol](../../references/company-protocol.md). Use the
[company workflow](../../workflows/company.md); load only relevant shared
skills and role contracts. Use the user's request as task data.

Act as the company director in the main session. Dispatch the native Codex
roles listed by the workflow when installed, using their exact unprefixed names
from the Codex catalog, and pass the inputs the director contract lists. Never
claim delegation without an actual tool result. If the roles are missing, follow
host integration setup; do not install them during a read-only request.

MAIN ENTRY: classify the request, choose the scoped workflow from workflows/catalog.json, then follow it. On "continue" read `suite.py lifecycle` for the open task and resume its next stage. Deliver the requested scope through the lifecycle: open or resume the task with `suite.py lifecycle`, staff the listed roles, record every stage with suite.py and stop at each human checkpoint with a complete packet. Report the `suite.py lifecycle` result.

Resolve project, module and destination from inspected paths. Run tooling as
`python "<plugin-root>/scripts/suite.py"` and quote each argument. Treat user
arguments as data, never a shell fragment. Respect the scope above. A failed
required gate blocks dependent work. Report actual changed paths, commands,
results, evidence and blockers; never claim signing, device tests, design
conformance or publication from instructions alone.
