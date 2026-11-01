# Host integration

## Resource paths

The plugin root is the directory containing `scripts/suite.py`, `references/`
and the manifests. Resolve it from the absolute path of the loaded instruction,
never from the application's working directory. A skill at
`skills/<name>/SKILL.md` or `codex-skills/<name>/SKILL.md` is two directories below
the root; an agent or command is one directory below it.

`<plugin-root>` is a placeholder for that inspected absolute path, not a shell
variable. Use `python "<plugin-root>/scripts/suite.py"` (or `python3` on Linux)
and quote project paths individually.

Claude commands can use the host-expanded `${CLAUDE_PLUGIN_ROOT}`. Shared skills
and Codex entry points resolve paths from their own loaded file instead.

## Claude Code

`.claude-plugin/plugin.json` uses native `agents/`, `skills/`, `commands/` and
`hooks/hooks.json` discovery: 18 agents, 32 skills and 18
commands. Start with `/android-software-house:company <task>`. Specialists are
registered as `android-software-house:<role>`.

## Codex

`.codex-plugin/plugin.json` declares `codex-skills/`: 32 adapters to the
shared skills and 18 entry points named `android-<command>`. Start with
`android-software-house:android-company`.

`codex-agents/` holds 18 native TOML roles generated from the same contracts
as `agents/`. Plugin installation does not register them. Run
`python "<plugin-root>/scripts/install_codex_agents.py" --project "<existing-project>"`
in an authorized setup task; it copies the roles to `<project>/.codex/agents/` and
a resource snapshot to `<project>/.codex/android-software-house/runtime/`. `--user`
targets `CODEX_HOME` or `~/.codex`; `--dry-run` previews; `--uninstall` removes
unchanged managed files. Projects that have the 1.x `android-development-suite`
agents installed run `--migrate-legacy` once: it removes only unchanged legacy
files and then installs this version. Reinstall after upgrades or project moves.

Use the exact unprefixed role names from `references/codex-catalog.json` with the
host's agent tool. Skills, model, reasoning and permissions inherit from the
parent. Reviewer read-only behavior is an instruction contract, not a sandbox.
If agents are unavailable, apply the contract in the main session, report the
fallback, and keep independent review stages blocked.

## Hooks

Claude uses `hooks/hooks.json`; Codex selects `hooks/codex.json`. Both call the
same heuristic guard. Codex does not support `ask` in PreToolUse, so its adapter
returns `deny` with a review reason. Codex plugin hooks require the user's trust
review. Host permissions remain authoritative; the guard is not a complete shell
parser or secret scanner.
