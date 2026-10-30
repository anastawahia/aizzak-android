# Known issues

Integration input blocker: Firebase Android environment and API URL/identity semantics unavailable, confirmed by user. Owner: project owner/backend configuration; affects real V1-AUTH and V1-ACCOUNT acceptance. Local foundation may proceed with unavailable actions and no simulated session.

Device verification limitation: build engineer's adb inventory found no connected devices; emulator inventory found no AVDs. UI tests may be authored/compiled, but device behavior, TalkBack and screenshots are unverified until a device is available.

Build/lint/test/review findings must be linked from [active status](tasks/foundation/status.md) after execution; this file does not assert a clean audit.
