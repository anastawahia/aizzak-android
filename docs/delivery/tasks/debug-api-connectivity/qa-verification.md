# CONN-01 QA verification — 2026-11-05

Status: passed for current-source build, JVM tests, Android Lint, host readiness and ADB forwarding.

## Executed evidence

- `suite.py --report docs/delivery/evidence/debug-api-connectivity-all.json verify --project <Android root> --kind all --execute --timeout 900`
  - Exit: 0.
  - Source SHA-256: `4b98eb7c1dffc2d528f0d38d03387fcf96e09e1ac3cc63c5bf74b3ffaee6bcc8`.
  - Debug assembly passed.
  - JVM: 96 tests, 0 failures, 0 errors, 0 skipped.
  - Android Lint: 0 errors, 36 warnings.
  - Debug APK SHA-256: `0aa914aafb1c3f3514f1c41cc450ce9fa05cb4a7e369a953e156cb6b3afa0017`.
- Read-only environment recheck:
  - `GET http://127.0.0.1/health` returned 200.
  - `GET http://127.0.0.1/health/ready` returned 200.
  - Connected device: Samsung SM-A155F.
  - `adb reverse --list` reported `UsbFfs tcp:8080 tcp:80`.
- Focused regression `environmentRejectsReleaseHttpNonloopbackAndCredentialUrls` executed in `HttpAccountContextRepositoryTest`: passed. The class ran 13 tests with zero failures/errors/skips.
- Android source/scripts remain clean in `git status --short app scripts`.

## Configuration boundary inspected

- Debug network policy denies cleartext by default and permits only the exact host `127.0.0.1` without subdomains.
- Main/release network policy continues to deny cleartext by default.
- The existing environment regression rejects release HTTP, debug LAN HTTP and debug `localhost` while accepting the explicit debug loopback.

## Live journey and limitations

The director separately observed the repaired live journey on the connected phone: after restoring ADB reverse and pressing Retry, the app displayed Home / Your assistants / Account / Settings. QA did not reinstall the APK, clear app data, submit credentials, or modify backend state.

`--kind all` does not execute connected instrumentation tests. Independent code review remains a separate requirement before advancing the implementation gate. No design-conformance, release signing or publication result is claimed.
