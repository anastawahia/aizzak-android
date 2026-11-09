# حزمة القبول — شريحة واجهات Data Search

- التاريخ: 2026-11-08
- المرحلة الحالية العملية: **مراجعة القبول النهائي بمسار استثناء يدوي**.
- المصدر: `3f3ab2bdb0bae1102c452d7e3206066e96edd4ce7494b82be47e196b48e3509f`.
- الأساس: [توجيه المالك والقيود المنقولة](design-qa-exception.md).
- حالة القرار: الحزمة جاهزة للمراجعة؛ لم يُسجل قبول نهائي للتطبيق بعد.

## ما تم تسليمه

واجهات Compose للشاشات `SCR-DS-001..010`، ونماذج الحالة والأفعال، والعربية والإنجليزية، والثيمين والتخطيطين، وMarkdown الآمن، وحالات الرسائل والرفع ونطاق الإجابة، وشروط الصلاحيات والفهرسة والصفحات. أُغلقت الملاحظات الأربع في آخر مراجعة برمجية: إرسال النص، تمرير المحادثات، صفحات المصادر، وسلامة الفهرسة.

هذه شريحة واجهات stateless مع اختبارات وبيانات fixture. بطاقة Home ما زالت غير مفعلة، ولا يوجد route أو ViewModel تكامل حي لهذه الميزة؛ لذلك لا يمكن تقييم رحلة Data Search تشغيلية من APK الحالي. مادة القبول الحالية هي المصدر والعقود المحلية والاختبارات واللقطات المقارنة، وليست خدمة RAG عاملة.

## التحقق والمراجعة

- [المراجعة البرمجية المستقلة](code-review-6.md): passed، وملاحظة P3 غير حاجبة باقية.
- [QA المستقل](verification-final.md): compile وlint ناجحان؛ 385 اختبار مشروع،منها281 Data Search،دون failures/errors/skips؛40 لقطة حديثة.
- [بوابة التنفيذ](final-gate.json): ناجحة، وسُجلت verification passed في lifecycle.
- [مطابقة التصميم](design-qa-report.md): **failed**؛53 matched،21 deviation،16 missing/unverified visual evidence. قرار المالك هو المتابعة دون إصلاح جديد، مع حفظ النتيجة في [الاستثناء](design-qa-exception.md).
- لا جهاز أو محاكي متصل؛ TalkBack وIME والاختبارات المتصلة غير منفذة.

## مقارنة التنفيذ بالمرجع لكل شاشة

كل لوحة تحتوي المرجع وCompose جنباً إلى جنب. عمود compact يغطي EN/light/390/1.0 وAR/dark/390/1.0؛ وexpanded يغطي EN/dark/840/1.0 وAR/light/840/1.3.

| الشاشة | compact | expanded |
|---|---|---|
| SCR-DS-001 المساحات | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-001-compact.png) | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-001-expanded.png) |
| SCR-DS-002 المحادثة | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-002-compact.png) | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-002-expanded.png) |
| SCR-DS-003 قائمة المحادثات | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-003-compact.png) | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-003-expanded.png) |
| SCR-DS-004 إنشاء/تسمية المحادثة | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-004-compact.png) | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-004-expanded.png) |
| SCR-DS-005 حذف المحادثة | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-005-compact.png) | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-005-expanded.png) |
| SCR-DS-006 المصادر | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-006-compact.png) | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-006-expanded.png) |
| SCR-DS-007 إضافة مصدر | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-007-compact.png) | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-007-expanded.png) |
| SCR-DS-008 تفاصيل المصدر | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-008-compact.png) | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-008-expanded.png) |
| SCR-DS-009 نطاق الإجابة | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-009-compact.png) | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-009-expanded.png) |
| SCR-DS-010 حذف المصدر | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-010-compact.png) | [مقارنة](../../evidence/design-qa-contact-3f3ab2bd/SCR-DS-010-expanded.png) |

## الملفات المثبتة لهذا التسليم

| الملف | SHA-256 |
|---|---|
| `code-review-6.md` | `df2c60a88f109b75294765a467e778e0ccbc29a7a134cb498139508cd3a2dd01` |
| `verification-final.md` | `26524a35eef9c27fe1f1a5608aa298427d1a1a4d8aeeecd55743b27fe8cdcc84` |
| `final-gate.json` | `f7d52bc021c386e668884cebbc5c2b565cb7cb6b3fd4ee411e6ffd1ea8919492` |
| `design-qa-report.md` | `751f918598002eb379b50fd82d8b33d87c71ac4b1633ef5170bfbc0df4a2149d` |
| `design-qa-conformance-result.json` | `1f60ae24c8bf80228bd97f3cf6f80d552c6e1b5f7d64ed8ccdfdbf8b172e2f6b` |
| `app/build/outputs/apk/debug/app-debug.apk` | `6a6164f3a1383ee2461885f87c84526a0a59c5e93f1ff88895f7bb709466964b` |

APK أعلاه debug build للمشروع؛ Data Search غير قابلة للفتح فيه. لا يوجد signed release أو نشر ضمن هذا التسليم. hashes الصور الأربعين مثبتة في [manifest](../../evidence/screenshots/data-search-agent-3f3ab2bd/manifest.json)، والحزمة التصميمية الخماسية مثبتة بقرار design في lifecycle.

## ما بعد القبول

بعد قرار قبول الشريحة مع القيود، تُوثق النتيجة كقبول واجهات مشروط. الربط التشغيلي يحتاج رد مالك الباك إند على [طلب العقد](backend-change-request.md)، ثم نطاق تكامل Android مؤكد واختبارات تكامل. لا تُعاد دورة إصلاح التصميم تلقائياً، ولا تُعدل ملفات الباك إند من هذا المشروع.
