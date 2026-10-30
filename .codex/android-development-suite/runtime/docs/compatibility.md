# Codex and Claude Code compatibility — 1.2.0

Checked on 2026-10-30 (Asia/Baghdad), Windows, Python 3.12.

| Capability | Codex 0.165.0-alpha.1.2 | Claude Code 2.1.289 |
|---|---|---|
| Manifest | `.codex-plugin/plugin.json` | `.claude-plugin/plugin.json` |
| Repository marketplace | `.agents/plugins/marketplace.json` | `.claude-plugin/marketplace.json` |
| Engineering skills | 24 adapters to canonical skills | 24 canonical skills |
| Workflow entry points | 14 `android-<command>` skills | 14 namespaced commands |
| Native specialist agents | 12 TOML agents after explicit project/user installation | 12 Markdown agents registered by the plugin |
| Specialist names | Unprefixed, e.g. `compose-developer` | `android-development-suite:compose-developer` |
| Tool guard | Explicit `hooks/codex.json` | Default `hooks/hooks.json` |
| Reviewable hook decision | `deny` with review reason | `ask` |
| Project Python tools | Shared standard-library implementation | Same implementation |

## Native agent verification

`scripts/install_codex_agents.py --project <existing-project>` installs the twelve
native definitions into `.codex/agents/` and a self-contained resource snapshot
into `.codex/android-development-suite/runtime/`. `--user` targets `CODEX_HOME` or
`~/.codex`. Definitions inherit the parent model, reasoning and permissions; no
config/auth/hook trust settings change. Edited managed files, unrelated name
collisions and linked targets are rejected. Upgrades and removal operate only on
ownership-inventoried files. A failed write rolls back file changes.

`python scripts/check_codex_agents.py --scope project` starts the actual Codex CLI
in an isolated home and trusted disposable project. A deterministic HTTP fixture
bound to localhost supplies protocol responses. The test verifies that the real
native spawn tool advertises all 12 agent names, then drives `spawn_agent`,
`wait_agent` and `close_agent` for each role. Every child request is checked for
its **exact complete installed developer instructions**, resolved runtime root
and inherited fixture model. Every wait must return the specialist's completed
handoff. The test is repeated with `--scope user` in a disposable `CODEX_HOME`.
Both scopes passed: 12 registered, 12 invoked, 12 completed handoffs; 49 localhost
requests per scope, **zero external model calls**. Personal configuration and
credentials are not used or changed. No hook trust is granted.

This verifies registration, instruction loading and dispatch through the real
runtime. The fixture is deterministic and does **not** evaluate real-model
reasoning, Android engineering quality or full Android project delivery.

## Plugin and regression verification

`python scripts/check_plugin.py --claude --codex` runs structural checks,
behavioral tests, native Claude validation/discovery, Codex installation and
skill discovery, and both native-agent scope tests. Its rich report is written
to `.checks/report.json`, excluded from distributions. The compact public result
is [compatibility.json](compatibility.json).

The isolated Codex marketplace test installs 1.2.0, confirms enabled state,
queries `skills/list` and `plugin/read`, and finds 38 enabled skills without
errors and one PreToolUse hook pointing to `hooks/codex.json`. It does not grant
hook trust. Claude's strict validators pass for the manifest, agents, skills and
commands; native discovery finds 12 agents, 38 skill/command entries and one hook.

The Python suite ran **82 tests, all passed**. The linked-directory test uses a
real Windows directory junction when ordinary symlink creation is unavailable.
Tests cover conflicting names, edited managed files, no-write preflights,
idempotence, upgrades, removal preserving user files, failed-write rollback,
malicious inventory paths, credential-like resources, malformed ownership
records, TOML Unicode/escaping, both installation targets,
relocation and runtime survival after deleting the original plugin, alongside
the existing hook/build/evidence/publishing/package regressions.

Native Android builds were not rerun for this change; source templates and pinned
versions remain unchanged. Earlier build evidence is retained in
[the original report](test-report.md). No real model-driven Android task or live
Play mutation is claimed. Native Linux/WSL and hosted CI execution remain separate
environment checks.

## Reproduce

From the repository root:

```sh
python android-development-suite/scripts/check_plugin.py --claude --codex
claude plugin validate . --strict --json
```

From the plugin directory:

```sh
python scripts/check_codex.py --marketplace-root .. --report .checks/codex.json
python scripts/check_codex_agents.py --scope project --report .checks/agents-project.json
python scripts/check_codex_agents.py --scope user --report .checks/agents-user.json
```

Absent CLIs are reported as blocked. Native probes cannot pass using structural
metadata alone. The CI Windows/Linux matrix runs standard-library regressions
without needing installed or authenticated host CLIs.

Sources: [Codex custom agents](https://learn.chatgpt.com/docs/agent-configuration/subagents),
[OpenAI packaging](https://developers.openai.com/plugins/build/plugins),
[Codex hooks](https://learn.chatgpt.com/docs/hooks),
[Claude manifests](https://code.claude.com/docs/en/plugins-reference).
