# Independent code review 4

- Date: 2026-11-07
- Source SHA-256: `e98f7779057d19d7b08d70331801c07be08d0a1623cecda603bc28783020320d`
- Result: **failed**
- Lifecycle effect: verification remains pending.

## Findings

1. **P1 — Supporting-pane blocked states can leak content in expanded layouts.** Render pane data only for `CONTENT/PARTIAL`, an empty surface for `EMPTY`, and a safe state panel for loading, denied, expired, offline, and error. Add a pane-state matrix that proves names are hidden.
2. **P2 — Source details can include a non-indexed source in answer scope.** Allow inclusion only for `INDEXED`; keep removal possible if an already-pinned source later becomes non-indexed.
3. **P2 — Message deletion capability is not applied to user messages.** Keep assistant-only actions separate, but expose conditional delete for both authors and test enabled/disabled behavior.
4. **P2 — Add source needs a typed initial choice and acquisition mode.** Add `IDLE` plus a typed `CHOICE/FILE/TEXT` mode, and render text fields/progress only for the selected mode and active operation.
5. **P2 — Send button has no localized TalkBack label.** Add explicit English/Arabic semantics and tests.

## Prior review status

All explicit findings from review rounds 1–3 were repaired in their original reproduction paths. This review found the remaining blocked-pane, capability, acquisition-state, and accessibility gaps above.
