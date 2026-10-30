# Developer guide

## Source and generated content

`tools/render_content.py` owns the authored instruction catalog and generates
committed Claude Markdown agents, native Codex TOML agents, shared skills,
Claude commands, Codex skill adapters and workflow
files plus discovery catalogs. `codex-skills/` contains short adapters to the
shared skills and generated `android-<command>` workflow entry points. Do not
edit adapters independently of the generator.
`codex-agents/*.toml` uses three TOML basic-string fields generated from the same
canonical role body. `@@PLUGIN_ROOT@@` is resolved only by the installer to its
managed runtime snapshot. Do not pin models, reasoning or permissions in adapters.
Edit its role/skill/workflow data and render, then review the resulting Markdown.
Python tooling, templates, references and docs are maintained directly. There is
no automatic generator execution when the plugin loads.

## Add a capability

1. Add a narrowly described skill to SKILLS with real procedures, standards,
   inputs, outputs, checks and recovery; generate `skills/<name>/SKILL.md` with
   supported frontmatter. Put lengthy material in linked references.
2. Add or adapt a specialist contract in AGENTS. Give it file ownership and
   output criteria; use `model: inherit`. Avoid permission modes/pregrants that
   change the user's controls. Reviewers should not edit their own findings.
3. Add routing under FLOWS and optionally a COMMANDS entry. Commands use
   `$ARGUMENTS` as data and `${CLAUDE_PLUGIN_ROOT}` to locate resources. Plain JSON
   in workflows/ is not a registered workflow runtime.
4. Add a new template to templates/ and wire a renderer only when it prevents
   repeated mechanical work. Refuse overwrite, validate identifiers/paths and
   reject unresolved tokens. Preserve existing-project conventions.
5. Add tests for actual behavior or safety invariants; do not rely on checking
   that a heading or description exists. Run render, tests and both validators.

## Check changes

```sh
python tools/render_content.py
python -m unittest discover -s tests -v
python scripts/suite.py validate
python scripts/check_plugin.py --claude --codex
claude plugin validate . --strict --json
claude plugin validate agents --strict --json
claude plugin validate skills --strict --json
claude plugin validate commands --strict --json
claude --plugin-dir . plugin details android-development-suite
```

Claude's `contents: []` JSON means no content diagnostics; it is not a per-file
inventory. `plugin details` verifies actual runtime discovery. Passing a Markdown
file directly to plugin validate is unsupported: it is interpreted as a JSON
manifest. Use the component directory instead.

For template behavior run the isolated demo described in the test report. Never
run integration fixtures against production credentials. Before changing a pinned
version set recheck official compatibility sources, artifact existence and actual
compilation. Avoid upgrading an existing app as a side effect of plugin updates.

```sh
python scripts/demo.py --destination ../isolated-demo --extended --execute
```

This creates a new, marked fixture app, integrates the feature and OpenAPI
templates, runs HTTPS MockWebServer/JVM checks, compiles instrumentation sources
and invokes the optional analyzer. It never uses live backend/Play credentials.
Reports live under the demo's `.android-suite/evidence/`. Without `--execute` it
only renders sources. It refuses arbitrary existing product directories.

For local signing mechanics, use a marked demo only:

```sh
python scripts/signing_smoke.py --project ../isolated-demo --execute
```

The utility creates a random-password, one-day test keystore, supplies passwords
through process environment, builds the AAB, checks its signature and deletes
the key in cleanup. It restores pre-existing signing environment variables.
The resulting artifact is a fixture; upload-certificate matching, required device
checks, policy eligibility and publication remain unverified. It never calls Play.

## Distribution

`python scripts/package_plugin.py --output ../dist/android-development-suite.zip`
creates a deterministic ZIP, checks structure, excludes caches and fails if
credential-like files appear. It packages the plugin root under its name.
Add `--marketplace` to create a standalone distribution containing both host
catalogs, installation instructions and the plugin under an
`android-development-tools/` root. Extract that archive and register the root
with either host's marketplace command.
Validate the ZIP by loading it with the installed CLI. The repository-level
`.claude-plugin/marketplace.json` supports a local marketplace; if distributing a
Git-hosted marketplace, use the actual repository URL after publishing it.
No fictional remote owner/repository is included in installation instructions.

Bump both host manifests and the Claude marketplace version together. The Codex
marketplace resolves the version from the plugin manifest. Native Codex checks
use a disposable `CODEX_HOME`, install via the local marketplace, then query
`skills/list` and `plugin/read`. `check_codex_agents.py` also uses a deterministic
localhost Responses fixture to drive actual native `spawn_agent`, `wait_agent`
and `close_agent` calls for every specialist in both scopes. It checks the full
installed contract in every child request and each completed handoff. No external
model calls, personal settings changes or hook trust grants occur. This measures
runtime registration and dispatch, not model reasoning or Android task completion.

The agent installer keeps a SHA-256 ownership inventory, preflights all managed
files and name collisions, rejects symlink/junction targets, rolls back failed
writes and preserves user configuration. Resource snapshots keep installed agents
independent of cache paths. Repeat installation after plugin updates or project
relocation. User-scope tests always point `CODEX_HOME` at a disposable directory.
Keep MIT notice with plugin
source and retain third-party licensing for any fetched dependencies. A release
of this plugin is separate from an Android application's Play release.

## API client scope

`publish_play.py` uses only the documented Android Publisher v3 edit API. Credentials
and account access are supplied externally; no auth emulation or fake publication
path exists. Protocol tests patch transport and clearly remain local tests.
Policy forms, listing assets, first application setup, production access and
review state require Play Console and human review. Large/resumable uploads and
automatic promotion/rollback are intentionally not implemented.
