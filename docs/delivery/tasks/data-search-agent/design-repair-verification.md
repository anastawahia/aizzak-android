# تحقق إصلاح مطابقة Data Search

- التاريخ: 2026-11-08.
- المهمة: `data-search-agent`، المرحلة: `verification`؛ النطاق `DS-REQ-01..15` و`SCR-DS-001..010`.
- المنفذ: `qa-engineer`، الوكيل `/root/visual_evidence_qa`؛ لم يعدل مصدر واجهة التطبيق.
- الحالة الحالية: **نجح التحقق النهائي بعد إصلاح حارس الصلاحية، وسجلت 272 لقطة جديدة للمراجعة المستقلة**. لا يثبت ذلك وحده مطابقة التصميم أو قبول المالك. نتيجة المطابقة المستقلة التاريخية للجولة الأولى: `62 matched / 28 deviation / 0 missing`، و`conformance failed`، كما هو موثق في `design-repair-findings-round1.md`.
- البصمة الحالية التي تجمع الاختبارات والصور: `e56cb5e3ed516fc3fc4087a9f63e3dd273144e16cfdc45cbbe1e04d22521fe57`؛ بقيت مطابقة لقراءة lifecycle قبل التحقق وبعد التسجيل.
- البصمة الناجحة تاريخياً للجولة الأولى: `72cc95c013a8d37a0c19cbf49a2273f292f4be21a7beea2b830f1e1cdd81b9a6`؛ الأدلة أدناه تخصها ولا تثبت تغييرات الدفعة التالية.

## مصفوفة اللقطات

أضيفت 232 حالة إلى الأربعين الأساسية في `DataSearchScreenshotTest.kt`، ليصبح العدد 272 حالة حقيقية تعتمد على `DataSearchContent` وحالة النموذج. تغطي كل حالة التكوينات الأربعة المعتمدة: EN/light/390/1.0، AR/dark/390/1.0، EN/dark/840/1.0، AR/light/840/1.3.

تشمل الحالات empty وfiltered-empty وoffline وpermission وsession، loading وerror وpartial، إنشاء/إعادة تسمية المحادثة والتحقق من المدخل، إرسال/انقطاع الرسائل، اختيار المصدر ومراحل الرفع النهائية، وحالات سلامة الملف والفهرس المستقلة بما فيها `UPLOADED` و`PENDING`. تحفظ نسخ الحالات في مجلدات مسماة، مع الإبقاء على اسم الملف القياسي `SCR-ID__locale_theme_wWIDTH_fsSCALE.png`. لا يصطنع الاختبار صوراً أو حالة خادم أو شبكة.

## الفحوص المنفذة والإخفاقات المحفوظة

الأمر في الجولات الكاملة:

```text
python -B C:/Users/anast/Desktop/aizzak-android/.codex/android-software-house/runtime/scripts/suite.py verify --project C:/Users/anast/Desktop/aizzak-android --kind all --execute --attempts 1 --timeout 1200
```

1. الجولة الأولى، بصمة `c062502e078342a3ee650f2111bd06de75d8806b02f99ed80404d09345d96d72`: exit 1؛ نفذ 660 اختباراً، وفشل اختبارا Paging فقط بسبب محدد النص القديم `○ Conversation 24`. الدليل الأصلي: `docs/delivery/evidence/data-search-design-repair-all-first-failure-c062502e.json`، مع XML الفشل المحفوظ. أعيدت الملاحظة إلى مالك التنفيذ، وصحح المحدد مع إبقاء تأكيد اختيار المحادثة وتحميل الصفحة التالية.
2. الجولة الثانية، بصمة `631944e32e399e82393e723e2ef17cc0e3437ed1cf79a46bc670cac12ccf881b`: exit 1؛ فشل تجميع `DataSearchModalIsolationTest` بسبب غياب opt-in لـ`ExperimentalTestApi`. **لم تنفذ الاختبارات في هذه الجولة**؛ تعداد XML في JSON يعود إلى الجولة السابقة. الدليل الأصلي: `docs/delivery/evidence/data-search-design-repair-all-round2-compile-failure-631944e3.json`. بتفويض المدير أضيفت فقط `@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)` إلى الاختبار، دون تعطيل أو تغيير التأكيدات.
3. الجولة الثالثة، بصمة `16bd38e0d02a74094bdc97c1db0204534e36908da24874d94ac5d090c33ab11c`: exit 1؛ نفذ 662 اختباراً، وفشل اختبارا عزل الحوار فقط عند تأكيد تركيز Cancel بعد نقرات خارج الحوار. تأكيدات منع تشغيل أفعال الخلفية بالنقر نجحت، واختبارات Paging نجحت. الدليل الأصلي: `docs/delivery/evidence/data-search-design-repair-all-round3-modal-failure-16bd38e0.json` وXML تحت `docs/delivery/evidence/test-results/data-search-design-repair-modal-failure-16bd38e0/`. أعيدت مسألة وضع الإدخال اللمسي/لوحة المفاتيح والتركيز إلى مالك التنفيذ للتحقق المركز.

لم تُعدل نتائج JSON الفاشلة إلى ناجحة. حُفظت هذه الإخفاقات بوصفها أدلة تاريخية ولا تستعمل وحدها لإثبات نجاح.

## تحقق الجولة الأولى الناجح

- بعد نجاح اختبار عزل الحوار المركز (2/2؛ XML في `docs/delivery/evidence/test-results/data-search-design-repair-modal-focused/`)، أعيد الأمر الكامل على المصدر المجمد أعلاه: **exit 0، BUILD SUCCESSFUL in 1m 25s**.
- الدليل الخام الأصلي: `docs/delivery/evidence/data-search-design-repair-all-72cc95c0.json`.
- نفذت الاختبارات فعلياً: **662 اختباراً** على مستوى المشروع، منها **558 اختبار Data Search في 20 suite**؛ صفر failures/errors/skipped في المجموعتين. تشمل 272 حالة الاختبار الخاصة باللقطات؛ تنفيذ unit العادي لم يكتب PNG، ولذلك يلزم تسجيل Roborazzi المقيد.
- Lint: **صفر أخطاء و47 تحذيراً** على مستوى المشروع، وصفر issue تحت `feature/datasearch`.
- حفظت جميع تقارير JUnit ونسخة Lint XML قبل مهمة التسجيل المنفصلة تحت `docs/delivery/evidence/test-results/data-search-design-repair-72cc95c0/`.
- أدى تعديل التركيز إلى طلب التركيز عند تغير وضع الإدخال الفعلي إلى لوحة المفاتيح؛ الاختبار يدخل وضع لوحة المفاتيح قبل تأكيد Cancel الأولي، ثم ينفذ نقرات خارج الحوار ويعود إلى لوحة المفاتيح للتحقق من Cancel ودورة Tab/ShiftTab وEnter. لم تُحذف تأكيدات العزل أو الأفعال.

هذه نتيجة QA للبناء والاختبارات، وليست حكماً على مطابقة التصميم أو قبول المالك. لا يعدل هذا الوكيل lifecycle.

## تسجيل لقطات الجولة الأولى الفعلي

```text
gradlew.bat :app:recordRoborazziDebug --tests android.aizzak.com.feature.datasearch.DataSearchScreenshotTest --console=plain --no-daemon
```

- **exit 0، BUILD SUCCESSFUL in 1m 31s**؛ تقرير التسجيل JUnit يؤكد 272 tests وصفر failures/errors/skipped.
- سجل الأمر والناتج الحقيقي: `docs/delivery/evidence/data-search-design-repair-screenshots-72cc95c0.json`.
- `manifest.json`: `docs/delivery/evidence/screenshots/data-search-agent-conform-72cc95c0/manifest.json`، يحتوي **272 PNG بمجموع 33,200,489 bytes** و28 فئة حالة، مع config وSHA-256 لكل صورة.
- حُفظ تقرير التسجيل المنفصل باسم `TEST-recordRoborazzi-DataSearchScreenshotTest.xml` بجوار نسخة تقارير الاختبار الكامل، فلا تطمس نتيجة اللقطات المنفصلة عدد الاختبارات الكامل السابق.
- حُفظت مجلدات الحالات الفرعية وأسماء الملفات القياسية؛ نسخت PNG الأصلية دون تغيير البايتات. أدلة الحالة الأساسية الأربعون موجودة مباشرة داخل مجلد الأدلة، والحالات الإضافية داخل مجلدات `empty` و`error` و`offline` وغيرها.
- فُحصت بصرياً الصور الحقيقية التالية قبل تسليمها: `error/SCR-DS-004__ar_light_w840_fs1.3.png` (العنوان المدخل باقٍ مع الخطأ)، `upload_quarantined/SCR-DS-007__en_light_w390_fs1.0.png` (مراحل الرفع والحجر وDone المعطل)، `SCR-DS-010__ar_dark_w390_fs1.0.png` (سياق التفاصيل خلف scrim وأيقونة الحذف)، `index_pending/SCR-DS-008__en_dark_w840_fs1.0.png` (فصل سلامة الملف عن الفهرسة pending وتعطيل التثبيت). ظهرت النصوص والأفعال المذكورة بلا قص مرئي في هذه العينات.
- أعاد `suite.py lifecycle` بعد انتهاء التسجيل البصمة `72cc95c013a8d37a0c19cbf49a2273f292f4be21a7beea2b830f1e1cdd81b9a6` نفسها؛ لا تغيير مصدر أثناء التنفيذ النهائي.
- أُرسل manifest إلى المراجع المستقل `/root/independent_design_match` لفحص الشاشات والعناصر كافة. فحص العينات أعلاه لا يحل محل حكم مطابقة التصميم.

## تحقق دفعة الفروق المرئية قبل إصلاح حارس الصلاحية

- البصمة: `ec8effdee96ab10497b8026b9e07d8cfc9a3eeca98bc621ef73a9907834e5e62`؛ طابقت قراءة lifecycle قبل الأمر وبعد انتهاء Gradle.
- الأمر الكامل أعلاه: **exit 0، BUILD SUCCESSFUL in 1m 24s**. الدليل الخام: `docs/delivery/evidence/data-search-design-repair-all-ec8effde.json`.
- نفذ **671 اختباراً** على مستوى المشروع، منها **567 اختبار Data Search في 22 suite**، وصفر failures/errors/skipped؛ تشمل اختبارات السياق offline والتحقق من الاسم الجديدة.
- Lint: صفر أخطاء و47 تحذيراً على مستوى المشروع، وصفر issue تحت `feature/datasearch`. نسخ JUnit وLint قبل أي تسجيل منفصل محفوظة تحت `docs/delivery/evidence/test-results/data-search-design-repair-ec8effde/`.
- أوقف تسجيل اللقطات لهذا المصدر بتوجيه المدير بعد أن كشفت المراجعة البرمجية P2: `OFFLINE` مع حالة جزء المحادثة `PERMISSION_DENIED/SESSION_EXPIRED` قد يعرض عنوان إعادة التسمية. ينتظر إصلاح الحارس واختباره ثم إعادة التحقق على بصمة جديدة؛ نجاح الاختبارات الحالي لا يتجاوز هذه الملاحظة.

## التحقق النهائي والصور بعد إصلاح حارس الصلاحية

- صحح مالك التنفيذ حارس عرض نموذج المحادثة offline بحيث يحجب عنوان ومدخل إعادة التسمية عند رفض الصلاحية أو انتهاء الجلسة في جزء المحادثة، مع إبقاء السياق المصرح للقراءة. أدلة اختباره المركز 12/12 محفوظة تحت `docs/delivery/evidence/test-results/data-search-design-repair-offline-guard-focused/`.
- الأمر الكامل نفسه على البصمة الحالية: **exit 0، BUILD SUCCESSFUL in 1m 22s**؛ الدليل الأصلي `docs/delivery/evidence/data-search-design-repair-all-e56cb5e3.json`.
- نفذت **675 اختباراً** على مستوى المشروع، منها **571 اختبار Data Search في 22 suite**؛ صفر failures/errors/skipped في المجموعتين. Lint صفر أخطاء و47 تحذيراً مشروعياً، وصفر issue داخل `feature/datasearch`.
- حفظت تقارير الاختبار الكامل وLint قبل التسجيل تحت `docs/delivery/evidence/test-results/data-search-design-repair-e56cb5e3/`.
- أعيد تسجيل الاختبار المقيد نفسه: **exit 0، BUILD SUCCESSFUL in 1m 32s**، و272 اختبار لقطة بلا failures/errors/skipped. سجل الأمر والناتج: `docs/delivery/evidence/data-search-design-repair-screenshots-e56cb5e3.json`؛ وتقرير JUnit المنفصل `TEST-recordRoborazzi-DataSearchScreenshotTest.xml` بجوار أرشيف التقارير الكامل.
- manifest الحالي: `docs/delivery/evidence/screenshots/data-search-agent-conform-e56cb5e3/manifest.json`؛ **272 PNG بمجموع 33,345,505 bytes** و28 فئة حالة، مع التكوين وSHA-256 لكل صورة. جميع الصور كتبت خلال التسجيل الحالي، وحفظت مجلدات الحالات والبايتات الأصلية؛ لم تتغير الأدلة التاريخية `72cc95c0`.
- فُتحت أربع عينات حقيقية جديدة: `offline/SCR-DS-002__en_light_w390_fs1.0.png` أظهر السجل والمسودة مع التنبيه وتعطيل الإرسال؛ `validation/SCR-DS-004__en_light_w390_fs1.0.png` أظهر سبب لزوم الاسم مع Create معطل؛ `empty/SCR-DS-002__ar_light_w840_fs1.3.png` أظهر هوية RAG الوسطية والدعوة والمؤلف؛ `permission_denied/SCR-DS-006__ar_dark_w390_fs1.0.png` أظهر دور الخطأ وحجب أسماء المصادر. النصوص والأفعال المذكورة ظهرت بلا قص مرئي في هذه العينات؛ السجل الطويل يظل قابلاً للتمرير.
- أرسل manifest مباشرة إلى `/root/independent_design_match` بعد حفظه، لمراجعة كل العناصر والتكوينات. لا يعد فحص عينات QA حكماً على المطابقة الكاملة، ولا يعدل هذا الوكيل lifecycle أو يعتمد انحرافاً.

## البيئة والحدود

- `suite.py doctor --devices`: exit 0، Windows، Java 17.0.19، SDK `D:/SDK`، Gradle 8.13، AGP 8.13.2، Kotlin 2.3.0؛ `usable_devices=[]`.
- لا اختبارات جهاز أو TalkBack/IME. لقطات Roborazzi دليل تخطيط JVM ولا تثبت rendering جهاز فعلي.
- لا تعديل على الباك إند أو Home أو التنقل أو الشبكة أو التصميم المعتمد أو lifecycle من هذا الوكيل.
- `suite.py screenshots` يجمع صور Home القديمة ويفلطح أسماء الصور إلى مجلد واحد؛ هذا يتعارض مع الحالات الفرعية ذات الاسم القياسي نفسه. استُخدم تسجيل Roborazzi المقيد الموثق أعلاه، مع سجل أمر حقيقي وmanifest يحافظ على مجلدات الحالات وhashes، دون حذف صور خارجة عن الميزة.
