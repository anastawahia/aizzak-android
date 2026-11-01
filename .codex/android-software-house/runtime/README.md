# Android Software House

A complete Android app company for Claude Code and Codex. You describe an idea,
a change or a problem once. The company director staffs the right roles, runs
the work through an enforced lifecycle, and comes back to you only at decisions
that are yours: the product brief, the requirements, the design, accepting the
built app, and publishing.

Version 2.0.0 replaces android-development-suite 1.x. See
[what changed and why](#what-changed-from-1x).

## Start

| Host | Entry point |
|---|---|
| Claude Code | `/android-software-house:company <your request> [project or destination]` |
| Codex | select `android-software-house:android-company`, then describe the request |

Examples:

```text
I want an app that lets families share a weekly shopping list. Destination C:/Apps/ShopList
Add password reset to C:/Projects/MyApp
Check C:/Projects/MyApp against its approved design
continue
```

The director answers in your language. Phase entry points exist for narrower
requests: `idea`, `new-app`, `plan`, `design`, `build-feature`, `design-review`,
`review`, `test`, `security-audit`, `performance-audit`, `integrate-api`, `build`,
`release`, `publish`, `status`, `analyze`, `incident` (Codex prefixes them with
`android-`).

## The company

Seven departments and their roles are listed in
[the role catalog](references/agent-catalog.md): management (company director),
product (product manager, business analyst), design (UX designer, UI designer,
design-QA reviewer), engineering (architect, core, Compose, integration, backend
liaison), quality (QA, code review, security, performance), release (build and
release, store listing) and operations. Skills are listed in
[the skill catalog](references/skill-catalog.md).

## The lifecycle

`intake → discovery★ → requirements★ → design★ → architecture → build →
verification → design-qa → acceptance★ → release-prep → store-readiness →
publish★ → operate` (★ = your decision)

Each workflow uses the stages it needs ([workflows](workflows/catalog.json)).
`suite.py` enforces them ([company protocol](references/company-protocol.md)):

- A stage cannot start before its prerequisites are passed.
- Your decisions are recorded with the exact files you reviewed (their hashes).
  If one of those files changes later, that decision and everything after it is
  reopened.
- Gate stages pass only with real, current evidence: executed builds and tests,
  and an independent code review.
- A source change reopens verification, design QA and acceptance.
- The task is complete only when `suite.py lifecycle` says `"completed": true`.

## Design conformance

The design checkpoint approves the visual reference **and** a
`screen-inventory.json` listing every element of every screen and the screenshot
configurations that must be covered (both themes, RTL, wide window, large font).
After the build, Roborazzi records screenshots (`suite.py screenshots`), and the
design-QA reviewer, who did not build the screens, judges each element as
matched, deviation or missing. `suite.py conformance` fails if any screen or
element is unjudged or missing, a deviation lacks your approval, a screenshot or
configuration is missing or stale, or the reviewer built the screens. You accept
the app only with that report in front of you. Templates:
[inventory](templates/documentation/screen-inventory.json),
[conformance record](templates/documentation/conformance-record.json).

## Install

Claude Code, for one session or persistently from the repository root:

```sh
claude --plugin-dir ./android-software-house
claude plugin marketplace add .
claude plugin install android-software-house@android-software-house-marketplace --scope local
```

Codex, from the repository root:

```sh
codex plugin marketplace add .
codex plugin add android-software-house@android-software-house-marketplace
python android-software-house/scripts/install_codex_agents.py --project "C:/path/to/android-project"
```

The installer registers the company roles as native Codex agents in that
project, with a self-contained runtime copy. Use `--user` for personal agents,
`--dry-run` to preview, `--uninstall` to remove unchanged managed files.
Projects with the 1.x agents run `--migrate-legacy` once instead; it removes
only unchanged 1.x files and restores them if the new install fails. Details in
[host integration](references/hosts.md).

Requirements: Python 3.10+. Android builds need JDK 17 and the Android SDK;
`suite.py doctor --project <dir>` reports what is missing.

## Tooling

`python scripts/suite.py <command>`, standard library only:

| Command | Purpose |
|---|---|
| `inspect`, `doctor`, `status` | Read-only project, host and task state |
| `init`, `feature`, `api`, `wrapper` | Scaffold an app, feature module or API client; bootstrap the wrapper |
| `verify --execute`, `screenshots --execute` | Run builds, tests, lint and Roborazzi with captured exit codes |
| `lifecycle`, `checkpoint`, `advance`, `deviation` | Open and move a task through its stages |
| `inventory`, `conformance` | Validate a screen inventory; check a conformance record |
| `gate`, `audit-release`, `dependencies` | Evidence gates, release artifact and dependency audits |

`scripts/publish_play.py` submits an approved bundle to Google Play and defaults
to a dry run. Nothing is published without the publish checkpoint.

## What changed from 1.x

The 1.x plugin let an implementation pass every gate without matching the
approved design: its implementation gate checked compile, lint, tests and code
review only; the design was approved before implementation but never compared
afterwards; no role owned conformance; and the instruction generator had drifted
from the committed files. 2.0 adds the design inventory, the independent design-QA
role and gate, owner acceptance before completion, an enforced lifecycle, product,
store and operations roles, and a generator with a `--check` mode that fails on
drift.

## Development

See [the developer guide](docs/development.md), [known limits](docs/limitations.md)
and [the test report](docs/test-report.md).
