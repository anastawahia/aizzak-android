# Host integration

## Resource paths

The plugin root is the directory containing `scripts/suite.py`, `references/`
and the manifests. Resolve it from the absolute path of the loaded instruction,
never from the application's working directory. A skill at
`skills/<name>/SKILL.md` or `codex-skills/<name>/SKILL.md` is two directories below
the root; an agent or command is one directory below it.

In all instructions, `<plugin-root>` is a placeholder for that inspected absolute
path, not a shell variable. Replace it before running commands. Use
`python "<plugin-root>/scripts/suite.py"` (or `python3` on Linux) and quote project
paths individually. The scripts operate on explicit `--project`/destination
paths; changing the working directory to the plugin is unnecessary.

Claude commands can use the host-expanded `${CLAUDE_PLUGIN_ROOT}`. Shared skills
and Codex entry points resolve paths from their own loaded file instead. Do not
assume either `CLAUDE_PLUGIN_ROOT` or `PLUGIN_ROOT` exists in interactive shells;
the documented environment variables are supplied to hook processes.

## Claude Code

`.claude-plugin/plugin.json` uses native `agents/`, `skills/`, `commands/` and
`hooks/hooks.json` discovery: 12 agents, 24 skills and 15 commands. Invoke
`/android-development-suite:<command>`. Delegate to a registered specialist only
when useful and permitted by the current task and repository instructions.

## Codex

`.codex-plugin/plugin.json` declares `codex-skills/`: 24 lightweight adapters to
the shared engineering skills and 15 workflow entry points named
`android-<command>`. For one coordinator select
`android-development-suite:android-orchestrate`; it applies android-orchestrator
in the main session, dispatches specialists and follows the
[human checkpoint protocol](single-agent-delivery.md). The host exposes namespaced names such as
`android-development-suite:android-analyze`,
`android-development-suite:android-build-feature`, and
`android-development-suite:android-test`. Select the skill in the picker or
request that displayed name; supply the task and explicit project path.

`codex-agents/` contains 12 native TOML definitions generated from the same
Markdown contracts in `agents/`. Plugin installation does not automatically
register them. Run `python "<plugin-root>/scripts/install_codex_agents.py"
--project "<existing-project>"` in an authorized setup task. The installer copies
the definitions to `<project>/.codex/agents/` and a self-contained resource snapshot
to `<project>/.codex/android-development-suite/runtime/`. Native definitions embed
that snapshot's absolute path. `--user` explicitly targets `CODEX_HOME` or
`~/.codex` instead. `--dry-run` previews; `--uninstall` removes unchanged managed
files. Reinstall after upgrades or after moving the target project to update its
absolute resource paths. The original plugin directory can be moved or removed.
Config/auth files and hook trust are never changed. Edited managed resources and
unrelated agent name collisions stop installation/removal before writes.

Open the trusted target project in a new Codex chat. Exact agent names are the
unprefixed names in `references/codex-catalog.json`, e.g. `compose-developer` and
`android-code-reviewer`. When delegation is authorized, use these types with the
host's native agent tool and pass project root, scope, file ownership, acceptance
criteria and allowed checks. Skills and model/reasoning/permissions inherit from
the parent; these definitions do not pin a model or override host permissions.
Reviewer read-only behavior is an instruction contract, not a separate sandbox.
If agents are unavailable or disabled, apply the relevant contract in the main
session and report the fallback. Never claim dispatch based solely on reading a
role file. A coordinator subagent returns a plan/handoffs to the parent; no nested
delegation requirement exists.

## Hooks

Claude uses `hooks/hooks.json`; Codex explicitly selects `hooks/codex.json`.
Both call the same heuristic guard. Codex normalizes shell tools as `Bash` and
patches as `apply_patch` with `tool_input.command`; raw CLI tool names are also
handled. Claude receives `ask` for reviewable operations. Codex currently does
not support `ask` in PreToolUse, so its adapter returns `deny` with a review
reason. Review and adjust the exact command or hook policy when appropriate;
do not automatically retry blocked operations through another tool.

Codex plugin hooks require the user's trust review before they run. Installation
does not grant hook trust. A disabled/untrusted hook does not prevent the skills
and Python tools from working. Host permissions remain authoritative, and these
heuristics are not a complete shell parser or secret scanner.

Sources: [OpenAI plugin packaging](https://developers.openai.com/plugins/build/plugins),
[Codex native agents](https://learn.chatgpt.com/docs/agent-configuration/subagents),
[Codex hooks](https://learn.chatgpt.com/docs/hooks),
[Claude manifest reference](https://code.claude.com/docs/en/plugins-reference).
