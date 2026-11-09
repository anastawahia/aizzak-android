# Independent code review 2

- Date: 2026-11-07
- Source SHA-256: `bff6c2cb690a3b1af415e43c41ce77e4e1742a67fdbf40e73cbd1e85efe2c7ef`
- Result: **failed**
- Lifecycle effect: verification remains pending.

## Findings

1. **P1 — Destination-specific empty states lost their primary actions.** Conversation empty must retain its header, scope, and composer; conversation-list empty must retain Start conversation; sources empty must retain Add source.
2. **P1 — A stale source ID falls back to the first source.** Never redirect an action to a different item. Keep an explicit pending target, disable confirmation when it is missing, and pass its ID to the final callback.
3. **P2 — Conditional actions do not fully respect confirmed capabilities and item state.** Show Index/Retry according to `AnswerIndex`, hide unconfirmed Add text, and add a dedicated message-delete capability.
4. **P2 — Add-source progress is hard-coded.** Add a typed upload phase with terminal and cancellable behavior, and derive the indicator and actions from it.
5. **P2 — Selected sources only is a silent radio action.** Wire it to an explicit scope change or render it as non-interactive content.

## Previously reported findings

Production preview-data leakage, contradictory state rendering, 840dp pane proportions, safe Markdown rendering, and broad test coverage were repaired. Interaction wiring was substantially repaired but retained the no-op scope action above. New tests are required for stale selections, destination-specific empty actions, upload phases, and conditional action visibility.
