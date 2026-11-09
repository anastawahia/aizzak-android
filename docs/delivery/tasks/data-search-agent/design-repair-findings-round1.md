# مطابقة مستقلة بعد الإصلاح — الجولة الأولى

- التاريخ: 2026-11-08؛ المراجع `/root/independent_design_match` بدور `design-qa-reviewer`؛ لم يعدل المصدر أو الاختبارات.
- المصدر: `72cc95c013a8d37a0c19cbf49a2273f292f4be21a7beea2b830f1e1cdd81b9a6`.
- فُتحت الصور الحقيقية الـ272 فعلياً: 40 لقطة أساسية أمام المرجع في 20 لوحة مقارنة، و232 لقطة حالة في 58 لوحة بأربعة تكوينات.
- النتيجة الفعلية: `suite.py conformance` **failed، exit 1**؛ **62 matched،28 deviation،0 missing**. الفحوص الأربعة reference_locked/inventory_complete/screenshots_current/independent_design_review كلها true. لم يُعتمد أي انحراف.
- الملفات: [سجل الأحكام](design-repair-conformance-round1.json)، [نتيجة الأداة](design-repair-conformance-result-round1.json)، [manifest](../../evidence/screenshots/data-search-agent-conform-72cc95c0/manifest.json).

اختفى نقص الدليل البصري السابق؛ لا تعني زيادة عدد الانحرافات تراجع الإصلاح. تكشف صور الحالات الجديدة فروقاً لم يكن ممكناً الحكم عليها سابقاً. عادت الأيقونات وسياق الحذف وأدوار secondary/amber وبطاقات المصادر والمراحل والنطاق، واختفت تعليمات تطهير العقد من حوار الحذف.

## دفعة الإصلاح المتبقية

1. **أشكال الأفعال والحقول**: `design-tokens.md` §4 يحدد «12dp للحقول والأزرار» مع الشرائح حبات كاملة؛ CSS المرجع `.primary,.secondary,.text,.danger` و`.field input,.field textarea` يحدد `border-radius:12px`، والمؤلف `.compose-row textarea,.send` يستخدم15px. الصور الحالية تظهر أزراراً عادية pill وSend دائرة وحقولاً شبه مربعة، بينما danger وخيارات الاكتساب صُححت. أصلح في نطاق Data Search فقط، واحتفظ بشكل الشرائح. IDs: `DS002-09/10،DS003-02،DS004-03/05/06/07،DS006-02/07،DS007-03/07/08،DS008-06/07،DS009-08`، وأفعال الحالات الآمنة. أمثلة الصور: `SCR-DS-004-compact.png` و`SCR-DS-009-compact.png` داخل [لوحات المقارنة](../../evidence/design-repair-contact-72cc95c0/).
2. **أدوار لون الحالات**: `design-reference.html` الدالة `stateNotice()` تستخدم `.notice.warn` لحالة offline و`.notice.error` لحالتي permission/session؛ CSS يربطهما amberContainer/amber وerrorContainer/error، وتثبتها `design-tokens.md` §2. الصور الجديدة تستبدلها ببطاقة Material افتراضية بنفسجي/رمادي محايد، وتكرر العنوان كنص مساعد. يلزم تطبيق أدوار الحالة الفعلية، و`surface/outlineVariant` للفراغ/التحميل المحايد بدلاً من لون Card الافتراضي. IDs الجامعة: `DS001-07/08،DS002-11/12،DS003-07/08،DS004-08،DS006-09/10،DS007-09،DS008-09،DS009-09`. الدليل: لوحات `--offline/permission_denied/session_expired/error/empty-states.png`.
3. **هوية فراغ المحادثة**: الدالة `chat()` في المرجع عندما `S==='empty'` ترسم `.avatar` وسطية مع `◉` ثم عنوان دعوة ونص مساعد؛ يبقى المؤلف. صورة `SCR-DS-002--empty-states.png` تظهر بطاقة عامة بلا هوية RAG. أعد الرمز/المحاذاة/الدعوة وفق المرجع، مع بقاء المؤلف. ID `DS002-11`.
4. **offline مع محتوى مصرح محمّل**: `chat()` في المرجع يضيف تنبيه offline إلى السجل ويعطل textarea/Send؛ لا يحذف transcript إلا عند permission/session. `ux-journeys.md` §7 يقول إن السجل المحلي المقروء يوسم ويعطل الإرسال/الرفع/الفهرسة/التثبيت. الصور الحالية ببيانات fixture محملة تخفي الشاشة بأكملها، مع نص يدعي أن المحتوى السابق للقراءة. أظهر دليلاً فعلياً للسياق المصرح read-only في المحادثة/المصادر/التفاصيل، مع حفظ المسودة وتجميد الأفعال. لا تغيّر منع أسماء المساحات القديمة في `SCR-DS-001`، ولا إخفاء المحتوى عند permission/session. IDs `DS002-12،DS006-10،DS008-09`؛ النموذج المحرر offline ينبغي أن يحفظ عنوانه دون تمكين طلب جديد (`DS004-08`).
5. **سبب تحقق الاسم**: الجرد `DS004-08` يطلب inline validation. صورة `SCR-DS-004--validation-states.png` تعرض اسماً فارغاً وCreate معطلاً بلا سبب مكتوب. أضف رسالة موضعية واضحة مثل لزوم اسم المحادثة؛ لا تخترع حداً خادمياً.
6. **نص الفشل الحقيقي**: الجرد `DS004-08` و`DS009-06` يطلب خطأ داخل النموذج وحفظ/استعادة الحالة. لقطتا `--error-states.png` تعرضان تعليمات شرطية للمطور/المراجع: «تحافظ أخطاء التحقق والخدمة...» و«عند فشل التثبيت يعود مربع الاختيار...». استبدلهما برسالة فعلية للمستخدم تصف تعذر الحفظ/تحديث النطاق وإبقاء الاسم/استعادة الاختيار، مع إعادة محاولة آمنة. لا تغير اختيارات الخادم المؤكدة أو الإجابات السابقة.

## حدود الحكم

الفروق الطبيعية في رسم الخط/أشرطة النظام واختلاف الأسماء والأوقات ومحتوى fixture لا تعد انحرافاً. انهيار الأجزاء عند العربية840 وfontScale1.3 يتبع الرموز المعتمدة رغم تبسيط HTML. هذه صور JVM للواجهة المعزولة؛ لا تثبت TalkBack أو IME أو الاستعادة أو تفويض الخادم. يؤكد QA الجوانب المنفذة باختباراته المستقلة. لم تُنفذ مراجعة جهاز، ولم يُعدل backend أو المرجع أو الجرد أو lifecycle من هذا المراجع.

أمر النتيجة الفعلية:

```text
python -B C:/Users/anast/Desktop/aizzak-android/.codex/android-software-house/runtime/scripts/suite.py conformance --project C:/Users/anast/Desktop/aizzak-android --task data-search-agent --record docs/delivery/tasks/data-search-agent/design-repair-conformance-round1.json
```

ينتظر التقرير الإصلاح المستهدف ثم تحقق وصور جديدة؛ لا انتقال للقبول بهذه النتيجة.
