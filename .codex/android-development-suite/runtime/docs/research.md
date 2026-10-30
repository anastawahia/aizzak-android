# Compatibility research — 2026-10-30

The installed CLI reports Claude Code 2.1.289. Its plugin help, strict validator,
component-directory validators and actual `--plugin-dir ... plugin details`
inventory were inspected before relying on the plugin format. A locally installed
Firebase plugin was also inspected for the standard manifest convention.

Primary references checked:

- [Plugin manifest](https://code.claude.com/docs/en/plugins-reference): manifest
  under `.claude-plugin`, components at plugin root, documented default discovery.
- [Skills](https://code.claude.com/docs/en/skills): SKILL.md directories, focused
  descriptions, resource links, arguments and plugin path substitutions.
- [Subagents](https://code.claude.com/docs/en/sub-agents): Markdown/frontmatter,
  tools, inherited model, bounded turns and main-session coordination.
- [Hooks](https://code.claude.com/docs/en/hooks): PreToolUse JSON input/decision
  output and Bash/PowerShell tool matching.
- [Plugin CLI](https://code.claude.com/docs/en/plugins/cli-reference): installed
  validation/loading commands and component inventory. A Markdown file is not a
  manifest; component-directory validation is used instead.
- [Marketplace](https://code.claude.com/docs/en/plugins/marketplace-reference):
  local relative plugin source and declared owner/description.
- [AGP 8.13](https://developer.android.com/build/releases/agp-8-13-0-release-notes):
  the chosen 8.13.2 baseline supports Kotlin 2.3, Gradle 8.13 and JDK 17/API 36.
- [AGP 9 built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin):
  a migration boundary, not a drop-in version replacement for this template.
- [Compose BOM](https://developer.android.com/develop/ui/compose/bom/bom-mapping):
  library alignment is distinct from the Kotlin/Compose compiler plugin version.
- [Play target requirements](https://developer.android.com/google/play/requirements/target-sdk):
  release-time verification is mandatory; no evergreen target/API deadline is asserted.

Official Google Maven POMs for the selected AGP, Compose BOM, Activity and Lifecycle
versions returned HTTP 200. Actual Android compilation and test execution then
verified the complete resolved set. This is stronger evidence than artifact
existence alone. The selected versions are a reproducible supported baseline,
not a claim that they are the newest available dependencies.

Newer online docs describe capabilities beyond 2.1.289. This plugin relies on
mechanisms observed in the installed CLI; it does not require newer workflow
runtimes, a nested-agent capability, an alias schema or optional MCP servers.
