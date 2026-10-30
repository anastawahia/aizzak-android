# UI decisions

Approved [UI design 0.2](../UI-DESIGN.md) and [HTML reference 0.2](../UI-PREVIEW.html) retain their reviewed bytes. Approval is in [FOUNDATION-01](tasks/foundation/status.md); their historical draft labels precede that decision.

Local slice implements shared theme, auth forms and pre-login preferences. Authentication submissions remain unavailable while services are absent. Home/Account content may be rendered in explicit previews/tests; production entry requires real session/verification/context in the later integration slice.

Targets: English initial language, Arabic/RTL, LTR sensitive/identifier inputs, fixed light/dark palettes, 48dp interactions, scrollable forms, adaptive width and scalable text. Device/TalkBack results must be recorded separately; an HTML preview or compiled test is not evidence of device execution.
