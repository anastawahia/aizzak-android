# حزمة قبول إعادة تصميم الواجهة

- المهمة: `design-conformance`
- سير العمل: `ui-redesign`
- المرحلة: `acceptance`
- التاريخ: 2026-11-01
- بصمة المصدر: `9d787246f8d1081e382b85d6a85cf3fe989b463ba8eba9b4309cff11a24e71f6`
- الحالة: جاهزة لقرار المالك؛ لم يُسجل القبول بعد.

## ما تم بناؤه

- تنفيذ 10 شاشات Compose تغطي 119 عنصراً في الجرد المعتمد.
- دعم العربية والإنجليزية وRTL/LTR والثيمين الفاتح والداكن.
- تنقل سفلي للنوافذ المدمجة وشريط تنقل جانبي للنوافذ بعرض 600dp فأكثر.
- دعم عرضي 390dp و840dp وخط 1.0 و1.5 ضمن لقطات الاختبار.
- إصلاح دلالات TalkBack للرموز الزخرفية، واستخدام سهم رجوع متجهي ينعكس في RTL.
- الحفاظ على الخروج المحلي فقط؛ لم يُعدّل الباك إند ولم يُستدعَ مسار الخروج البعيد.

المسارات التطبيقية الرئيسية المعدلة: `app/src/main/java/android/aizzak/com/core/designsystem/`، شاشات `feature/account` و`feature/auth` و`feature/home` و`feature/settings`، غلاف التنقل `LocalFoundationApp.kt`، وموارد النصوص العربية والإنجليزية. أضيفت اختبارات اللقطات في `app/src/test/java/android/aizzak/com/DesignScreenshotTest.kt`، وحُدثت اختبارات `androidTest` الحالية.

## مرجع التصميم المعتمد

- `docs/design/UI-PREVIEW.html` — `d20dc95814303301c5dfb0146f8ec25ea717d4ecca64e28d5e4ac6e125f98d0c`
- `docs/design/UI-DESIGN.md` — `1f74b0a82357a560443045864f89cc27685a58af2a6b3999e0ec702f7b51e7ab`
- `screen-inventory.json` — `89fef11c5447da7f501ccf463da98cc2e1f4023f00f42726ad788b4f30f8093a`

## لقطات القبول مقابل المرجع

المرجع لكل صف هو `docs/design/UI-PREVIEW.html`. يعرض الجدول لقطة ممثلة `en/light/390dp/1.0`، بينما يثبت `manifest.json` أربع تهيئات لكل شاشة: العربية الداكنة 390dp، العربية الفاتحة 840dp بخط 1.5، الإنجليزية الداكنة 840dp، والإنجليزية الفاتحة 390dp.

| الشاشة | الاسم | لقطة ممثلة |
|---|---|---|
| `SCR-HOME-01` | Home | `docs/delivery/evidence/screenshots/1793547328/SCR-HOME-01__en_light_w390_fs1.0.png` |
| `SCR-ACCT-01` | Account | `docs/delivery/evidence/screenshots/1793547328/SCR-ACCT-01__en_light_w390_fs1.0.png` |
| `SCR-AUTH-01` | Sign in | `docs/delivery/evidence/screenshots/1793547328/SCR-AUTH-01__en_light_w390_fs1.0.png` |
| `SCR-AUTH-02` | Sign up | `docs/delivery/evidence/screenshots/1793547328/SCR-AUTH-02__en_light_w390_fs1.0.png` |
| `SCR-AUTH-03` | Reset password | `docs/delivery/evidence/screenshots/1793547328/SCR-AUTH-03__en_light_w390_fs1.0.png` |
| `SCR-AUTH-04` | Verify email | `docs/delivery/evidence/screenshots/1793547328/SCR-AUTH-04__en_light_w390_fs1.0.png` |
| `SCR-LOAD-01` | Loading session and context | `docs/delivery/evidence/screenshots/1793547328/SCR-LOAD-01__en_light_w390_fs1.0.png` |
| `SCR-CTX-01` | Context error | `docs/delivery/evidence/screenshots/1793547328/SCR-CTX-01__en_light_w390_fs1.0.png` |
| `SCR-SET-01` | Settings | `docs/delivery/evidence/screenshots/1793547328/SCR-SET-01__en_light_w390_fs1.0.png` |
| `SCR-ACCT-02` | Change password | `docs/delivery/evidence/screenshots/1793547328/SCR-ACCT-02__en_light_w390_fs1.0.png` |

دليل اللقطات الكامل: `docs/delivery/evidence/screenshots/1793547328/manifest.json` — `1d28a9d8fc11aef753988b2f8fb25f21a622c259fc6d19ac36368c893482243d`.

## نتائج التحقق والمراجعات

- `assembleDebug + testDebugUnitTest + lintDebug`: ناجح على البصمة الحالية؛ 95 اختباراً، 0 فشل، 0 خطأ، 0 متخطى. الدليل `design-conformance-all-final.json` — `59c52f40418642eebdd4ce443b29ad3e653c744b59e93eaba29423d1f5de33a9`.
- `assembleDebugAndroidTest`: ناجح. الدليل `design-conformance-ui-compile.json` — `dcbd692fb1de5bb844d37a51cca5295482b1d0771b2961aa1a03003290cd4d8e`.
- مراجعة الكود المستقلة: ناجحة، بلا نتائج P0–P3. `code-review.md` — `dc4e401d57bca3d2181f13435cb5c1dd5d3e21931d12c48942b6ef81a26c326d`.
- مطابقة التصميم المستقلة: ناجحة؛ 10 شاشات، 40 لقطة، 119 مطابقاً، 0 انحراف، 0 مفقود. `conformance-record.json` — `ebb6c2412fcf8b2ef81178ccecb017e06d704add71203570e3298e15f28cc560`.
- تقرير بوابة المطابقة: `conformance-report.json` — `4eb7557609b3fcec1ba3bdfb41d21aaa36c40fcc9072e96835cc721426f1cf83`.
- APK تجريبي غير موقع للإصدار: `app/build/outputs/apk/debug/app-debug.apk` — `a1013054c4dd65ed0b6a335976d47944056c2a1699efb222570a597435625a3f`.

## الانحرافات والقيود المعروفة

- لا توجد انحرافات تصميم مفتوحة أو معتمدة.
- لم يتوفر جهاز أو محاكي متصل؛ جُمّع APK اختبارات الواجهة بنجاح لكن لم تُنفذ اختبارات `androidTest` على جهاز. لقطات Roborazzi هي دليل JVM على التخطيط وليست دليلاً على فروق الرسم الخاصة بجهاز بعينه.
- فحص Android Lint المعتمد في البوابة ناجح. تشغيل Detekt الإضافي السابق كشف ديناً ساكناً قديماً ومختلط النطاق ولم يُخفَ بخط أساس أو suppressions؛ لم يُستخدم كدليل نجاح.
- تعذر على مراجع التصميم عرض ملف HTML المحلي مباشرة بسبب سياسة أمان المتصفح؛ استخدم وثائق التصميم والجرد المقفلين مع الفحص البصري لجميع اللقطات الأربعين.
- لا توقيع إصدار، ولا حزمة Release، ولا نشر إلى Google Play ضمن هذه المهمة.
- ثُبتت أدوار Android Software House 2.0.0 محلياً تحت `.codex/` لاستكمال دورة العمل؛ لم يحدث تثبيت نظامي.

## قرار المالك المطلوب

الرجاء قبول التنفيذ بهذه البصمات أو طلب تعديلات محددة. القبول يمرر مرحلة `acceptance` فقط؛ لا يصرح بتوقيع إصدار أو نشره.
