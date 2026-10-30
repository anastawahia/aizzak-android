# TASK-002 — Local Compose foundation

Owner: compose-developer. Date: 2026-10-30.
Status: source implemented; compiler/device verification pending coordinator-owned execution.

## Delivered source and boundaries

- `app/src/main/java/com/aizzak/android/MainActivity.kt`: AppCompat host; lifecycle-aware SettingsViewModel collection; first-run English initialization only when preferences load and no existing explicit application locale exists; platform/AppCompat locale authority thereafter. Read/init-save failures display retry, not fictitious loaded preferences. Effective saved/system theme controls status/navigation icon appearance through `SystemBarStyle.auto` in a SideEffect.
- `core/designsystem/{AizzakTheme,Components}.kt`: fixed approved light/dark semantic palette, shared form layout/brand/notices/headings.
- `navigation/LocalFoundationApp.kt`: typed serializable Navigation3 keys for sign-in/sign-up/reset/preferences only. Authenticated Home/Account are deliberately absent from this local navigation until actual session authorization is implemented. Back from Preferences restores the source page.
- `feature/auth/AuthScreen.kt`: localized, scrollable local forms, explicit unavailable notice and disabled submission with readable semantic disabled colors. Passwords and visibility state use non-saveable `remember`; no saved instance state, storage, network or fake success.
- `feature/settings/SettingsScreen.kt`: theme and language choices, radio semantics, saving/error/retry, actual package version. Theme persistence uses the core owner's repository/ViewModel. Locale choices call AppCompat only; locale text is not duplicated in DataStore.
- `feature/home/HomeScreen.kt`: stateless list-driven unavailable assistant cards with adaptive 1/2/3 columns; presentation IDs are not backend keys. No click action. Empty lists stay empty. Previews include Arabic/large text.
- `feature/account/AccountScreen.kt`: stateless genuine optional identity fields, LTR email, actions as callbacks; sample email exists only in a clearly labelled preview. No production account route.
- `res/values{,-ar}/strings.xml`, `xml/locales_config.xml`, manifest: Arabic/English resources, RTL support, AppCompat automatic locale persistence metadata; no permissions/network/configuration data added.
- `res/drawable-nodpi/applogo.png`: byte-identical approved original. SHA-256 `119462BB78EB240A65C869FC067EE599639B3CB5A41953F25C07B17D2A8C7E0F`. Used for brand and initial launcher icon without redrawing/recoloring.

## Verification source

`app/src/androidTest/java/com/aizzak/android/LocalFoundationUiTest.kt` contains three interaction/semantics regressions: unavailable authentication cannot enter Home and Preferences returns to source; theme/language dispatch without authentication; all three deferred cards have no click action. They test local view/navigation boundaries; they do not prove AppCompat locale persistence, process recreation, TalkBack or network behavior.

`git diff --check` executed with exit 0 (existing line-ending warning for coordinator's DESIGN-REVIEW.md). Original/copied logo hash matched. No Gradle or device run performed by this owner, because build engineer owns serialized execution. Compiler/test/lint/device outcomes must be supplied by the actual run, not inferred here. Android tests currently use English text expectations and should run on the isolated English test device/configuration.

## Remaining verification / handoff

Source ready for coordinator/build-engineer compile, unit/lint, Android test compilation and device execution if available. Parent reported independent review identified system-bar forced-theme mismatch; corrected as above. Need reviewer confirmation and real checks. Real authentication/session/context/account services remain deferred by the user's explicit unavailable Firebase/API inputs. No PR, push, publishing or global configuration performed.
