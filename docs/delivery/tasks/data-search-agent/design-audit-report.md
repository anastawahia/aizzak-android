# تدقيق مطابقة تصميم Data Search

التاريخ: 2026-11-09. المهمة: `data-search-agent`؛ المرحلة المفحوصة: `design-qa`؛ النطاق: `DS-REQ-01..15`، والشاشات `SCR-DS-001..010`.

**النتيجة: failed — 78 عنصراً مطابقاً، و12 عنصراً مسجلاً كانحراف، و0 مفقود، من أصل90.** تشمل الانحرافات10 عناصر ذات فروق بصرية مؤكدة، وعنصرين يرتبطان بتعارض قياسات داخل الحزمة المعتمدة نفسها. فُحصت الصور الحالية272/272 بجميع التكوينات المطلوبة. عادت أداة المطابقة فعلياً `exit 1`؛ اجتازت سلامة المرجع واكتمال الجرد وحداثة الصور واستقلال المراجع.

هذا تدقيق وتوثيق فقط، امتثالاً لتوجيه المالك: «توقف عن دورات الاصلاح قم بالفحص والمقارنة وتوثيق الاختلاف والانحراف عن التصميم بدون اكمال الاصلاح». لم يُعدّل المصدر أو الاختبارات أو النصوص أو التصميم أو الجرد أو `lifecycle.json`، ولم يُنفذ إصلاح أو `advance` أو اعتماد استثناء أو طلب موافقة على الانحرافات. العدد النهائي هنا يحل محل التقدير المبدئي في رسائل التقدم؛ أظهر توثيق الأشكال والطباعة بدقة فروقاً لم يتضمنها ذلك التقدير.

## أساس المقارنة وحداثة الدليل

المراجع المستقلة: `/root/independent_design_match` بدور `design-qa-reviewer`؛ لم ينفذ الشاشات. المنفذون مسجلون بالاسم في [سجل الأحكام](design-audit-conformance-record.json). استُخدم المصدر للقراءة فقط لتأكيد سبب الفروق المرئية، ولم يُستخدم بديلاً عن الصور.

| المادة | البصمة SHA-256 |
|---|---|
| المصدر الحالي | `e56cb5e3ed516fc3fc4087a9f63e3dd273144e16cfdc45cbbe1e04d22521fe57` |
| [المرجع البصري](design-reference.html) | `a980babc438fbd667dbf621a7a0e246de27b4178a4d071b90065336f4b28a884` |
| [الجرد](screen-inventory.json) | `4b32d7a09513f43ecb11cba817fc41c7ab3294e542d7b24bf2266c6f2602c1c8` |
| [رموز التصميم](design-tokens.md) | `2122771e81d0fddb8b9b11516d82d34b1d0afb958f26664a276d0c99d70667f2` |
| [manifest الحالي](../../evidence/screenshots/data-search-agent-conform-e56cb5e3/manifest.json) | `921963b9ada24440cf29009adaecaf39e9ec2c892314e6f0fa773acbc1b19d25` |
| سجل أحكام هذا التدقيق | `fd77c30b227cb400436ccf7871850bddd4a2e97771b603d4fea392996f08f784` |

فُحصت بصمات الصور272 فعلياً: لا ملف متغير أو مفقود؛ مجموعها33,345,505 bytes. أكدت قراءة `source_fingerprint` البصمة الحالية أعلاه. تُحفظ الرحلات والأسلاك وبقية بصمات الحزمة المعتمدة كاملة في السجل. لم يُعدّل المرجع لتقليل الفروق.

| التكوين | اللغة/الاتجاه | المظهر | العرض | مقياس الخط |
|---|---|---|---:|---:|
| C1 | en/LTR | light |390dp|1.0|
| C2 | ar/RTL | dark |390dp|1.0|
| C3 | en/LTR | dark |840dp|1.0|
| C4 | ar/RTL | light |840dp|1.3|

استُخدمت20 لوحة مقارنة أساسية، كل منها يضع رندرة HTML المعتمد إلى جوار Compose للتكوين المقابل، و58 لوحة حالات، كل منها يحتوي التكوينات الأربعة:40 صورة أساسية و232 صورة حالات. استُكمل الفحص المحفوظ248 صورة بآخر24 صورة لنطاق الإجابة، ثم أعيد فتح المقارنات المحددة لحسم اختلافات الطباعة والأركان والشرائح. صور المرجع محفوظة في `docs/delivery/evidence/design-qa-reference-3f3ab2bd/`؛ تسمية المجلد تاريخية ولا تعني استعمال مصدر قديم للحكم على التنفيذ الحالي. الصور الحالية كلها من manifest `e56cb5e3`.

## الانحرافات المسجلة

جميع الملاحظات أدناه بدرجة **P3 بصرية**؛ لا يوجد هنا ادعاء بأنها خطأ تشغيل أو فقد بيانات. تكفي مخالفة عنصر غير معتمد لإخفاق المطابقة. لا يتضمن هذا التقرير اقتراح إصلاح أو تكليفاً بالتنفيذ.

| الملاحظة والعناصر | التكوين/الحالة | المرجع مقابل الفعلي | الدليل |
|---|---|---|---|
| AUD-01 — `DS001-04` | C1–C4، المحتوى | رموز §4 تجعل أركان البطاقة16dp، وHTML يقارب15px. بطاقات المساحات الحالية أكثر استدارة:20dp من الشكل المتوسط الافتراضي. الأيقونة والاسم والعدادات موجودة؛ المشكلة في الشكل. | [compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-001-compact.png)، [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-001-expanded.png). تأكيد للقراءة: `DataSearchScreens.kt:238` و`AizzakTheme.kt:63`. |
| AUD-02 — `DS001-07`، `DS002-11`، `DS009-09` | C1–C4، empty | رمز عنوان الفراغ في §3 هو22sp/28sp/Bold. العناوين الحالية أخف بصرياً، وترث `headlineSmall` الافتراضي غير المعدل؛ لا تحقق رمز الفراغ الغليظ. ظهور هوية RAG والنص والأفعال صحيح، لكنه لا يلغي فرق الطباعة. | [لا مساحات](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-001--empty-states.png)، [محادثة فارغة](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-002--empty-states.png)، [نطاق فارغ](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-009--empty-states.png). تأكيد: `DataSearchScreens.kt:318,1253`، `AizzakTheme.kt:47`. |
| AUD-02 — `DS003-07`، `DS006-09` | C1–C4، empty/filtered_empty | العنوان نفسه محدد22sp/28sp/Bold. الفعلي أصغر وأقل بروزاً: `titleMedium` بقيم16sp/22sp/Semibold. حاويات الفراغ والأفعال موجودة. | [المحادثات](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-003--empty-states.png)، [المصادر](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-006--empty-states.png)، [مرشح فارغ](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-006--filtered_empty-states.png). تأكيد: `DataSearchScreens.kt:502,713`. |
| AUD-03 — `DS005-03`، `DS010-03` | C1–C4، حوارا الحذف | عنوانا `h1` في HTML المعتمد غليظان. الفعلي أكبر وأخف بوضوح باستخدام `headlineSmall` الموروث. يتضمنان اسم المحادثة/الملف كما يطلب الجرد؛ اختلاف الوزن لا يُفسر باختلاف اسم العينة أو تنعيم الحروف. | [حذف محادثة compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-005-compact.png)، [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-005-expanded.png)، [حذف مصدر compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-010-compact.png)، [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-010-expanded.png). تأكيد: `DataSearchScreens.kt:1034`. |
| AUD-04 — `DS002-02`، `DS006-04` | C1–C4، الحالات التي تُظهر الشرائح | §4 يحدد حبة كاملة للشرائح؛ `.chip` في HTML يستخدم `primaryContainer`، وشريحة النطاق وسط الصف. الفعلي مستطيلات بزوايا محدودة، والاختيار بحاوية `secondaryContainer` السماوية، وصف النطاق بمحاذاة start. العدد والمرشحات مكتوبة بوضوح؛ الفروق في الشكل واللون والمحاذاة. | [النطاق compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-002-compact.png)، [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-002-expanded.png)، [المرشحات compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-006-compact.png)، [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-006-expanded.png). تأكيد: `DataSearchScreens.kt:299,693`. |

## تعارض القياسات داخل المرجع

**AUD-05 — `DS002-13` و`DS006-11`، التكوين C3 فقط.** الفعلي عند840dp ومقياس1.0 يعرض نحو210dp للمحادثات و420dp للوسط و210dp للمصادر، مع حدود1dp. §5 من الرموز يحدد260–300dp للمحادثات و300–340dp للمصادر. لذلك سُجل العنصران `deviation` عند القراءة الصارمة للقياسات، وبقي الوسط الأوسع قابلاً للقراءة كما يطلب الجرد.

يوجد تعارض داخل الحزمة: HTML نفسه يستخدم نسب27%/30% مع rail عام، فتقل أجزاؤه عند840dp عن المديات المكتوبة أيضاً؛ كما أن جمع الحدين الأدنى260+300dp يترك280dp فقط للوسط، فلا يمكن أن يكون الوسط أعرض من جزء المصادر في هذا العرض. هذه **ليست ملاحظة شكلية مثبتة على التنفيذ وحده**، ولا قراراً من المراجع بتعديل التصميم. لم أفترض أولوية تحذف أحد القيدين، ولم أمنح استثناءً، ولذلك أبقيت هذين العنصرين ظاهرين منفصلين عن العناصر العشرة ذات الفروق البصرية المؤكدة.

الدليل: [المحادثة expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-002-expanded.png)، [المصادر expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-006-expanded.png)، ورموز §5–6؛ تأكيد قياس الفعلي للقراءة فقط في `DataSearchScreens.kt:268,274`. انهيار الأجزاء في C4 صحيح وفق قاعدة `fontScale>=1.3`، ولم يُحسب انحرافاً.

## فروق مرئية لا تُحسب عدم مطابقة

هذه الفروق موثقة حتى لا يُفهم `matched` على أنه تطابق بكسلي. اختلاف خط المنصة الطبيعي لا يشمل اختلاف الوزن أو الرمز المحدد في AUD-02/03.

| العناصر/التكوينات | المرجع مقابل الفعلي وسبب الحكم | مثال الدليل |
|---|---|---|
| الشاشات كلها، C1–C4 | إطار جهاز HTML وشريط الحالة التجريبي وارتفاعه844/760px مقابل مساحة التقاط Compose1000dp، مع اختلاف تنعيم خط النظام. هذه فروق عرض الدليل؛ ليست عناصر واجهة مفقودة. | [مقارنة المساحات](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-001-compact.png) |
| `DS001-01/02`، C1–C4 | HTML يكرر عنوان/eyebrow المساحات في الجسم تحت شريطه؛ الفعلي يجمعهما في شريط التطبيق ويعرض شرح الملكية مرة واحدة. هذا فرق تقديم مسجل يحقق موضع العناصر المطلوب صراحة في الجرد، وليس فرق تنعيم خط. | [المساحات](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-001-compact.png) |
| `DS001-04/05`، `DS002-02..08`، `DS003-03/04`، `DS008-02`، C1–C4 | أسماء Operations/Product مقابل Product knowledge/Operations، وعدادات وأوقات وأسماء ملفات ورسائل مختلفة، واختيار مصدرين مقابل «الكل»، محتوى fixtures. حالات المؤشر/الانقطاع والمراجع المنظمة مقصودة لتغطية الجرد؛ لا يتطلب الجرد نفس البيانات التجريبية. اختلاف شكل بطاقات/شرائح تلك العناصر محسوب أعلاه بصورة مستقلة. | [المحادثة](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-002-compact.png)، [التفاصيل](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-008-compact.png) |
| `DS004-05`، C1–C4 | محدد النموذج غير ظاهر في HTML المبسط، ويظهر في العينة ذات القدرة المؤكدة. الجرد والرموز يجعلان ظهوره مشروطاً؛ وجوده ليس عنصراً مخترعاً في هذه العينة. | [التحرير](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-004-compact.png) |
| `DS005-01`، `DS010-01`، C1–C4 | HTML يعيد خلفية المحادثة لكلا الحوارين؛ الفعلي يُظهر قائمة المحادثات لحذف محادثة وتفاصيل المصدر لحذف مصدر. هذان السياقان هما النص الصريح في الجرد، وحُكما مطابقين. لا يُستعمل اختلاف الخلفية لإخفاء اختلاف عنوان الحوار. | [حذف محادثة](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-005-compact.png)، [حذف مصدر](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-010-compact.png) |
| `DS006-04/08`، `DS007-04`، `DS008-03/04`، C1–C4 | HTML يختصر المرشحات والمراحل والحالات. الفعلي يقدم المرشحات الأربعة والأفعال المنفصلة والمراحل الست وحالات uploaded/pending وغيرها التي يطلبها الجرد؛ الزيادة المطلوبة بالجرد ليست انحرافاً. شكل الشرائح فقط محسوب في AUD-04. | [الإضافة](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-007-compact.png)، [pending](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-008--index_pending-states.png) |
| `DS002-13`، `DS006-11`، C4 | HTML يستمر في ثلاثة أجزاء عند مقياس1.3؛ الفعلي ينهار إلى جزء رئيسي. قاعدة التكيف المكتوبة صريحة في طلب الانهيار، لذلك هذا الجانب مطابق؛ تعارض العرض في C3 يبقى مسجلاً أعلاه. | [المحادثة expanded، الصف العربي](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-002-expanded.png) |
| `DS010-05`، C1–C4 | لم يظهر ادعاء تطهير index/chunks في الحوار. العنصر مشروط بتأكيد العقد، وليس نصاً واجب الظهور دائماً. غيابه في العقد الحالي مطابق. | [حذف المصدر](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-010-compact.png) |

## تغطية الشاشات والعناصر

الأحكام التسعون فردية مع وصف الجرد والملاحظة ومرجع الصورة في [السجل الكامل](design-audit-conformance-record.json). كل سطر أدناه يغطي C1–C4؛ عدد الصور يتضمن الحالات الإضافية، وليس عدد الشاشات المختلفة.

| الشاشة | الصور المفحوصة | مطابق/انحراف/مفقود | مقارنة مرئية |
|---|---:|---|---|
| SCR-DS-001 |20|6/2/0|[compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-001-compact.png) · [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-001-expanded.png)|
| SCR-DS-002 |28|10/3/0|[compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-002-compact.png) · [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-002-expanded.png)|
| SCR-DS-003 |28|7/1/0|[compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-003-compact.png) · [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-003-expanded.png)|
| SCR-DS-004 |32|8/0/0|[compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-004-compact.png) · [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-004-expanded.png)|
| SCR-DS-005 |4|6/1/0|[compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-005-compact.png) · [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-005-expanded.png)|
| SCR-DS-006 |28|8/3/0|[compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-006-compact.png) · [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-006-expanded.png)|
| SCR-DS-007 |48|9/0/0|[compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-007-compact.png) · [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-007-expanded.png)|
| SCR-DS-008 |48|9/0/0|[compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-008-compact.png) · [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-008-expanded.png)|
| SCR-DS-009 |32|8/1/0|[compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-009-compact.png) · [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-009-expanded.png)|
| SCR-DS-010 |4|7/1/0|[compact](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-010-compact.png) · [expanded](../../evidence/design-repair-contact-e56cb5e3/SCR-DS-010-expanded.png)|

غطت58 لوحة الحالات: الفراغ وfiltered-empty، والتحميل والخطأ وpartial، وoffline ومنع الوصول وانتهاء الجلسة؛ الإرسال والانقطاع المنفردان؛ الإنشاء/إعادة التسمية والتحقق من الاسم؛ اختيار مصدر والنص ومراحل الرفع والإكمال والإخفاق والانتهاء والحجر والاستعادة والجاهزية دون فهرسة؛ سلامة uploaded/scanning/quarantined وحالات not-started/pending/indexing/failed للفهرس. صور المحتوى تغطي ready/indexed. لا تُستنتج حالة من صورة لحالة أخرى.

اللون العنبرى للعمل/الانتباه وحاوية الخطأ للفشل/الحذف وصورة الملف والمساعد وحافة المساعد والسياقات خلف الحذف والأفعال12dp والتفريق بين السلامة والفهرسة موجودة في المصدر الحالي. أصبحت حالات offline ذات البيانات المحملة مقروءة مع تعطيل التعديل في المحادثة والمصادر والتفاصيل والتحرير، وتُخفى الأسماء المحمية في permission/session. بقيت فروق الخط والبطاقة والشرائح والقياسات أعلاه، لذلك لا يُدعى التطابق الكامل.

## الفحص الفعلي وحدوده

الأمر المنفذ من جذر المشروع:

```powershell
python -X utf8 -B 'C:\Users\anast\Desktop\aizzak-android\.codex\android-software-house\runtime\scripts\suite.py' conformance --project 'C:\Users\anast\Desktop\aizzak-android' --task 'data-search-agent' --record 'docs/delivery/tasks/data-search-agent/design-audit-conformance-record.json'
```

الخروج الفعلي: **1**. [النتيجة الفعلية المحفوظة](design-audit-conformance-result.json): `status=failed`، `matched=78`، `deviation=12`، `missing=0`، `approved_deviations=[]`. جميع الفحوص الأربعة `reference_locked/inventory_complete/screenshots_current/independent_design_review=true`؛ سبب الإخفاق هو العناصر الاثنا عشر المسجلة دون استثناءات معتمدة. عبارة الأداة التي تذكر `suite.py deviation` نص آلي محفوظ، وليست طلباً من هذا التدقيق لاعتماد استثناء.

هذه صور Roborazzi حقيقية على JVM وليست لقطات جهاز متصل. لا تثبت الصور وحدها TalkBack أو ترتيب التركيز أو عودته أو الظهور فوق IME أو مساحة اللمس غير المرئية أو بث الشبكة أو عدم تكرار الطلبات أو تغيير الصلاحيات. قراءة ضوابط الأحجام وأدلة QA المنفصلة لا تحول ذلك إلى اختبار جهاز. فُحص اتجاه RTL والتفاف النص في C4 بصرياً؛ لا يوجد اقتطاع غير مفسر بالتمرير في الصور المعروضة. اختبار التباين اللوني الحسي هنا لا يعادل قياساً آلياً شاملاً لكل زوج ألوان/حالة تركيز.

لم يُعد تشغيل Gradle أو تعديل دليل الالتقاط؛ استُخدمت مجموعة272 الحالية التي وثقها QA. لم يتغير سجل الجولة السابقة الفاشلة أو نتيجتها، ولم تتغير أي مرحلة رسمية بهذا التدقيق. الملفات الثلاثة الجديدة فقط هي تقرير التدقيق وسجل أحكامه ونتيجة الأداة.
