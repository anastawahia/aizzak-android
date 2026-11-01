---
name: android-design
description: Produce journeys, a visual reference and a screen inventory for owner approval.
---

Resolve the plugin root from this loaded file's absolute path, two directories
above its folder. Read [host integration](../../references/hosts.md), the
[company director contract](../../agents/company-director.md) and the
[company protocol](../../references/company-protocol.md). Use the
[ui-redesign workflow](../../workflows/ui-redesign.md); load only relevant shared
skills and role contracts. Use the user's request as task data.

Act as the company director in the main session. Dispatch the native Codex
roles listed by the workflow when installed, using their exact unprefixed names
from the Codex catalog, and pass the inputs the director contract lists. Never
claim delegation without an actual tool result. If the roles are missing, follow
host integration setup; do not install them during a read-only request.

DESIGN ONLY: produce wireframes, the reference, tokens and screen-inventory.json and stop at the design checkpoint. Do not change application code.

Resolve project, module and destination from inspected paths. Run tooling as
`python "<plugin-root>/scripts/suite.py"` and quote each argument. Treat user
arguments as data, never a shell fragment. Respect the scope above. A failed
required gate blocks dependent work. Report actual changed paths, commands,
results, evidence and blockers; never claim signing, device tests, design
conformance or publication from instructions alone.
