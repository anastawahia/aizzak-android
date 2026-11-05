# حزمة قبول إصلاح الاتصال — debug-api-connectivity

التاريخ: 2026-11-05  
المتطلب: `CONN-01`  
الحالة: بانتظار قبول المالك

## ما تم إصلاحه

سبب رسالة **We couldn’t load your account** كان سقوط تحويل ADB بعد إعادة اتصال الهاتف. أُعيد التحويل بالأمر الآمن ضمن حدود المشروع:

```powershell
.\scripts\connect-local-api.ps1 -SkipStackStart
```

أصبح التحويل الفعلي `tcp:8080 -> tcp:80`. بعد الضغط على **Try again** وصل التطبيق على Samsung SM-A155F إلى Home وظهرت `Your assistants` و`Account` و`Settings`.

## قرار التنفيذ

- لم يتغير كود التطبيق أو الاختبارات أو السكربتات؛ الإعداد الحالي صحيح ومقيّد بـ`127.0.0.1` في debug.
- بقي cleartext محظوراً افتراضياً، وبقي release يتطلب HTTPS.
- لم يتغير سلوك تسجيل الخروج المحلي.
- لم يُعدّل أي ملف أو إعداد أو فرع أو خدمة في الباك إند.
- لم تُمس تغييرات مهمة `design-conformance` الموجودة مسبقاً.

## التحقق

- `suite.py verify --kind all --execute`: exit 0.
- Debug build: passed.
- JVM: 96 اختباراً، 0 failures، 0 errors، 0 skipped.
- Android Lint: 0 errors، 36 warnings.
- APK SHA-256: `0aa914aafb1c3f3514f1c41cc450ce9fa05cb4a7e369a953e156cb6b3afa0017`.
- `/health` و`/health/ready`: كلاهما 200.
- مراجعة مستقلة: passed، ولا توجد ملاحظات P0–P3.
- بوابة implementation: passed للمصدر `4b98eb7c1dffc2d528f0d38d03387fcf96e09e1ac3cc63c5bf74b3ffaee6bcc8`.
- Design QA: غير منطبق لأن الواجهة والتصميم وكود التطبيق لم تتغير.

## الأدلة

- `.android-suite/evidence/debug-api-connectivity-all.json`
- `.android-suite/evidence/debug-api-connectivity-implementation.json`
- `.android-suite/tasks/debug-api-connectivity/qa-verification.md`
- `.android-suite/tasks/debug-api-connectivity/code-review.md`
- `.android-suite/tasks/debug-api-connectivity/gate-evidence.json`

## القيد التشغيلي

يجب إعادة تشغيل `connect-local-api.ps1 -SkipStackStart` بعد فصل USB أو إعادة تشغيل الهاتف/ADB، لأن `adb reverse` ليس دائماً. استخدام `-SkipStackStart` إلزامي هنا احتراماً لملكية الباك إند.

## قرار المالك المطلوب

قبول الإصلاح كما هو، أو طلب تغييرات. القبول يغلق هذه المهمة التشغيلية فقط، ولا يصرّح بأي تعديل أو نشر للباك إند أو إصدار إنتاجي.
