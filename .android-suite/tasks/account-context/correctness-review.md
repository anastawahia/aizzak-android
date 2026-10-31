# ACCOUNT-CONTEXT-01 — Correctness review

Date: 2026-10-31. Independent reviewer: `/root/android_correctness_review`.

The independent source review reported one P2 in `scripts/connect-local-api.ps1`: a single readiness probe could fail during normal Compose startup. No other actionable product finding was reported in that review.

The coordinator replaced the single probe with bounded readiness polling: both health endpoints must return 200 before ADB forwarding, a 60-second deadline, three-second individual requests, and one-second retries after transient errors. Current script SHA-256: `4C46C744F3ECB54965F3A0C7EEB804AE609ACBD136772A9B787FC8C09A742792`.

Coordinator verification completed for the healthy path and a PowerShell mock with two initial HTTP 503 exceptions followed by success; the latter made four probes and completed ADB setup. These are coordinator checks, not an independent re-review. The requested follow-up independent review did not complete because delegated execution became unavailable. The separate independent security review did complete and passed after its corrections; see [security-review.md](security-review.md).

No claim of real-account success follows from these source reviews or fake-service tests. Current runtime and device execution are recorded separately.

## Completed independent follow-up

On resumption native `/root/final_scoped_review` independently re-reviewed the readiness fix and the test-only locale repair. Result: passed, no actionable findings. This supersedes the earlier unavailable follow-up above. The reviewer confirmed against locally cached official Compose UI 1.10.4 sources that `stringResource` uses `LocalResources`, derived from `LocalConfiguration` and `LocalContext.resources`; the helper supplies both without changing device/application locales. Assertions and production gates remain intact. Reviewer changed no files and ran no Gradle/device checks. Scoped diff check exited 0.

| Reviewed source | SHA-256 |
|---|---|
| scripts/connect-local-api.ps1 | 4C46C744F3ECB54965F3A0C7EEB804AE609ACBD136772A9B787FC8C09A742792 |
| EnglishTestContent.kt | BD183A4D68FCDBDE8554AB95841F49D835AD6BA090043213B08D91EB1F22C4B4 |
| AccountContextUiTest.kt | 22D9881A4415852C524B25E05A3BD2B634807D3F6DE3AEBD8931D6C9B969602F |
| AuthUiTest.kt | C50A01AEFFA8472A263E0125562403DE044B3AA1F84B942F0602A21B34D269D4 |
| LocalFoundationUiTest.kt | 105D2AC4D502716C93244E40B687689175AF80AD27D16F9483919AEC6DA60049 |
