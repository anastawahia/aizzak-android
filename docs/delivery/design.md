# UI decisions

يبقى [تصميم الواجهة 0.2](../design/UI-DESIGN.md) و[مرجع HTML 0.2](../design/UI-PREVIEW.html) المرجعين الحاكمين المعتمدين في [FOUNDATION-01](tasks/foundation/status.md). احتفظ مرجع HTML بالبايتات والبصمة المعتمدين؛ أما مواصفة الواجهة فتغير فيها رابط نطاق المنتج فقط عند نقل التوثيق إلى بنية `docs/` الحالية، من دون تغيير رموز التصميم أو القرار المعتمد. تسميات المسودة التاريخية في الملفين تسبق قرار الاعتماد.

أعيد فحص اتساق اللوحة في 2026-11-06: تطابقت أدوار اللون العشرون في مرجع HTML مع مواصفة الواجهة وعقد الرموز وثيم Compose، ولم يلزم تغيير أي قيمة. يسجل [تحقق مزامنة الألوان](tasks/ui-preview-color-sync/status.md) الفرق غير اللوني الناتج عن نقل مسارات التوثيق والبصمات الحالية.

Local slice implements shared theme, auth forms and pre-login preferences. Authentication submissions remain unavailable while services are absent. Home/Account content may be rendered in explicit previews/tests; production entry requires real session/verification/context in the later integration slice.

Targets: English initial language, Arabic/RTL, LTR sensitive/identifier inputs, fixed light/dark palettes, 48dp interactions, scrollable forms, adaptive width and scalable text. Device/TalkBack results must be recorded separately; an HTML preview or compiled test is not evidence of device execution.
