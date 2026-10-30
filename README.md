# AIZZAK

Native Android foundation for AIZZAK, using Kotlin, Jetpack Compose and Material 3.
Phone/tablet support starts at Android 8.0 (API 26), with compile/target API 36.
Application ID: `com.aizzak.android`; debug installs use `com.aizzak.android.debug`.
The reviewed scope and design are linked from [DESIGN-REVIEW.md](DESIGN-REVIEW.md).
The later approval and delivery evidence are recorded in [.android-suite/tasks/foundation/status.md](.android-suite/tasks/foundation/status.md).

1. Use Android Studio with JDK 17 and Android SDK 36.
2. Use the checked-in Gradle 8.13 Wrapper, whose distribution checksum is pinned.
3. Run `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` (Windows: `gradlew.bat`).
4. Run `:app:connectedDebugAndroidTest` on an authorized API 26+ device.
5. Optional source analysis: `:app:detekt`; review Kotlin parser support before enabling as a release gate.

Release builds require an external upload keystore and the four `ANDROID_KEYSTORE_*`
and `ANDROID_KEY_*` environment variables named in `app/build.gradle.kts`.
Do not add passwords to Gradle properties. `bundleRelease` never publishes.
Firebase Android configuration and an approved API environment have not been supplied.
Successful authentication or account context must not be inferred from local foundation UI.
Agent operations, workspaces and conversations remain deferred by the agreed scope.

Pinned baseline: AGP 8.13.2, Kotlin/Compose compiler 2.3.0, Compose BOM 2026.02.01,
Activity 1.12.4, Lifecycle 2.10.0, Coroutines 1.10.2, AppCompat 1.7.1,
DataStore 1.1.7 and Navigation 3 1.0.1. This is a reproducible compatibility
baseline; dependency resolution and verification outcomes are recorded separately.
