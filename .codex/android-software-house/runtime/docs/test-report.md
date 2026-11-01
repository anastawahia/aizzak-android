# Test report: 2.0.0

Date: 2026-10-31. Host: Windows 11, Python 3.12, JDK 17.0.19, Android SDK at
`D:\SDK`, Claude Code 2.1.289. Codex CLI was not installed on this host, so the
Codex runtime checks (`check_plugin.py --codex`) were **not run** for 2.0.0.
The 1.3.0 Codex results in [history](history/1.3.0/README.md) do not carry over.

## Plugin checks

| Check | Result |
|---|---|
| `tools/render_content.py --check` (also on a fresh Windows clone with `core.autocrlf=true`) | passed |
| `python -m unittest discover -s tests` | 111 tests, all passed |
| `scripts/suite.py validate` (structure, catalogs, lifecycle/workflow consistency, local links) | passed |
| `claude plugin validate` on the plugin, `agents/`, `skills/`, `commands/` (`--strict`) | passed |
| `claude --plugin-dir . plugin details android-software-house` | discovered 18 agents, 50 skills (32 skills + 18 commands), 1 PreToolUse hook |
| `install_codex_agents.py --project <AIZZAK> --migrate-legacy --dry-run` | passed: 1.3.0 install with 12 roles and 198 unchanged files detected; nothing written |

New tests cover: stage ordering, mandatory stages, design QA that cannot be
skipped after an inventory approval, acceptance requiring a passed design QA,
inventory validation, owner references on checkpoints, staleness after source
and reference changes, every conformance failure mode (missing element,
unapproved deviation, dropped screen, uncovered configuration, changed
screenshot, reviewer who implemented, changed reference, older source),
the CLI round trip, generator reproducibility, records-only destination
adoption, and legacy migration including restore on a failed install.

## Android demo build (`scripts/demo.py --execute`)

A fresh app rendered from the template, with the official checksummed wrapper:

| Check | Result |
|---|---|
| `app:all` (assembleDebug, testDebugUnitTest, lintDebug) | passed, 5 tests, 0 failures |
| `app:ui-compile` (assembleDebugAndroidTest) | passed |
| `app:static` (isolated Detekt) | passed |
| `app:screenshots` (recordRoborazziDebug) | passed, 4 images: light/dark 411dp, light 840dp, font scale 1.3 |

Two template defects were found by this run and fixed before the passing run:
Robolectric 4.17 needs Java 21 to render SDK 36 (the template now pins JVM tests
to SDK 34 in `robolectric.properties`), and the screenshot test's resource
qualifiers had the locale after the width.

## End-to-end lifecycle on the demo app (real CLI, real screenshots)

`ui-redesign` task: inventory validated; `build` before design approval refused
(exit 2); design approved with the reference and inventory; build recorded;
verification passed by re-running the implementation gate on the executed
`verify --kind all` report; design QA with a `missing` element failed and did
not advance; a record whose reviewer was the implementer failed only
`independent_design_review`; a complete independent record passed; acceptance
recorded; `lifecycle` reported `completed: true`. After editing one string
resource, `lifecycle` reported `completed: false` with `verification`,
`design-qa` and `acceptance` stale.

## Not verified

- Codex skill discovery and native agent dispatch for 2.0.0 (CLI not installed).
- Real-model behavior: whether Claude or Codex agents follow the contracts on
  a real app task. The checks above verify loading, tooling and enforcement.
- Device runtime, TalkBack, and Google Play operations (none were run).
- See [limitations](limitations.md).
