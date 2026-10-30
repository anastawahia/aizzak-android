# FOUNDATION-01 — Android foundation

Date: 2026-10-30 (Asia/Baghdad)
Stage: partial — local foundation implemented and automated checks passed; service/device integration remains blocked
Route: scoped new-app; approved design -> bootstrap -> preferences/shared UI -> authentication/session -> account/home -> verification/review.
Baseline: Git commit 0ed7686, clean working tree before this task. Repository contains design/API documents and installed specialist contracts, no Android app or wrapper.

## Human decisions

- Requirements: passed, inherited agreed scope V1-AGREED-SCOPE.md 1.1 and DESIGN-REVIEW.md. No new scope introduced.
- FOUNDATION-DESIGN-01: passed. User's exact decision in this chat on 2026-10-30: «اعتمد», in response to the packet listing Kotlin/Compose/Material 3, API 26, verification/context before Home and current UI/colors.
- Approved SHA-256: V1-AGREED-SCOPE.md `C805DEA51CBA54C54DE636852EA1FF0948DE63DDFB45D4934F0179ACF640683C`; TECHNICAL-DESIGN.md `6D186D62495917D269C64483991901290BBBBE58DC19182F2B16807A8E720D60`; UI-DESIGN.md `1F74B0A82357A560443045864F89CC27685A58AF2A6B3999E0EC702F7B51E7AB`; UI-PREVIEW.html `D20DC95814303301C5DFB0146F8EC25EA717D4ECCA64E28D5E4AC6E125F98D0C`.
- Source design files retain their historical review labels and approved bytes; this record records the later approval.
- PR: not_applicable; local delivery only, no push, release or publication authorized.
- User answered the pending questions: package `com.aizzak.android`; «غير جاهزة؛ تابع التأسيس والأجزاء المحلية» for Firebase/API. Namespace/application ID confirmed. Proceed with local foundation; real auth/backend integration remains explicitly deferred for missing inputs.

## Dependencies and ownership

| Task | Owner | Dependency | Acceptance | State |
|---|---|---|---|---|
| TASK-001 bootstrap | build-release-engineer | confirmed package ID | single :app, API 26/36, pinned tools, official verified wrapper, assemble/unit/lint evidence | passed for local slice |
| TASK-002 preferences/shared UI | android-core-developer (models/persistence/VM/JVM tests), compose-developer (host/screens/resources/UI tests) | accepted bootstrap | approved palette, English initial, Arabic/RTL, system/light/dark persistence | source completed; JVM checks passed; device behavior unverified |
| TASK-003 auth/session/account/home | relevant core/integration/Compose specialists | accepted UI contracts and Firebase/API inputs | no fake sign-in/context; session/verification gating, descriptive unavailable agents | blocked for real integration; local forms and stateless Home/Account preview content delivered |
| TASK-004 verification/review | build-release-engineer executes checks; android-code-reviewer independently reviews behavior/test coverage | stable source | actual tests/lint/build and severity-ranked independent review | passed for local machine/source gate; device and integration checks unverified |
| Coordination/docs | main coordinator | actual handoffs | preserve user files and approval hashes; evidence and honest blockers | completed for local slice |

## Current evidence and unresolved inputs

- suite.py init-memory: exit 0; creates missing notes only.
- suite.py doctor: exit 0; Java/Javac 17.0.19, Android SDK D:\SDK, platform android-36, build tools 35/36 present; wrapper absent. No build/device/test success yet.
- Official Android AGP 8.13 documentation checked: AGP 8.13.2 supports Kotlin 2.3, requires Gradle 8.13/JDK 17; API 36 supported. https://developer.android.com/build/releases/agp-8-13-0-release-notes
- Package response received: `com.aizzak.android`. Passed to build specialist for generation.
- Firebase Android configuration/test environment, absolute API base URL, token issuer and context/logout semantics unresolved. No invented service responses or credentials.
- Agent availability is native and authorized by the invoked orchestration skill; no global agent/config installations.
- build-release-engineer dispatched and its preparation accepted: inspected bootstrap templates/version profile, verified safe staging/collision-check strategy and official wrapper verification behavior. Generation/download/build have not run. Its report: [bootstrap-preflight.md](bootstrap-preflight.md).
- Subsequent bootstrap: scaffold generated in new staging and collision-checked into root; actual scaffold/configs inspected and accepted. Official wrapper bootstrap passed (`evidence/wrapper-bootstrap.json`), distribution download/checksum then completed during initial build. Build still in progress; no success inferred. Build files owned by build specialist, src/main handed to Compose/core owners with disjoint paths.
- Core stable contract accepted: immutable AppPreferences/ThemePreference, PreferencesRepository Flow, DataStore adapter, SettingsViewModel with serialized writes and independent read/write error recovery. Source plus 10 meaningful JVM tests authored; [handoff](local-state.md). Tests not executed yet.
- Native independent reviewer dispatched against stable core/build source; final UI review pending completion of Compose handoff. No independent review success inferred yet.
- Compose source handoff accepted: [local-ui.md](local-ui.md). Signed-out-only typed routes, persisted settings wiring, approved logo/palette, localized fields/resources, Home/Account stateless preview content, three authored UI regressions. UI owner ran diff whitespace check; no device execution.
- Independent source review found one P2 system-bar/app-theme mismatch; returned to Compose owner and verified fixed by reviewer. Source review passed; [review.md](review.md). A later one-line selectable overload compiler fix is under scoped re-review.
- First actual build diagnostic failed compilation (exit 1): SettingsScreen positional selectable arguments matched the wrong overload. Retained `evidence/bootstrap-build.json` with first failure. UI owner corrected to named arguments. Baseline report fingerprint predates final source and is not used as current success evidence. Fresh build/unit/lint suite running against frozen corrected source, then Android test compilation.
- First full suite then compiled the APK successfully but failed one of 10 JVM tests (9 passed, 1 failure, no errors/skips); lint was not reached. Original evidence retained as `evidence/foundation-all-attempt1.json`. Failure: Windows replacement rename in Android DataStore FileStorage under the JVM's SDK_INT=0 stub during the second persistence write. Core/build specialists inspected official library source: the actual supported API26+ Android path uses NIO move; a dependency upgrade alone would not fix the host branch. Approved scoped correction: configure public cross-platform OkioStorage for real-file JVM repository tests only, keep all multiple-write/recreation assertions, leave production DataStore default and pinned dependency unchanged. This tests repository persistence with real host files; Android's default storage path still requires device testing.

## Final automated execution

- `evidence/foundation-all.json`: exit 0; assembleDebug + testDebugUnitTest + lintDebug, 149.64s. Ten tests passed, zero failures/errors/skips. Lint: zero errors, 20 warnings. Warnings retained without suppressions: 16 version-update notices, API33 localeConfig attribute with AppCompat fallback, Android12 dataExtractionRules recommendation, unused scaffold icon, approved original logo launcher-shape recommendation.
- `evidence/foundation-ui-compile.json`: exit 0; assembleDebugAndroidTest, 26.219s. Three authored Compose test cases compiled, NOT executed.
- Both final reports fingerprint `3ce87ead9e99e6aae7c9d335e96a53847efe69e05ac7f925d1bc1ce30b33a9b8`; coordinator confirmed with both installed plugin and native snapshot fingerprint functions.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`, application ID com.aizzak.android.debug, 14,156,461 bytes, SHA-256 `f47e66ca555b5363d22605d0bc48bd2aa43c75722316a62c824c5f7dada1da8f`.
- `evidence/device-inventory.json`: doctor --devices exit 0, no online devices/emulators. Existing build-specialist inventory also found no AVDs. UI execution, actual locale restoration/recreation, TalkBack, font/resize/keyboard/bar appearance and Android default storage remain unverified.
- No service config, real identity, account context, release signature, publication, commit or push is claimed. Missing integration inputs were explicitly accepted by user as reason to continue local parts.
- Final independent reviewer accepted current fingerprint after comparing every reviewed source/config hash (unchanged) and retaining a complete input manifest. Earlier reviewer fingerprint discrepancy has no proven cause; the final reviewed, executed and coordinator-calculated fingerprints all match. Final review SHA-256 `98b200ec0963fdf9f0b282f3758f8211bc3b4a09e9eee9a9bb859ec12fc5d529`.
- Machine local implementation gate: `suite.py --report <root>/.android-suite/evidence/implementation-gate.json gate --project <root> --stage implementation --evidence <root>/.android-suite/evidence/implementation-gate-input.json` returned exit 0, passed, no errors. Input links exact successful execution and independent review hashes. This scoped gate does not prove device or real integration acceptance, and no exception converted a failed check to success.

## Next action

Local delivery and machine evidence gate are complete. Resume real integration when Firebase Android configuration for the chosen variant plus API test URL/identity semantics are supplied. Device UI/accessibility acceptance needs an available Android target. No repeated requirements/design approval is needed for unchanged approved scope. Never add a fake identity or production bypass to compensate for missing services. PR/release/publication remain outside this delivery.
