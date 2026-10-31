# AIZZAK

Native Android foundation for AIZZAK, using Kotlin, Jetpack Compose and Material 3.
Phone/tablet support starts at Android 8.0 (API 26), with compile/target API 36.
Namespace and application ID: `android.aizzak.com`, matching the supplied Firebase Android client.
Debug and release use this same application ID; no debug suffix is applied.
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
Firebase Android configuration is supplied in `app/google-services.json`; the approved debug API environment is the local WSL development stack.
Enable Email/Password in Firebase Authentication for this project. The app uses Firebase Authentication
for sign-in, account creation, email verification and password reset; Firebase manages session persistence.
After verified authentication, the app loads `/api/v1/me/context` before opening Home and Account.
An unavailable or denied context keeps protected content closed and offers retry or signout.
Ordinary signout clears the local Firebase session and account context. It does not call
the current backend `/me/logout`, whose master implementation temporarily blocks the
Firebase subject across all devices and fresh logins. The HTTP logout adapter remains
dormant pending a separately approved backend contract; see
[backend reply](.android-suite/tasks/account-context/backend-reply.md).
Authentication tests use isolated fakes and do not create real Firebase accounts.
Agent operations, workspaces and conversations remain deferred by the agreed scope.

For a USB-connected debug device, run `./scripts/connect-local-api.ps1` from PowerShell.
This starts the existing WSL `Ubuntu-24.04` `/home/AIZZAK` nginx/application dependencies,
checks Windows access to the local API, then forwards device port 8080 to host port 80 with ADB.
Use `-SkipStackStart` when those services are already running. Re-run after a device reconnect/reboot.
The debug API URL is `http://127.0.0.1:8080/api/v1/`; its cleartext exception is limited to
that loopback host. Keep the trusted development machine and USB connection available while testing.
Release requires an explicit HTTPS `AIZZAK_API_BASE_URL` Gradle property or environment variable;
no production address or permissive certificate trust is supplied by the local setup.
The scoped integration and logout semantics are recorded in
[ACCOUNT-CONTEXT-01](.android-suite/tasks/account-context/status.md).
Backend files/configuration are owned by its developer. Record needed changes as Markdown
requests in the Android task directory; do not modify the backend from this project.

Pinned baseline: AGP 8.13.2, Kotlin/Compose compiler 2.3.0, Compose BOM 2026.02.01,
Activity 1.12.4, Lifecycle 2.10.0, Coroutines 1.10.2, AppCompat 1.7.1,
DataStore 1.1.7 and Navigation 3 1.0.1. This is a reproducible compatibility
baseline; dependency resolution and verification outcomes are recorded separately.
