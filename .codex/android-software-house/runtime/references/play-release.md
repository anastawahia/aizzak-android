# Google Play release runbook

This is a version-sensitive checklist, not a promise of current eligibility.
Re-read official sources at release time and record URL/date/app class/account
constraints in `.android-suite/releases.md`. If sources are unavailable, record
the policy check as blocked rather than asserting old target/testing rules.

## Prepare a reviewable release

1. Confirm application ID, namespace, versionName and monotonically increasing
   versionCode against the installed/store version. Freeze the source fingerprint.
2. Check current [target API requirements](https://developer.android.com/google/play/requirements/target-sdk)
   for the actual device classes and distribution dates. A template target is not
   a policy guarantee. Check [16 KB page size support](https://developer.android.com/guide/practices/page-sizes)
   if native libraries or SDKs ship in the bundle.
3. Complete required compile, static, unit, integration and device checks. Resolve
   critical security findings and review the merged release manifest/permissions.
4. Build AAB with the project's wrapper. Upload signing material lives outside the
   repository and comes from an approved provider. Verify the AAB JAR signature
   with jarsigner, record SHA-256, and compare its certificate to the expected
   upload certificate. Play App Signing's serving certificate differs from the
   upload certificate. See [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756).
5. Complete name/descriptions, localized listing, icon, feature graphics,
   screenshots, contact details and [privacy policy/Data safety](https://support.google.com/googleplay/android-developer/answer/10787469).
   Verify all included SDK data collection/sharing, retention/deletion and consent.
6. Complete content rating, ads/target audience declarations, app access review
   credentials through approved channels, permission declarations and relevant
   policy forms. Never store reviewer passwords in shared project memory.
7. Check [testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465)
   for this account, including internal/closed tracks, tester eligibility and
   production access. Do not hardcode a tester/day threshold as universal.
8. Document track, countries, rollout fraction, monitoring budget and stop
   conditions. A rollback may need a forward fix with a higher versionCode and
   compatible data schema; it is not always possible to reinstall an old bundle.

## Submission and authorization

The provided `scripts/publish_play.py` is an optional real Google Play Developer
API client. Default execution is a local dry-run with no network writes. It does
not create a Play app, listing, account, privacy declarations, credentials or
production eligibility. First-time setup/review may require Play Console.

Use a short-lived OAuth access token with Android Publisher scope from an approved
provider. Set `PLAY_ACCESS_TOKEN` in the process environment; never print it or
pass it as a CLI argument. Required Play permissions must already exist.

Prepare and present the exact bundle/package/versionCode/track/rollout and release
gate record before seeking explicit human authorization. Record it only after
the user grants it, with this schema (values shown here are field descriptions,
not an executable approval):

```text
action: commit-google-play-edit
package: exact application ID
track: exact existing Play track ID
sha256: exact bundle SHA-256
version_code: integer from the actual bundle
rollout: fraction in (0, 1]
approved_by: human identifier
approved_at: integer UTC Unix timestamp within the last hour
authorization_reference: actual message/change-approval reference
```

Save the actual values as JSON outside shared/private-secret output. The file is
an audit record and accidental-publication guard, not an authentication system.
Never create it to bypass a missing approval. `--execute` also requires a current
release gate record and verified bundle signature. The `human_approval` evidence
must reference the actual authorization.

The client creates an edit, uploads and verifies bundle SHA/versionCode, updates
the exact track, validates and commits once. Failed uncommitted edits are discarded
where possible. It has no automatic retry for mutations. If commit times out,
inspect Play Console before any retry: a request may have succeeded remotely.
API commit indicates submission; it does not establish review approval or public
serving. Record actual API IDs and confirm Console serving/rollout state before
claiming publication. No listing policy declarations are submitted by this client.

See the official [edits API](https://developers.google.com/android-publisher/api-ref/rest/v3/edits),
[bundles upload](https://developers.google.com/android-publisher/api-ref/rest/v3/edits.bundles/upload),
[tracks](https://developers.google.com/android-publisher/api-ref/rest/v3/edits.tracks)
and [staged rollouts](https://support.google.com/googleplay/android-developer/answer/6346149).

## Observe and maintain

Monitor Crashlytics or the configured alternative, Play Android vitals, crash/ANR
rates, adoption and backend health by version/track. Establish thresholds from
the product's baseline before rollout. Verify deobfuscation mapping/symbol upload
for the exact bundle. Changes to production rollout require explicit approval.
Record incidents, regression tests, mitigations and confirmed recovery.
