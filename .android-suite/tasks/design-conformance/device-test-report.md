# تقرير اختبارات الجهاز

- المهمة: `design-conformance`
- التاريخ: 2026-11-01؛ أُعيد التحقق على المصدر الحالي في 2026-11-05.
- الحالة: نجحت اختبارات الجهاز الحالية 15/15 بعد فتح الجهاز وإعادة محاولة فشلت بسبب شاشة القفل.
- بصمة المصدر الحالية: `4b98eb7c1dffc2d528f0d38d03387fcf96e09e1ac3cc63c5bf74b3ffaee6bcc8`
- بصمة المصدر قبل الإصلاح: `9d787246f8d1081e382b85d6a85cf3fe989b463ba8eba9b4309cff11a24e71f6`
- بصمة المصدر بعد الإصلاح: `04454cbdfae23445beed14a68c718c52771e86661137b823d1b0d2886b7769f5`

## بيئة التنفيذ

- الجهاز: Samsung `SM-A155F` فعلي ومتصل عبر ADB.
- النظام: Android 15، API 35.
- اللغة: `en-GB`.
- الشاشة: `1080x2340`، كثافة `450 dpi`.
- المضيف: Windows 11، Java 17، Android SDK في `D:\SDK`.

## التنفيذ والنتائج

1. فحص البيئة والجهاز:
   - الأمر: `python .codex/android-software-house/runtime/scripts/suite.py doctor --project <project> --devices`
   - رمز الخروج: `0`.
   - النتيجة: جهاز واحد صالح للتنفيذ، ولا توجد تشخيصات مانعة.

2. المحاولة الأولى للمجموعة الكاملة:
   - الأمر: `python .codex/android-software-house/runtime/scripts/suite.py verify --kind ui --project <project> --execute --attempts 1 --timeout 1800`
   - المهمة الفعلية: `:app:connectedDebugAndroidTest`.
   - رمز الخروج: `1`.
   - النتيجة: 15 اختباراً؛ 13 ناجحاً، فشلان، 0 أخطاء، 0 متخطى.
   - الفشلان: `authorizedNavigationAndSignoutClearAccountContent` و`accountSwitchDisposesPasswordRouteAndStartsAtHome`.
   - السبب المثبت: كان `onNodeWithText(email)` يشترط تطابقاً وحيداً، بينما شاشة الحساب تعرض البريد في عقدتين مقصودتين. لم يكن الفشل في سلوك التطبيق.

3. الإصلاح الموجّه:
   - الملف الوحيد المعدل: `app/src/androidTest/java/android/aizzak/com/AccountContextUiTest.kt`.
   - التغيير: استخدام `onAllNodesWithText(email).onFirst().assertExists()` في الحالتين فقط.
   - لم تُعدّل ملفات الإنتاج أو الباك إند أو عقد الخروج المحلي.

4. إعادة الاختبارين المستهدفين:
   - الأمر: `gradlew.bat :app:connectedDebugAndroidTest --console=plain --no-daemon -Pandroid.testInstrumentationRunnerArguments.class=<two-tests>`.
   - رمز الخروج: `0`.
   - النتيجة: 2/2 ناجحان على الجهاز.

5. إعادة المجموعة الكاملة عبر `suite.py`:
   - الأمر: `python .codex/android-software-house/runtime/scripts/suite.py verify --kind ui --project <project> --execute --attempts 1 --timeout 1800`.
   - رمز الخروج: `0`.
   - النتيجة: 15/15 ناجحة، 0 فشل، 0 أخطاء، 0 متخطى.
   - زمن الاختبارات المسجل في XML: 34.173 ثانية؛ انتهى Gradle بنجاح في نحو 55 ثانية.

## الأدلة

- تقرير HTML: `app/build/reports/androidTests/connected/debug/index.html`
- نتيجة XML: `app/build/outputs/androidTest-results/connected/debug/TEST-SM-A155F - 15-_app-.xml`
- APK الاختبار: `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`
  - SHA-256 للتحقق الحالي: `847658174af2b49a6dcc660ba8ff166f6db25e8cd332e921d0009704b803fd95`
- APK التطبيق المستخدم: `app/build/outputs/apk/debug/app-debug.apk`
  - SHA-256 للتحقق الحالي: `0aa914aafb1c3f3514f1c41cc450ce9fa05cb4a7e369a953e156cb6b3afa0017`

## الحدود والتسليم

- تستخدم الاختبارات سياقاً وإجراءات وهمية؛ لم تُجر أي مصادقة حقيقية أو تغيير كلمة مرور أو استدعاء خروج للباك إند.
- لا تثبت اختبارات الدلالات مطابقة بصرية؛ يبقى سجل مطابقة التصميم هو مرجع ذلك.
- لأن إصلاح الاختبار غيّر بصمة المصدر، يعرض `suite.py lifecycle` مرحلتي `verification` و`design-qa` كـ `stale` والمرحلة التالية `verification`. يجب على مدير الشركة إعادة تثبيت البوابات اللازمة على البصمة الجديدة قبل القبول.
- لم يتم توقيع إصدار أو نشره.

## إعادة التحقق بتاريخ 2026-11-05

- `suite.py verify --kind all --execute`: رمز الخروج `0`؛ نجح البناء و96/96 اختبار JVM وAndroid Lint بلا فشل أو خطأ أو تخطٍ.
- محاولة `suite.py verify --kind ui --execute` الأولى: رمز الخروج `1`؛ فشلت 15/15 بنفس خطأ البنية التحتية `No compose hierarchies found in the app`.
- أثبت فحص الجهاز بعد المحاولة أن `mWakefulness=Dozing` وKeyguard ظاهر، لذلك بقي نشاط اختبار Compose خلف شاشة القفل؛ لم يكن ذلك فشلاً وظيفياً في التطبيق.
- أُرسل `KEYCODE_WAKEUP` و`wm dismiss-keyguard` وضُبط `svc power stayon true`. لم تُسجل قيمة `stay_on_while_plugged_in` السابقة، لذلك لم تُخمن ولم تُسترجع تلقائياً. القيمة الحالية المقروءة بعد الاختبارات هي `15`.
- بعد فتح الجهاز يدوياً صار `isKeyguardShowing=false` والجهاز `Awake`.
- إعادة `suite.py verify --kind ui --execute`: رمز الخروج `0`؛ 15/15 ناجحة، 0 فشل، 0 أخطاء، 0 متخطى، ومدة suite المسجلة 73.016 ثانية.
- `suite.py screenshots --execute`: رمز الخروج `0`؛ نجحت 96/96 اختبارات JVM وتولدت 40 لقطة تغطي 10 شاشات × 4 إعدادات.

### أدلة المصدر الحالي

- `.android-suite/evidence/design-conformance-all-4b98eb7c.json`
- `.android-suite/evidence/design-conformance-ui-4b98eb7c-attempt1.json`
- `.android-suite/evidence/design-conformance-ui-4b98eb7c-final.json`
- `.android-suite/evidence/design-conformance-screenshots-4b98eb7c.json`
- `.android-suite/evidence/screenshots/1793858048/manifest.json`
- تقرير الجهاز النهائي: `app/build/outputs/androidTest-results/connected/debug/TEST-SM-A155F - 15-_app-.xml`

لم تُعدّل ملفات المصدر أو الباك إند أو دورة العمل أثناء إعادة التحقق الحالية. الخروج بقي محلياً فقط، ولم يُستدع مسار خروج الباك إند.
