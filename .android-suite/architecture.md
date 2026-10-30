# Architecture and decisions

ADR-001: Approved [technical design 1.1](../TECHNICAL-DESIGN.md), approval recorded in [FOUNDATION-01](tasks/foundation/status.md).

Single :app module, Kotlin 2.3.0, Compose/Material 3 and AppCompat Activity. UI observes a lifecycle-aware settings ViewModel; PreferencesRepository exposes immutable preferences through Flow, DataStore adapter owns local persistence. AppCompat/platform owns the selected locale; DataStore stores only theme and locale-initialization metadata. Constructor injection and a small application container, no Room/Hilt needed for this slice.

Fixed palette mirrors approved UI tokens. Typed Navigation 3 keys own local auth/preferences back navigation. No service SDK calls or network operations inside composition. Missing service configuration does not produce a locally authorized session.

Bootstrap pins AGP 8.13.2, Gradle 8.13, JDK 17, API 26/36; checked official compatibility: https://developer.android.com/build/releases/agp-8-13-0-release-notes . This reproducible baseline is not a claim of latest versions. Actual dependency/build results are recorded under evidence, independently of version selection.
