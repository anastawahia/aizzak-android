# أدلة القرارات من مشروع الويب المرتبط

تاريخ المراجعة: 2026-10-30. المصدر: `\\wsl.localhost\Ubuntu-24.04\home\web_app`.

هذه مراجعة كود وإعدادات عامة محلية، وليست اختبار تشغيل أو إثبات حالة إنتاج. لم يُعدّل مشروع الويب. أرقام الأسطر وقت القراءة وقد تتغير لاحقاً. جميع مسارات الأدلة أدناه نسبة إلى جذر مشروع الويب. لم تُنسخ مفاتيح أو رموز مصادقة؛ قيم Firebase المذكورة معرفات عامة.

## الأدلة حسب القرار

| القرار | الملف والأسطر | الدليل والحدود |
|---|---|---|
| D-01 | `src/components/AppHeader.jsx:57–60`؛ `src/pages/Login.jsx:127–129` | AIZZAK في الشعار والعنوان وفي النص العربي والإنجليزي؛ لا Android manifest في هذا المشروع يحدد package ID |
| D-02 | `src/contexts/AuthContext.jsx:67–76, 172–178, 224–236`؛ `src/App.jsx:18–32` | رفض الدخول غير المتحقق، وعدم تحميل سياقه، وحماية المسارات، وإعادة فحص التحقق كل 3 ثوان أثناء شاشة الانتظار |
| D-02 | `src/pages/VerifyEmail.jsx:23–35` | إعادة إرسال بحالة انتظار؛ لا عداد cooldown مدته 60 ثانية في التنفيذ المقروء |
| D-03 | `src/features/rag/pages/SpacesHome.jsx:13–24, 49–69` | يعرض ناتج listSpaces ويستدعي createSpace بإجراء المستخدم؛ تعليق ينسب main space إلى الخادم |
| D-03 | `src/api/index.ts:177–200` | تعليق fileAgentSpaceId يصف اختيار أقدم مساحة من أول صفحة كحل مؤقت ويقول إن main space لم يوفر تلقائياً؛ يتعارض مع تعليق SpacesHome. المنطق يستخدم آخر عنصر في أول 100، فلا يضمن الأقدم عند تجاوزها |
| D-04 | `src/features/rag/pages/RagConversation.jsx:109–133`؛ `src/features/rag/api/messages.ts:85–101` | استدعاء SSE حقيقي بـstream:true وAccept:text/event-stream؛ fallback إلى JSON في 406/415/501 قبل وصول المحتوى، ثم إعادة جلب السجل |
| D-04 | `src/api/index.ts:222–230` | محادثة تعديل الملفات ترسل stream:false؛ لا استخدام WebSocket عُثر عليه في src المفحوص |
| D-05 | `src/pages/Workspace.jsx:27–76`؛ `src/api/index.ts:30–31, 222–230`؛ `src/features/rag/api/messages.ts:58–63` | مفاتيح الوكلاء المعروضة، وطلبا النص لـRAG وتعديل الملفات. حقول العرض الثابتة ليست إثبات كتالوج الخادم الحي |
| D-05 | `src/components/ChatComposer.jsx:40`؛ `openapi.json` → `components.schemas.MessageCreateIn/ConversationCreateIn` | textarea بلا maxLength؛ content كائن إضافي الحقول، والعنوان نص أو null بلا سقف معلن في المخطط. لا يثبت هذا عدم وجود حد داخل الوكيل أو الخادم |
| D-06 | `src/features/rag/api/conversations.ts:14–19`؛ `src/features/rag/api/messages.ts:47–53`؛ `src/api/index.ts:214–220` | مرشحا المساحة والمساعد، وحد الصفحة 100، وإرجاع data دون cursor. التمرير لأسفل في الواجهة لا يعوض الصفحات الناقصة |
| D-07 | `src/contexts/AuthContext.jsx:87–120`؛ `src/api/index.ts:94–96, 99–143` | حذف الحساب يرمي unavailable بعد إعادة التحقق. updateUsername يغير Workspace ثم Firebase displayName؛ updatePhoto يرفع إلى profile/{uid} في Storage ثم يغير photoURL. دالة API المسماة updatePhoto غير متاحة، لكن تنفيذ AuthContext يستخدم Firebase مباشرة |
| D-08 | `src/App.jsx:46–51` | تعليق إعادة توجيه روابط RAG القديمة يذكر محو محادثاتها في 2026-08-16؛ لا سجلات قاعدة بيانات أو خطة ترحيل تؤكد شمول المحو لحسابات Android |
| D-09 | `src/App.jsx:68, 122`؛ `src/features/admin/pages/AdminAccessGate.jsx:6–24`؛ `src/api/index.ts:376–387`؛ `src/pages/GmailSuccess.jsx:7–17` | /admin ضمن نفس الويب وشرط platform:admin؛ إنشاء connection وطلب authorize_url؛ صفحة النجاح تقرأ email وتعود للرئيسية بعد 3 ثوان. وجودها لا يثبت رابط عودة Android ولا نجاح الربط؛ حالة الاتصال تُقرأ من API |
| D-10 | `src/firebase.js:5–15`؛ `.firebaserc`؛ متغيرا PROJECT_ID وAUTH_DOMAIN العامان في `.env` | Firebase project aizzak-agent وauthDomain aizzak-agent.firebaseapp.com. لم تُنسخ apiKey/appId أو بقية محتوى env إلى هذه الوثيقة |
| D-10 | `src/api/http.ts:12–20`؛ `vite.config.js:8–14`؛ `openapi.json` → info | عنوان API من VITE_API_BASE_URL أو نفس الأصل؛ القيمة المحلية فارغة، ووكيل التطوير http://localhost. عنوان العقد AIZZAK Platform API وإصداره v1، بينما الوثائق المحلية الأخرى تعلن 1.0.0. اختلاف العنوان/الإصدار لا يثبت وحده اختلاف السلوك؛ يلزم فحص تعاقدي |
| D-12 | `src/contexts/SettingsContext.jsx:17–25, 45–48` | en وdark افتراضياً، حفظ محلي، وRTL للعربية، واتباع مظهر النظام عند اختياره |
| D-12 | `src/contexts/AuthContext.jsx:238–263` | pulse عند البداية وكل 60 ثانية وأحداث العودة للظهور؛ الطلب فقط عند visibilityState=visible. يقابل ذلك AppState في Android، لا مراقبة DOM |
| D-12 | `src/components/ChatMessage.jsx:33`؛ `src/features/rag/components/WorkspaceFiles.jsx:71` | ar-EG/en-US في الدردشة، وar-IQ/en في الملفات، دون timeZone صريح؛ لا توجد سياسة locale موحدة يمكن نقلها كما هي |

## سلوك موجود لا يعتمد تلقائياً في Android

1. حذف هويات Firebase غير المتحققة عند الخروج أو بعد عشر دقائق موجود في AuthContext:147–160؛ لا يدخل متطلبات التطبيق الجديد لمجرد وجوده، ويحتاج سياسة مستقلة.
2. ربط اسم المستخدم باسم Workspace قد يغير تسمية مشتركة؛ لا يُستعمل كعقد ملف شخصي.
3. اختيار مساحة ضمني من صفحة واحدة، وإسقاط meta.next_cursor، لا يعالجان السجلات الكبيرة.
4. SSE parser في messages.ts يمرر data ويتجاهل اسم event؛ وجوده لا يثبت اكتمال معالجة notice/final/error في الويب. يحتفظ Android بمعايير القبول الأوضح في MOB-FR-024.
5. مجموع الرموز في RagConversation يحسب الرسائل المحملة؛ لا يُعتمد كإجمالي موثوق.
6. ملفات docs/API.MD وبعض دوال التوافق تصف عقوداً قديمة أو غير متاحة؛ قُدّم التنفيذ المتصل وopenapi.json على أسمائها العامة. لا تُعتبر التعليقات وحدها إثبات حالة الخادم.

## ما لم تجب عنه المراجعة

package ID واسم المتجر، سياسة تحقق البريد داخل الخادم، provisioning أول مساحة، قائمة الوكلاء الحية وحدود المدخلات، الوصول إلى نهاية سجل طويل، قواعد Firebase Storage، سياسة المحو والترحيل، Android App Links، عناوين البيئات القابلة للهاتف وتطابقها التعاقدي، الحد الأدنى لـAndroid والأداء والاحتفاظ بالـcache.
