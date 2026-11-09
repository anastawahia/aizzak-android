# المراجعة البرمجية المستقلة — فشل الجولة الثانية

- التاريخ: 2026-11-08؛ المهمة `data-search-agent` / `verification`؛ `DS-REQ-01..15` و`SCR-DS-001..010`.
- المراجع الفعلي: `/root/repair_code_review` بدور `android-code-reviewer`؛ بلا تعديل ملفات أو تشغيل Gradle. حفظ المدير تسليم الوكيل.
- المصدر: `ec8effdee96ab10497b8026b9e07d8cfc9a3eeca98bc621ef73a9907834e5e62`.
- الحكم: **failed**؛ ملاحظة P2 مؤكدة واحدة؛ لا P1/P2 إضافية مؤكدة.

## P2 — إظهار عنوان محظور من التحرير offline

الموضع: `app/src/main/java/android/aizzak/com/feature/datasearch/DataSearchScreens.kt:103`.

فرع `EDIT_CONVERSATION` في شرط عرض المحتوى offline يرجع true متى وُجدت مساحة، دون فحص `conversationPaneState`. إعادة الإنتاج: `viewState=OFFLINE` مع مساحة محددة و`conversationPaneState=PERMISSION_DENIED` أو `SESSION_EXPIRED` وهدف RENAME واسم سابقين. ينتقل الفرع إلى عرض CONTENT العودي ثم يكشف الاسم رغم حظر الجزء.

يلزم رفض هاتين الحالتين مثل بقية الفروع، واختبار اختفاء الهدف والاسم السابقين، مع بقاء حفظ الاسم في offline المصرح بها.

## بقية الفحص

تستخدم النسخة offline قدرات مغلقة وworking=true وأحداثاً خاملة، ولا تمرر إلا الرجوع والإلغاء والنسخ المحلي؛ المؤلف والأسماء وأفعال التحميل والتغيير معطلة ولا يوجد إرسال آلي عند الاتصال. فروع المحادثة والمصادر والتفاصيل تفحص الجزء المحمي، والاستدعاء العودي محدود بانتقال OFFLINE إلى CONTENT. تحقق الاسم وشروط القدرة وعزل حوارات الحذف محفوظة. اختبارات offline الحالية لا تغطي تعارض الجزء المحظور أعلاه.

`suite.py lifecycle` و`git diff --check` للنطاق: exit 0؛ verification stale وcompleted=false وقت المراجعة. لا تستنتج المراجعة نجاح الاختبارات أو المطابقة البصرية أو الجهاز.

بصمات الملفات المفحوصة:

| الملف | SHA-256 |
|---|---|
| `DataSearchScreens.kt` | `1761597b666741dfd52cab9a1b5925b787a7d947774771feb7fd9f47a9e5f409` |
| `values/strings.xml` | `309eacf7bd22358cc81bee64765475d2c7c9421c3054a09b6f5a5ff5bf39df2f` |
| `values-ar/strings.xml` | `833ca8afb9a2192b81dc9b4a790d38c009df5089148e20df1973934b2bd2b1c8` |
| `DataSearchOfflineContextTest.kt` | `e97395244bc815fe3639769a675a3a0cfbfcfd940ec3323d1e0e437fe56f39bd` |
| `DataSearchFormFeedbackTest.kt` | `828009b9481aa4691d256564c9b99f35367ce79022f4d573bd47d48c22ae0191` |
