# Vibrant palette 0.2 — independent artifact QA

Date: 2026-10-30. Task: `VIBRANT-PALETTE-02`. Scope: `V1-THEME-01`, `V1-QUALITY-01..02`; design artifacts only.

Result: **passed for the executed static checks**. No actionable findings. This is not browser, Android, device, layout or accessibility certification.

## Reviewed fingerprints

| Artifact | SHA-256 |
|---|---|
| UI-PREVIEW.html | `D20DC95814303301C5DFB0146F8EC25EA717D4ECCA64E28D5E4AC6E125F98D0C` |
| UI-DESIGN.md | `1F74B0A82357A560443045864F89CC27685A58AF2A6B3999E0EC702F7B51E7AB` |
| Original and embedded applogo.png | `119462BB78EB240A65C869FC067EE599639B3CB5A41953F25C07B17D2A8C7E0F` |

## Executed checks

An inline Python command read the actual HTML/CSS/specification, compared hashes before and after QA, decoded the embedded logo, calculated sRGB contrast and invoked `node --check` with the extracted JavaScript on UTF-8 stdin. Final Python command exit: **0**. `node --check` exit: **0**.

- All 20 semantic color rows match the CSS custom properties in both themes: 40 matching values.
- Original logo bytes match the embedded image exactly.
- 32 text-role comparisons pass 4.5:1. Range: **4.916:1–14.648:1**. Includes body and muted text on background/surface/soft, primary links on background/surface/hover soft, primary button/nav foreground, selected-row text, selected-hover text, error text and three accent/container pairs.
- 18 interactive-indicator comparisons pass 3:1. Range: **3.288:1–7.791:1**. Includes field/radio outlines on background/surface/hover soft; focus rings on those surfaces; selected radio primary on accent; primary on hover soft and surface. Decorative card dividers are not treated as interactive indicators.
- Static inspection confirms English/follow-system defaults, Arabic RTL assignment, all 10 views, theme controls, and noninteractive agent articles with the deferred-state label.
- No external script, stylesheet, import, HTTP URL, fetch, XMLHttpRequest or WebSocket reference found in the preview.
- Both reviewed file hashes remained unchanged throughout the completed check.

## Limits

No browser or layout execution, screenshots, large-font rendering, TalkBack, Compose semantics, Gradle build or device test was performed. Prior recorded local-browser access restriction was respected; no alternative browser/server was used. Human visual review remains pending. No implementation/specification files were modified by QA.

Two preliminary QA-command failures were checker issues, corrected before the passing run: expected table-row count was 21 instead of the actual 20; Windows subprocess text encoding defaulted to cp1252 rather than UTF-8. Neither failure indicated a defect in the design artifacts.
