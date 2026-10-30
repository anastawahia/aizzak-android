# TASK-002 — Local preferences and lifecycle state

Owner: android-core-developer. Date: 2026-10-30 (Asia/Baghdad).
Status: source/test implementation ready; execution pending shared build owner's verification.
Scope: V1-THEME-01, locale initialization portion of V1-LOCALE-01, settings error/lifecycle portions of V1-QUALITY-01..02.

## Files and accepted interfaces

- `app/src/main/java/com/aizzak/android/core/model/AppPreferences.kt`: `ThemePreference` with explicit stable keys (`system`, `light`, `dark`) and `AppPreferences(theme, localeInitialized)`.
- `app/src/main/java/com/aizzak/android/data/preferences/PreferencesRepository.kt`: immutable `Flow<AppPreferences>`, suspending theme save and locale-initialization marker.
- `app/src/main/java/com/aizzak/android/data/preferences/DataStorePreferencesRepository.kt`: injected `DataStore<Preferences>` plus application-level `Context.aizzakPreferencesDataStore` delegate; no identity/token/locale-string storage.
- `app/src/main/java/com/aizzak/android/feature/settings/SettingsViewModel.kt`: `uiState: StateFlow<SettingsUiState>`, theme/initialization writes, independent read/write failure flags, retry and notification dismissal. Null preferences distinguish failed initial load from real default preferences. Last confirmed preferences remain visible after failed writes. Queue serializes rapid writes; lifecycle cancellation propagates.
- `app/src/test/java/com/aizzak/android/data/preferences/DataStorePreferencesRepositoryTest.kt`.
- `app/src/test/java/com/aizzak/android/feature/settings/SettingsViewModelTest.kt`.

`SettingsUiState` includes `loading`, nullable `preferences`, `saving`, `readFailed`, `writeFailed`, and `errorDismissed`; its computed `error: SettingsError?` uses READ/WRITE. Dismissal hides a notification without deleting retryable failure state. Failed theme and locale-initialization writes remain independent, so unrelated successful writes cannot falsely report recovery.

Locale value remains solely Android/AppCompat-owned. The UI owner must initialize English only when no explicit locale has been restored and the first-run marker is false, then save the marker. These files neither change locale directly nor fabricate authentication/session/backend behavior.

## Verification scope

Ten authored JVM test cases: empty defaults; real DataStore file/store recreation persistence; unknown future theme compatibility without rewriting; IO error propagation; initial read retry with no fake defaults; failed save retention/retry; serialized rapid theme writes; independent read/write errors with unrelated locale success; dismissal retaining retry; ViewModel clearing cancelling pending write.

Read baseline: approved technical/UI documents, Git status, native host/Kotlin/state/local-persistence/unit-testing contracts, actual shared Gradle catalog. Independent role owns build configuration, wrapper and Gradle execution. No Gradle command, compile, JVM test, device check, or lint has been executed by this role; generated tests are not a passing test result. Build owner/coordinator must append actual execution evidence.

No Gradle/resources/MainActivity/design files modified by this role. No commits, pushes, publication, dependency installation, auth integration, Room or migrations.

## Verification failure and bounded correction

Shared first suite run `.android-suite/evidence/foundation-all-attempt1.json` (preserved by build owner) compiled/packaged the app but failed the durable recreation test: 10 tests, 9 passing, 1 failing, no errors/skips. The second real-file update failed replacing the existing file on Windows. No test was disabled and no persistence assertion was removed.

Official AndroidX Maven source archives for `datastore-core-android` 1.1.7 and 1.3.0 were read directly: both select the old `File.renameTo` implementation when `Build.VERSION.SDK_INT < 26`. Android JVM test stubs have SDK_INT zero; upgrading alone therefore does not fix this Android-variant host path. This is not evidence that the min-26 Android production path fails. The official 1.1.7 preferences source also confirms `createWithPath` delegates to the same FileStorage factory, so that overload alone would not correct this environment mismatch.

The coordinator accepted the focused correction: JVM persistence cases use public `PreferenceDataStoreFactory.create(storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer), scope = ...)` through a small host factory. It still uses a real DataStore, official serializer, real filesystem, both updates, orderly scope close and full reopen assertions. Production `preferencesDataStore` remains unchanged. These tests establish repository/serialization persistence on host storage, and do not establish Android default-storage/device behavior. No dependency upgrade or mocked SDK field was introduced. Second shared execution remains pending at the time of this correction.

Primary references:

- [Official failure issue 203087070](https://issuetracker.google.com/issues/203087070).
- [AndroidX DataStore release notes](https://developer.android.com/jetpack/androidx/releases/datastore): temp-file fix is listed under 1.3.0-alpha03; Android branch inspection remains necessary for host tests.
- [Official 1.1.7 Android core source archive](https://dl.google.com/dl/android/maven2/androidx/datastore/datastore-core-android/1.1.7/datastore-core-android-1.1.7-sources.jar), `FileMoves.android.kt`.
- [Official 1.1.7 preferences source archive](https://dl.google.com/dl/android/maven2/androidx/datastore/datastore-preferences-core-android/1.1.7/datastore-preferences-core-android-1.1.7-sources.jar), public factory and serializer contracts.
- [Official 1.1.7 Okio storage source archive](https://dl.google.com/dl/android/maven2/androidx/datastore/datastore-core-okio-jvm/1.1.7/datastore-core-okio-jvm-1.1.7-sources.jar), public cross-platform `OkioStorage`.

## Next handoff

Compose owner consumes these interfaces; build owner runs shared suite build/unit/lint after source stabilizes. Address actual compiler/test findings within these owned files, then independent review. Firebase/API integration remains deferred under the user's explicit local-only instruction.
