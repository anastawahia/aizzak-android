# البدء السريع

## 1. عنوان الخدمة

العقد يعرّف الأساس النسبي التالي:

~~~text
/api/v1
~~~

استبدل BASE_URL بعنوان البيئة المستهدفة، مثل عنوان localhost أو عنوان staging.

## 2. المصادقة

أرسل Firebase ID Token في كل طلب محمي:

~~~http
Authorization: Bearer <FIREBASE_ID_TOKEN>
Accept: application/json
X-Correlation-Id: <OPTIONAL_TRACE_ID>
~~~

مسارات الصحة /health/live و/health/ready عامة ولا تحتاج إلى مصادقة.

## 3. مثال قراءة سياق المستخدم

~~~bash
curl -sS "${BASE_URL}/api/v1/me/context" \
  -H "Authorization: Bearer ${FIREBASE_ID_TOKEN}" \
  -H "Accept: application/json"
~~~

## 4. مثال قائمة الوكلاء

~~~bash
curl -sS "${BASE_URL}/api/v1/agents" \
  -H "Authorization: Bearer ${FIREBASE_ID_TOKEN}" \
  -H "Accept: application/json"
~~~

استجابات القوائم تكون بالشكل العام:

~~~json
{
  "data": [],
  "meta": {
    "next_cursor": null,
    "limit": 50
  }
}
~~~

## 5. رفع ملف وفهرسته

التدفق الصحيح يتكون من ثلاث مراحل:

1. POST /api/v1/files لتسجيل الملف والحصول على رابط رفع موقّع.
2. رفع البايتات إلى الرابط الموقّع ثم POST /api/v1/files/{file_id}/complete.
3. POST /api/v1/knowledge/documents لبدء الفهرسة يدويًا.

إكمال الرفع لا يبدأ الفهرسة تلقائيًا.

## 6. الأخطاء

الأخطاء تستخدم application/problem+json وفق RFC 9457، بالشكل العام:

~~~json
{
  "type": "about:blank",
  "title": "Request failed",
  "status": 400,
  "code": "stable.machine.code",
  "correlation_id": "trace-id"
}
~~~

استخدم code للمنطق البرمجي وcorrelation_id عند التتبع أو التواصل مع الدعم.

## 7. البث

- SSE: أرسل Accept: text/event-stream في عملية الوكيل المناسبة.
- WebSocket: اتصل بـ /api/v1/ws واتبع رسائل البروتوكول في api-specification.md، القسم 3.2.

## 8. العمليات القابلة لإعادة المحاولة

في العمليات التي يعرّف عقدها Idempotency-Key، أرسل قيمة فريدة وثابتة لنفس المحاولة المنطقية:

~~~http
Idempotency-Key: 7f3ad7e9-b0ea-4e67-a21e-f1e22b7d0e11
~~~

لا تُعد استخدام المفتاح لطلب مختلف.
