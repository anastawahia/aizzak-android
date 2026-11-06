# مراجعة مستقلة — debug-api-connectivity

التاريخ: 2026-11-05. المراجع الفعلي: `/root/connectivity_independent_review`، الدور `android-code-reviewer`. النتيجة: **passed**، ولا توجد ملاحظات P0–P3 ضمن نطاق الإصلاح.

راجع المراجع حالة Git والملفات التالية دون تعديلها:

- `app/src/debug/res/xml/network_security_config.xml`: يسمح HTTP في debug للعنوان `127.0.0.1` فقط.
- `app/src/main/res/xml/network_security_config.xml`: يمنع cleartext افتراضياً.
- `app/src/main/java/android/aizzak/com/core/network/ApiEnvironment.kt`: يقبل HTTP فقط لعنوان debug المحلي والمسار `/api/v1/`.
- `app/build.gradle.kts`: يبقي release مقيداً بعنوان HTTPS صريح.
- `app/src/test/java/android/aizzak/com/data/session/HttpAccountContextRepositoryTest.kt`: يغطي رفض HTTP في release وLAN و`localhost` والعناوين ذات credentials/query/fragment أو المسار الخاطئ.
- `scripts/connect-local-api.ps1`: يعيد إنشاء `adb reverse tcp:8080 tcp:80`.

القرار: العطل تشغيلي بسبب سقوط تحويل ADB بعد إعادة اتصال الهاتف. بعد تشغيل `scripts/connect-local-api.ps1 -SkipStackStart` نجحت إعادة المحاولة الحية ووصل التطبيق إلى Home. لا يلزم تغيير كود Android.

المخاطر المتبقية:

- يجب استخدام `-SkipStackStart` دائماً التزاماً بملكية الباك إند؛ الوضع الافتراضي للسكريبت يشغّل `docker compose up` وهو خارج نطاق هذه المهمة.
- لا يوجد اختبار instrumentation مباشر لـ`NetworkSecurityPolicy`، لكن اختبار التحقق الحالي والنجاح الفعلي على الجهاز كافيان لهذا الإصلاح التشغيلي.

لم يغيّر المراجع أي ملف، ولم يشغّل Gradle أو يغيّر حالة الباك إند أو الجهاز.
