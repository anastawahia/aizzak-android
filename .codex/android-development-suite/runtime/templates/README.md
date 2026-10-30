# Templates

`suite.py init` renders `android-project/` into a new directory. `feature` renders
`feature-module/`; integrate it with the host settings/catalog and navigation as
described in the emitted INTEGRATION.md. `api` renders `api-integration/` after
validating its supported OpenAPI subset. Each renderer refuses existing targets,
invalid identifiers, path traversal and unresolved `@@VARIABLE@@` tokens.

`compose-screen/Screen.kt.tmpl` is a small state-rendering pattern to adapt within
the project's theme. It is not a stand-alone module. Render PACKAGE and FEATURE
tokens using `render_tree` or deliberately adapt the source. Strings are passed
from localized resources by its caller.

Copy/adapt `github-actions/android.yml` and optional `release.yml` into a generated
app's `.github/workflows/` after reviewing runner policy and action revisions.
They are intentionally not installed in the plugin repository as Android CI jobs.
No CI template publishes. `documentation/` supplies incremental project memory.

The starter uses a pinned compatible API 36 toolchain profile. Feature templates
target the same profile; align their compileSdk/minSdk with an existing host.
Do not mechanically apply template versions to existing projects. Add domain or
repository abstractions only when behavior/data dependencies justify them.
