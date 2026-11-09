# دليل التنفيذ والتسليم

هذا القسم هو سجل العمل الجاري والتسليمات المنجزة. ابدأ من الوثائق المختصرة التالية، ثم انتقل إلى مجلد المهمة عند الحاجة إلى التفاصيل والأدلة.

## السجلات الحاكمة

| المجال | الوثيقة |
|---|---|
| ملخص التطبيق والحالة | [overview.md](overview.md) |
| التقدم الزمني | [progress.md](progress.md) |
| المتطلبات | [requirements.md](requirements.md) |
| التتبع من المتطلبات إلى التحقق | [traceability.md](traceability.md) |
| قرارات المعمارية | [architecture.md](architecture.md) |
| عقود التكامل | [api-contracts.md](api-contracts.md) |
| قرارات الواجهة | [design.md](design.md) |
| سياسة التحقق | [testing.md](testing.md) |
| المشكلات المعروفة | [known-issues.md](known-issues.md) |
| سجل الإصدارات | [releases.md](releases.md) |

## سجلات المهام

كل مجلد تحت [tasks](tasks/) يمثل مهمة مستقلة ويحتفظ بتصميمها أو قرارها، مراجعتها، تحققها، ومراسلات الباك إند إن وجدت. الملف `status.md` هو نقطة البدء عندما يكون موجوداً؛ وإلا تُقرأ وثيقة القبول أو التحقق الأحدث في المجلد.

المهام الحالية والتاريخية تشمل:

- [account-context](tasks/account-context/status.md)
- [auth-login](tasks/auth-login/status.md)
- [data-search-agent](tasks/data-search-agent/status.md)
- [debug-api-connectivity](tasks/debug-api-connectivity/acceptance-packet.md)
- [design-conformance](tasks/design-conformance/acceptance-checkpoint-2026-11-05.md)
- [design-docs-paths](tasks/design-docs-paths/status.md)
- [documentation-consolidation](tasks/documentation-consolidation/status.md)
- [firebase-package](tasks/firebase-package/status.md)
- [firebase-settings](tasks/firebase-settings/status.md)
- [foundation](tasks/foundation/status.md)
- [home-phone-conformance](tasks/home-phone-conformance/acceptance-checkpoint.md)
- [stable-staging-api](tasks/stable-staging-api/backend-request.md)
- [ui-preview-color-sync](tasks/ui-preview-color-sync/status.md)
- [vibrant-palette-02](tasks/vibrant-palette-02/verification.md)

## الأدلة

يحتوي [evidence](evidence/) على تقارير البناء والاختبارات والبوابات ولقطات الشاشة. هذه الملفات أدلة مرتبطة بزمن التنفيذ؛ لا تُعدّل نتائجها أو أوامرها التاريخية عند إعادة تنظيم الوثائق. أي دليل جديد يجب أن يُربط من سجل المهمة التي أنتجته.

