# حزمة قبول إعادة تصميم الواجهة — 2026-11-05

- المهمة `design-conformance`، المسار `ui-redesign`، المرحلة `acceptance`.
- بصمة المصدر `4b98eb7c1dffc2d528f0d38d03387fcf96e09e1ac3cc63c5bf74b3ffaee6bcc8`.
- الإصدار التجريبي [app-debug.apk](../../../app/build/outputs/apk/debug/app-debug.apk) ببصمة `0aa914aafb1c3f3514f1c41cc450ce9fa05cb4a7e369a953e156cb6b3afa0017`.

## ما تم تسليمه

تغطي الواجهة 10 شاشات Compose و119 عنصراً من التصميم المعتمد، بالعربية والإنجليزية وبالاتجاهين والمظهرين. يظهر التنقل السفلي في العرض المدمج، والشريط الجانبي عند 600dp فأكثر. الخروج من الحساب محلي. تتضمن النسخة الحالية تحديث المصادقة الموجود في الفرع، الذي يحد انتظار الدخول وإنشاء الحساب واستعادة كلمة المرور وإعادة إرسال التحقق إلى 20 ثانية.

استُعيدت نهايات LF في ثلاثة ملفات من حزمة التصميم بعد تحويلها محلياً إلى CRLF. تطابقت الملفات الخمسة مع بصماتها المعتمدة في [حزمة التصميم](design-checkpoint.md).

## الشاشات مقابل المرجع

المرجع البصري لجميع الصفوف هو [المعاينة المعتمدة](../../../docs/UI-PREVIEW.html). يعرض الجدول تهيئة الإنجليزية الفاتحة بعرض 390dp. تشمل [قائمة اللقطات](../../evidence/screenshots/1793858048/manifest.json) أربع تهيئات لكل شاشة، منها العربية RTL والمظهر الداكن وعرض 840dp وخط 1.5.

| الشاشة | لقطة النسخة الحالية |
|---|---|
| الرئيسية | [SCR-HOME-01](../../evidence/screenshots/1793858048/SCR-HOME-01__en_light_w390_fs1.0.png) |
| الحساب | [SCR-ACCT-01](../../evidence/screenshots/1793858048/SCR-ACCT-01__en_light_w390_fs1.0.png) |
| تسجيل الدخول | [SCR-AUTH-01](../../evidence/screenshots/1793858048/SCR-AUTH-01__en_light_w390_fs1.0.png) |
| إنشاء الحساب | [SCR-AUTH-02](../../evidence/screenshots/1793858048/SCR-AUTH-02__en_light_w390_fs1.0.png) |
| استعادة كلمة المرور | [SCR-AUTH-03](../../evidence/screenshots/1793858048/SCR-AUTH-03__en_light_w390_fs1.0.png) |
| التحقق من البريد | [SCR-AUTH-04](../../evidence/screenshots/1793858048/SCR-AUTH-04__en_light_w390_fs1.0.png) |
| تحميل الجلسة | [SCR-LOAD-01](../../evidence/screenshots/1793858048/SCR-LOAD-01__en_light_w390_fs1.0.png) |
| خطأ سياق الحساب | [SCR-CTX-01](../../evidence/screenshots/1793858048/SCR-CTX-01__en_light_w390_fs1.0.png) |
| الإعدادات | [SCR-SET-01](../../evidence/screenshots/1793858048/SCR-SET-01__en_light_w390_fs1.0.png) |
| تغيير كلمة المرور | [SCR-ACCT-02](../../evidence/screenshots/1793858048/SCR-ACCT-02__en_light_w390_fs1.0.png) |

## النتائج والقيود

- نجح بناء Debug وAndroid Lint و96/96 اختبار JVM بلا تخطٍّ. [دليل البناء والتحقق](../../evidence/design-conformance-all-4b98eb7c.json)
- نجح 15/15 اختباراً على Samsung SM-A155F، بنظام Android 15 وAPI 35، بعد فتح شاشة القفل. تستخدم اختبارات الواجهة بيانات وإجراءات وهمية. [تقرير الجهاز](device-test-report.md)
- اجتاز نطاق الواجهة مراجعة الكود المستقلة. وثّق المراجع مشكلة P2 سابقة في نطاق المصادقة الأوسع، إذ قد يطول انتظار بعض طلبات تحديث الهوية والرمز وتغيير كلمة المرور عند غياب رد Firebase. [مراجعة الكود](code-review-2026-11-05.md)
- راجع المراجع المستقل 119 عنصراً في 10 شاشات، وأعاد تثبيت الأحكام على اللقطات الأربعين المطابقة بالبصمة. النتيجة 119 مطابقاً وصفر انحراف أو عنصر مفقود. لقطات Roborazzi دليل JVM على التخطيط. اعتمد المراجع على الجرد ووثائق التصميم المقفلة والتقييم السابق عند تثبيت النتيجة الحالية، مع بقاء قيد عرض ملف HTML المحلي مباشرة. [تقرير المطابقة](conformance-report-4b98eb7c.json)
- بقي الهاتف مضبوطاً لإبقاء الشاشة نشطة عند توصيله بالطاقة بعد محاولة تجاوز السكون. القيمة الحالية `15` والقيمة السابقة غير مسجلة.
- يُستبعد توقيع الإصدار والنشر من نطاق هذه الحزمة.

## بصمات أدلة القبول

| الدليل | SHA-256 |
|---|---|
| [دليل JVM وLint](../../evidence/design-conformance-all-4b98eb7c.json) | `e1715a0dec2ec6a71475a9a2531af12aea4f244180214aeca6c10ffaa4301239` |
| [دليل تشغيل الجهاز النهائي](../../evidence/design-conformance-ui-4b98eb7c-final.json) | `b6c6bae83c7f2f7d3fa3c61fd3d5a6819a365a3ffd7e5733334f0051afebc3c4` |
| [تقرير الجهاز](device-test-report.md) | `6c3ead1eb9a348bee4af77cc591c8e1caa23fa24736c4be610f78eca50d2982b` |
| [مراجعة الكود](code-review-2026-11-05.md) | `a03dd35d8b6e54eea6c856518c193fcf188052ca7aa7e92505f49dc30f7c3d82` |
| [قائمة اللقطات](../../evidence/screenshots/1793858048/manifest.json) | `ce148af6eb71a3fa165739b8edd8dc1ce709bc0eca794c8022d43366a6c99fe3` |
| [سجل المطابقة](conformance-record-4b98eb7c.json) | `c499600cb6f7a4d58401708d8a91e5d4d91f8ce49949fd951f941e96243d1fc6` |
| [تقرير المطابقة](conformance-report-4b98eb7c.json) | `51946d62e42308e88f5cace4ed1f666fafa09e01f4ac7a103b28eb1e82fac28a` |
| [بوابة التحقق](reports/verification.json) | `cd0015c5c3f3a10e571d145bf2d6fa98a5780b1132a6f5339852326913ef175f` |
| [بوابة مطابقة التصميم](reports/design-qa.json) | `7669bf5026d99da7ee0e1d5ac64ae5c38e155c33a34145bad4f175ba5793b595` |

## القرار

هذه الحزمة للقبول على البصمة الحالية بعد إعادة التحقق والمطابقة. يُغلق اعتمادها مرحلة `acceptance` ومهمة `ui-redesign` فقط.
