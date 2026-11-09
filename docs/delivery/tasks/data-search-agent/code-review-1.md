# Independent code review 1

- Date: 2026-11-07
- Role: `android-code-reviewer`
- Scope: Data Search Android implementation only
- Result: **failed**
- Lifecycle effect: verification remains pending; the implementation must be revised and reviewed again.

## Findings

1. **P1 — Interactions are not usable.** `DataSearchContent` exposes no action contract, while navigation, send, delete, copy, and text input callbacks are empty. Add hoisted state and a typed `DataSearchActions` contract, then verify user input and actions.
2. **P1 — Preview data can appear in safe or empty states.** Production defaults and missing selections fall back to realistic fixture names such as `policy.pdf`. Make selections nullable/typed, remove fixtures from production defaults, and render an explicit safe state.
3. **P1 — Mutually exclusive states render together.** Content, empty, pagination, and permission/error notices can be visible simultaneously. Model per-screen state explicitly and render one valid branch at a time.
4. **P2 — The 840dp three-pane layout makes the conversation pane narrower than both side panes.** Use a proportional layout or a breakpoint/minimum-width policy that keeps the conversation pane widest.
5. **P2 — Assistant content is plain text only.** Add an accessible safe renderer for the approved Markdown, code, and link behavior.
6. **P2 — Tests assert tag presence rather than behavior and state correctness.** Add interaction tests, mutually-exclusive state tests, the required loading/empty/error/offline/permission/session/partial matrix, and an expanded-layout assertion at 840dp.

## Constraints confirmed

- No backend changes were made.
- Home, navigation, and live network integration remain disabled pending an approved backend contract.
- This review is not a design-conformance review.
