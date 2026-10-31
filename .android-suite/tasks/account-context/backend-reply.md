# رد الباك إند على backend-handoff.md — 2026-10-31

## ما تم في الباك إند

1. جدّد المالك `VAULT_SECRET_ID`. عطل AppRole كان من انتهاء المفتاح، لا من الكود. كل الخدمات تعمل الآن.
2. تعديلات الباك إند في `codex/account-context` **لم تُعتمد**. حُفظت كما هي في commit محلي على الفرع نفسه (`5835ca3`، دون push)، ولم تُدمج.
3. الباك إند العامل الآن مبني من `master` (`4bc6417`). أُعيد بناء صورة `app` منه وأُعيد إنشاء `app` وحده.
4. الفحص بعد الإقلاع:
   - `GET /health` ← 200
   - `GET /health/ready` ← 200
   - `GET /api/v1/me/context` بلا مصادقة ← 401

## ما يعنيه ذلك لتطبيق Android

- `GET /api/v1/me/context` و`POST /api/v1/me/heartbeat` كما هما، ولا تغيير في شكل الاستجابة.
- **`POST /api/v1/me/logout` على master يحظر حساب Firebase كله** (`auth:revoked:<sub>`) حتى انتهاء الرمز مع هامش 60 ثانية (3660 ثانية على الأكثر). يشمل الحظر كل الأجهزة وتطبيق الويب، ويشمل تسجيل دخول جديد.
- لذلك **يجب ألا يستدعي Android مسار `/me/logout` الآن.** إن استدعاه، فلن يستطيع المستخدم الدخول من جديد قبل ساعة تقريباً، لا من Android ولا من الويب.

## التعديل المطلوب في Android

1. في `app/src/main/java/android/aizzak/com/feature/auth/AuthViewModel.kt:121`:
   غيّر `fun signOut() = startSignOut(remote = true)` إلى `fun signOut() = startSignOut(remote = false)`.
   الخروج يصبح محلياً فقط: `FirebaseAuth.signOut()` ومسح بيانات الحساب.
2. حدّث `AuthViewModelTest` الذي يعدّ `logouts`: الخروج العادي يجب أن يعطي `logouts == 0`.
3. أبقِ `HttpAccountContextRepository.logout` في الكود، دون استدعاء، حتى تُحسم مهمة الخروج.
4. شغّل البناء واختبارات JVM والواجهة كما في `verification.md`.

## بعد ذلك

رحلة التحقق كما في نقطة الاستئناف: `adb reverse tcp:8080 tcp:80` ← دخول موثّق ← `/me/context` صالح ← الشاشة الرئيسية ← خروج ← دخول من جديد فوراً، ويجب أن ينجح.

## مؤجَّل

إبطال الرمز من الخادم عند الخروج دون حظر بقية الأجهزة مهمة منفصلة في الباك إند. تحتاج قرار المالك ومراجعة وPR. لا تُعدَّل ملفات الباك إند من مشروع Android.
