# Android Development Suite

A Codex and Claude Code plugin for Kotlin/Compose Android delivery: requirements,
architecture, design, implementation, integration, testing, review, performance,
release preparation, approved Google Play submission and maintenance.

It combines specialized instruction-based agents with real, portable project
tools. The coordinator selects a scoped workflow and uses the active host's
main session and available delegation tools. Both hosts support the twelve native
specialists; Codex requires the explicit agent installer below.
Small fixes use a partial plan. Long references load only when needed.

## One coordinator for every task

In Codex select **android-development-suite:android-orchestrate** once, then
describe the outcome in your normal language. This applies the android-orchestrator
role in the main conversation, where it can dispatch specialists and receive their
results. You do not need to choose workflows or invoke each specialist yourself.

For example:

```text
Use android-orchestrate as my single coordinator for C:/Projects/MyApp.
Add offline favorites. Choose the specialists, accept their outputs and pass
them to the next stage. Pause for my review of changed requirements, design
documents and the prepared PR before pushing/opening it.
```

The same entrypoint handles a new app (supply the idea and destination), edits,
defects and unfinished apps. For unfinished work say "continue this app" and
identify the remaining outcome; the coordinator checks source and saved progress
before routing the remaining tasks. In the same chat simply describe your next
task or approve/request changes to the current review packet.

The delivery loop is intake -> requirements -> **human review** -> design ->
**human review** -> implementation -> QA/independent review -> prepared PR ->
**human review** -> approved push/PR creation -> verified handoff. Only applicable
stages run: a small fix can reference unchanged requirements/design; read-only,
planning, test and build requests retain their scope. PR creation is performed
only when included in the delivery scope and approved with concrete branch,
repository, diff and title/body details. Merge and publication remain separate.

At a checkpoint the coordinator links real documents/results, explains the next
action and waits for your reply. Say "approve these requirements", "change ..."
or "approve this PR packet". Approval resumes the next dependent stage without
requiring a new specialist invocation. Task records under
`.android-suite/tasks/<task-slug>/status.md` preserve handoffs and version-bound
decisions so a new chat can resume unchanged approved work.

Native specialists still require the installer below. When unavailable the main
session reports the fallback; it cannot claim independent review or delegation
without actual results. A directly invoked coordinator subagent returns handoffs
to its parent, and the parent continues dispatch; nested agents are not required.
This is an instruction-driven session, not an always-running background service.
See [the coordinator protocol](references/single-agent-delivery.md).

## Install and verify

### Codex

From the parent repository (the directory containing `.agents/plugins/`):

```sh
codex plugin marketplace add .
codex plugin add android-development-suite@android-development-tools
codex plugin list --marketplace android-development-tools --json
python android-development-suite/scripts/install_codex_agents.py --project "/absolute/path/to/android-project"
```

Restart/open a new chat and select a skill in the picker. For single-contact
delivery choose `android-development-suite:android-orchestrate`. Other names include
`android-development-suite:android-analyze`,
`android-development-suite:android-build-feature`, and
`android-development-suite:android-test`. The 15 command equivalents use the
`android-` prefix; the 24 engineering skills retain their names. Describe the
task and explicit project path after selecting the skill. There are 39 Codex
skills in total, plus **12 native agents after the installer runs**. Replace the
project path with an existing project. Open it as a trusted project and start a
new chat so Codex reloads the agent definitions. No Claude installation is required.

The installer writes `.codex/agents/*.toml` and a self-contained resource snapshot
under `.codex/android-development-suite/runtime/` in that project. Moving/removing
the original extracted plugin does not break installed agent resources. Names
are unprefixed, for example `android-orchestrator`, `compose-developer`,
`integration-engineer` and `android-code-reviewer`; see [all twelve](references/agent-catalog.md).
Ask Codex to delegate to named specialists, or invoke a workflow skill. The main
session owns dispatch and file ownership; a coordinator subagent returns handoffs
to its parent when nested delegation is unavailable.

For personal agents replace `--project ...` with `--user`. This uses `CODEX_HOME`
or `~/.codex`. Add `--dry-run` to preview; rerun after an upgrade; add `--uninstall`
to remove unchanged managed files. No configuration/auth/hook-trust settings are
changed. Existing unrelated agents are preserved. Conflicting names or edited
managed files stop the operation before any write; reconcile those files first.
Project instructions and parent permissions still govern every specialist.

Codex **0.165.0-alpha.1.2** was tested with actual isolated installation and
app-server discovery. `.codex-plugin/plugin.json` selects `codex-skills/` and
`hooks/codex.json`. Review/trust the optional hooks in the host before expecting
them to run. [Host behavior and paths](references/hosts.md),
[current compatibility evidence](docs/compatibility.md).

### Claude Code

Tested host: Claude Code **2.1.289**, Python **3.12**, Windows, JDK **17** and
Android SDK **36**. Use Python 3.10+ (3.11+ if extending TOML tooling). Keep `python`
on PATH for hooks; on Linux create a suitable `python` command or change the hook
command to `python3`. The explicit tooling works with either interpreter name.

From this plugin directory, load without changing installed plugins:

```sh
claude --plugin-dir .
```

From the parent repository, persistent local installation is supported through
the included marketplace:

```sh
claude plugin marketplace add .
claude plugin install android-development-suite@android-development-tools --scope local
```

Restart Claude after installation, or use `/reload-plugins` when supported.
To inspect actual discovery without model calls:

```sh
claude --plugin-dir . plugin details android-development-suite
claude plugin validate . --strict --json
python scripts/suite.py validate
python -m unittest discover -s tests -v
python scripts/check_plugin.py --claude --codex
```

The plugin needs no MCP service and declares no `.mcp.json`. Core validation uses
only the Python standard library. Android dependencies and Gradle are downloaded
only as part of explicitly requested setup/build work. Production credentials
are never bundled. [Execution evidence and limits](docs/test-report.md).

## Commands

The table describes both hosts. In Codex choose the corresponding
`android-development-suite:android-<suffix>` skill, such as `android-test` for
the `test` row. The slash-command syntax below applies to Claude Code.

Claude namespaces plugin commands with the manifest name. Therefore the supported
commands are `/android-development-suite:new-app`, etc. The requested shorthand
`/android:new-app` would require renaming the plugin to `android`; no undocumented
alias or alternate manifest field is used.

| Command suffix | Arguments | Behavior |
|---|---|---|
| `orchestrate` | outcome, project/destination | One coordinator, automatic scoped routing, accepted handoffs and human requirements/design/PR checkpoints |
| `new-app` | idea, destination, application ID | Requirements, minimal architecture, Compose scaffold and initial checks |
| `analyze` | project | Read-only repository/dependency/test inventory |
| `plan` | feature or defect, project | Implementation DAG, owners, acceptance checks; planning only |
| `design` | screen or journey, project | Screen/state/token/navigation/accessibility specification |
| `build-feature` | feature, project | Scoped implementation and regression verification |
| `integrate-api` | OpenAPI file/URL, project | Contract-based API and data integration |
| `test` | unit/ui/all, module, project | Actual checks with explicit blocked/skipped status |
| `review` | diff/scope, project | Independent review with P0–P3 actionable findings |
| `security-audit` | scope, project | Review-only threat, permissions, dependency and license assessment |
| `performance-audit` | journey/metric, project | Measurement plan/profiles/findings; no automatic source edits |
| `build` | debug/release, module, project | Build only; release needs configured signing |
| `release` | version, project | Signed AAB/readiness preparation; no Play submission |
| `publish` | AAB, package, versionCode, track, rollout | Dry-run then exact, explicitly authorized submission |
| `status` | project | Read-only knowledge/progress and current evidence |

Examples inside Claude Code:

```text
/android-development-suite:new-app A local habit tracker in ../HabitApp, com.example.habits
/android-development-suite:analyze C:/work/ExistingApp
/android-development-suite:build-feature Add offline retry to the existing inbox
/android-development-suite:integrate-api docs/openapi.json
/android-development-suite:test unit app
/android-development-suite:release 1.2.0
```

The 24 reusable skills are also available as namespaced invocations, such as
`/android-development-suite:state-management`, and can be selected by Claude.
Command arguments are task data, not untrusted shell snippets.

## Architecture and catalogs

```text
Main host session + android-orchestrator contract
  -> scoped plan + specialist handoffs
  -> specialist skills + on-demand references/templates
  -> Python tools + project Gradle Wrapper
  -> actual reports + source/artifact hashes
  -> required quality gates + next-stage decision
  -> repository-local .android-suite knowledge
```

All **12 agents** have missions, tool boundaries, input/output contracts,
handoffs, escalation and completion requirements:

| Agent | Ownership |
|---|---|
| android-orchestrator | Workflow routing, dependencies, gates and consolidation |
| product-analyst | PRD, stories, acceptance and prioritization |
| android-architect | Layers/modules, DI boundaries, data flow and ADRs |
| ui-ux-designer | Implementable journeys, Material 3, accessibility and adaptive specs |
| compose-developer | Components, screens, state rendering and semantics |
| android-core-developer | Kotlin logic, ViewModels, coroutines and lifecycle |
| integration-engineer | REST/auth, persistence, caching and offline sync |
| android-qa-engineer | Test strategy, execution and regression evidence |
| security-reviewer | Independent Android/security/dependency/license review |
| performance-engineer | Measured startup, memory, battery and rendering work |
| build-release-engineer | Gradle, CI, signing, artifacts and approved submission |
| android-code-reviewer | Independent correctness, concurrency and maintainability review |

[Agent catalog](references/agent-catalog.md) · [24-skill catalog](references/skill-catalog.md).
Skills cover planning, bootstrap/architecture/DI, Material/Compose/adaptive UI,
Kotlin/state/navigation, API/storage/sync, security, all testing levels,
static analysis/performance, Gradle/CI/Play and crash maintenance.

Specialists load only relevant skills instead of preloading the entire catalog.
Commands and Codex entry points apply the orchestrator contract in the main conversation. When invoked
as a subagent, android-orchestrator returns a task DAG for the parent to dispatch;
the plugin does not depend on nested agents. If delegation is unavailable, the
main session can perform the same scoped work and must state that limitation.
Parallel work requires stable interfaces and disjoint file ownership.

## Workflows and gates

[Workflow catalog](workflows/catalog.json) defines all 14 workflows: single-contact delivery, new app,
analysis, feature, bugfix, UI redesign, backend integration, architecture refactor,
regression testing, security audit, performance optimization, release preparation,
Play publication and post-release incident handling. Each includes prerequisites,
agents/skills, steps, safe parallelism, artifacts, gates, failure handling,
approval boundaries and a final-report contract. JSON/Markdown workflow files are
explicit supporting resources, not auto-registered Claude executable workflows.

```sh
python scripts/suite.py route feature --small
```

Requirements, architecture, implementation, integration and release gates have
[required checks](references/gates.json). Failed, skipped, absent or stale required
evidence blocks automatic progression. The [evidence protocol](references/evidence.md)
explains hashes, human review and documented exceptions. Hashes prove file identity,
not the truth of a manually supplied review. No automatic bypass is provided.

## Tooling and configuration

Commands below are run from this plugin directory; project paths are explicit.
`--report <path>` precedes the subcommand and writes an explicit JSON report.

```sh
python scripts/suite.py doctor --project ../MyApp
python scripts/suite.py inspect --project ../MyApp
python scripts/suite.py init ../NewApp --name "New App" --package com.example.newapp --min-sdk 26 --target-sdk 36
python scripts/suite.py wrapper --project ../NewApp --download
python scripts/suite.py init-memory --project ../NewApp
python scripts/suite.py --report ../NewApp/.android-suite/evidence/checks.json verify --project ../NewApp --kind all --execute
python scripts/suite.py feature ../NewApp/feature-settings --package com.example.newapp.settings --name Settings
python scripts/suite.py api ../GeneratedApi --package com.example.newapp.data --spec tests/fixtures/openapi.json
python scripts/suite.py dependencies --project ../NewApp --sbom ../NewApp/build/reports/bom.json
```

`init` supports namespace, separate `--application-id`, min/target/compile SDK;
the baseline supports APIs up to 36. Build types are debug/release, with debug ID
suffix. Add product-specific flavors/device-class resources according to the
inspected project's requirements. The starter is phone/tablet oriented; Wear,
TV, Auto, native SDKs and AGP 9 migrations require tailored configuration.

`verify --kind` accepts `build`, `unit`, `lint`, `ui`, `ui-compile`, `release`, `dependencies`,
`static` or `all` (debug build + JVM tests + lint). Default module is `app`;
`--module feature:settings` targets a nested module. `static` invokes an existing
Detekt task; it is opt-in for the starter. Use `--attempts 1..3` only for transient
transport retries. No script silently edits code to achieve a green result.

`--execute` authorizes a trusted wrapper to run project code. Inspection does not.
`wrapper --download` fetches official Gradle scripts/JAR, verifies the JAR SHA-256
and pins the distribution checksum. It refuses existing/partial wrapper files.
Downloads/caches are local; no system Gradle install is needed.

[Version profile](references/versions.json): AGP 8.13.2, Kotlin 2.3.0, Gradle 8.13,
JDK 17, API 36, a pinned Compose BOM and compatible AndroidX dependencies. This
is a reviewed reproducible baseline, not a latest-version claim. Existing projects
retain their own versions. Recheck official compatibility and Play rules before
upgrade/release. The optional Detekt CLI is compiler-isolated and its parser
support must be reviewed when using newer Kotlin syntax.

The minimum starter has no unnecessary Hilt/network/database layers. Use the
default recommended integrations when justified: Hilt, Retrofit/OkHttp/Kotlinx
Serialization, Room, DataStore, WorkManager, Navigation Compose and configurable
Crashlytics/monitoring. [Engineering procedures](references/engineering.md).

## Environment, Windows and WSL

Android builds require JDK, SDK platform/build tools, accepted SDK licenses and
network access for initial dependency resolution. Device checks additionally need
an authorized physical device or emulator; absence is reported as blocked.

```powershell
.\scripts\doctor.ps1 -Project C:\work\MyApp -Devices
python .\scripts\suite.py doctor --project C:\work\MyApp --devices
```

```sh
sh scripts/doctor.sh --project /work/MyApp --devices
```

Doctor distinguishes build host, SDK, emulator binary, usable device serials and
running emulator serials. `--devices` explicitly permits querying ADB and starting
its local server. WSL is a separate Linux build host: Windows SDK installation
does not give it Linux build tools, emulator acceleration or USB authorization.
Use a Linux SDK and explicitly configured emulator/ADB networking or USB forwarding.
Windows projects must have paths without cmd metacharacters for safe `.bat` execution.

## Security and release

The lightweight PreToolUse hook reviews Write/Edit/Bash/PowerShell inputs and
Codex apply_patch/exec inputs. It
blocks obvious private-key text and requests review for sensitive-file writes
and known destructive/publication patterns. It never modifies the repository or
logs raw tool inputs. It is a heuristic, not a complete shell parser, secret
scanner or security boundary; use host permissions and dedicated scanning.
Claude uses `ask` for reviewable actions. Codex's adapter uses `deny` because its
PreToolUse protocol does not currently support `ask`. A blocked command needs
review of the exact operation or hook policy before retrying. Codex hook trust
is a separate host decision; installing this plugin does not silently grant it.

Signing secrets and Play OAuth tokens come from approved providers. Release
builds fail clearly when signing variables are missing; no debug-key fallback.
No blanket source rewrites, automatic software installs or production actions.
Dependency inventory is not an SBOM/vulnerability scan: provide a resolved
CycloneDX SBOM and use an approved current scanner. Unknown licenses and GPL/AGPL
obligations require qualified review; no legal clearance is asserted.

`publish_play.py` defaults to no-network dry-run. Real API upload requires an
explicit `--execute`, current release evidence, signature verification, a human
approval record bound to artifact/package/version/track/rollout, and a short-lived
`PLAY_ACCESS_TOKEN`. The approval record is an audit guard, not authentication.
The API client handles edit/upload/track/validate/commit and best-effort discard
on failure. It does not create a store listing or satisfy policy declarations.
There are no automatic mutation retries. Successful submission is distinct from
review acceptance and public availability. [Complete release runbook](references/play-release.md).

## Troubleshooting

| Symptom | Action |
|---|---|
| No commands/agents | Check `--plugin-dir` points at this directory; use `plugin details`, `/plugin` errors, restart/reload |
| No Codex skills | Verify marketplace installation and enabled state with `codex plugin list`; restart/open a new chat; use the namespaced picker entry |
| Codex roles absent from agent list | Run `scripts/install_codex_agents.py --project <existing-project>` (or `--user`), open the trusted target project in a new chat; verify the parent has agents enabled |
| Codex hook skipped | Review/trust its current definition in the host; skill discovery alone does not prove hook execution |
| Hook cannot find Python | Put a Python 3 interpreter named `python` on PATH or adapt hooks.json to python3 |
| Missing wrapper | Review the version profile and run explicit `wrapper --download`; never overwrite a partial wrapper blindly |
| Dependency resolution fails | Preserve first failure, verify network/proxy and versions; bounded retries for transport failures only |
| JDK/SDK mismatch | Run doctor; align JAVA_HOME/ANDROID_HOME and exact platform before retrying |
| UI tests blocked | Authorize/start a compatible device; generated/compiled tests have not executed |
| Release signing fails | Configure the four signing environment values via the approved provider outside Git |
| Gate stale | Rerun affected checks on final source; do not replace hashes with guessed success |
| Play API error/timeout | Inspect Console and edit/version IDs before retrying; request renewed approval if target changes |

## Extend and maintain

See [developer guide](docs/development.md), [compatibility research](docs/research.md), [acceptance scenarios](tests/acceptance.md)
and [known limitations](docs/limitations.md). All instructions and script source
are included. Add components through the documented discovery directories; the
manifest uses standard defaults. No unsupported registration keys are required.

This implementation is not represented as production-certified merely because
files and tests exist. Device behavior, live services, hosted CI, policy/account
eligibility and authenticated agent quality require their own execution evidence.
