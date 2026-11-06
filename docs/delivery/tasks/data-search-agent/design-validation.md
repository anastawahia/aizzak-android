# تحقق تصميم Data Search Agent — مواءمة API وتوحيد النظام البصري

الحالة: `passed` للتحقق الوثائقي والساكن قبل نقطة اعتماد التصميم  
التاريخ: 2026-11-06  
النطاق: `DS-REQ-01..13` و`SCR-DS-001..010`

## توحيد التصميم مع المرجع العام

المرجعان الحاكمان لهذا التحديث هما:

- `docs/design/UI-PREVIEW.html` ببصمة `D20DC95814303301C5DFB0146F8EC25EA717D4ECCA64E28D5E4AC6E125F98D0C`.
- `docs/design/UI-DESIGN.md` ببصمة `8B9C00244E6571B50E2CCB03FD3F8F6BCFF643E196D8E518F333BC426E008684`.

نُفذ فحص PowerShell محلي يقرأ متغيرات CSS من `.phone/.phone.dark` و`.device/.device.dark` ويقارن 18 دوراً في كل مظهر، ثم يتحقق من overrides التوزيع الدلالي الثمانية في نهاية `<style>`.

- أدوار الألوان المفحوصة: 36؛ الاختلافات: 0.
- overrides التوزيع المطلوبة: 8؛ المفقود: 0.
- Chrome المراجعة يطابق خلفية الصفحة وحدود عناصر التحكم والجهاز والملاحظة والظل وحاوية المسودة في `UI-PREVIEW.html`.
- الوجهة النشطة تستخدم `primary/onPrimary`.
- eyebrow والتحميل وشريحة المساحة تستخدم الأزرق العام؛ بقي السماوي لأيقونات هوية Data Search، حافة المقتطف، وحالات `ready/indexed` ذات النص.
- لم تتغير الرحلات أو النصوص أو عناصر الشاشات أو عقد API؛ لذلك بقي `screen-inventory.json` دون تعديل.

## سلطة العقد

قُرئت حزمة `docs/api` قبل هذا التحديث. مصدرها المعلن:

- الفرع: `chore/public-file-links-closing`
- الالتزام: `ff547ad8228741a532077b8eaf8fa2eb928be782`
- تاريخ التوليد: 2026-10-22

ليست الحزمة دليلاً على `master` العامل. لذلك يعكس التصميم المسارات والحقول الموثقة، ويعرض في أدوات المراجعة تحذيراً بأن التنفيذ ينتظر تأكيد الباك إند. لم يُعدل الباك إند.

## الملفات والبصمات

| الملف | SHA-256 |
|---|---|
| `ux-journeys.md` | `B2D277811E45868196C971467BED0D8D129B5BB6A1CFEBB1A56AFD6EAEB37CF7` |
| `wireframes.md` | `E9DEE56A346E5C4129562FDF1DDDF1FF4BB81B1BC8F823CFA33F5DDE1B098509` |
| `design-reference.html` | `D03F7F917FD8EAC830F9108EF33F6C2CDCB081496F850E94F336B83B1E115C7E` |
| `design-tokens.md` | `5C9030854CF1723807F1F2FEBA65EE48441EBA194BF90BD03EE73392CE917ED6` |
| `screen-inventory.json` | `D13B637E885437ECB69BD7F2DE3B1BCF4E12FBA4816EC2E21412655A708A8816` |

## فحص الجرد

الأمر:

```powershell
python "C:\Users\anast\Desktop\aizzak-android\.codex\android-software-house\runtime\scripts\suite.py" inventory --file "C:\Users\anast\Desktop\aizzak-android\docs\delivery\tasks\data-search-agent\screen-inventory.json"
```

النتيجة: رمز خروج `0`، و`status: passed`، و`errors: []`.

- الشاشات: 10 (`SCR-DS-001..010`).
- العناصر: 106، بمعرفات مستقرة وفريدة.
- اللغات: الإنجليزية والعربية.
- التكيف: compact وexpanded ومقياس 1.3.
- التكوينات المطلوبة: en/light/390/1.0، ar/dark/390/1.0، en/dark/840/1.0، ar/light/840/1.3.

## JavaScript وترابط المرجع

استُخرج `<script>` من `design-reference.html` وشُغّل:

```powershell
node --check -
```

النتيجة: رمز خروج `0` بلا أخطاء صياغة. يحتوي map التنفيذ على الشاشات العشر، وكل `reference_anchor` في الجرد يقابل معرفاً موجوداً في المرجع.

أضيف دعم إعدادات اللقطة داخل fragment بعد `|` حتى يمكن تشغيل التكوينات محلياً من دون شبكة أو تعديل الملف.

## دليل Edge التاريخي

شُغّل `msedge.exe --headless=new --dump-dom` في التحقق السابق بملف مستخدم مؤقت منفصل. يثبت الجدول اكتمال المحتوى والتكيف قبل تعديل الألوان، لكنه لا يُعد دليلاً بصرياً حالياً بعد التوحيد:

| الشاشة/التكوين | Exit | أحرف `#product-screen` | النتيجة |
|---|---:|---:|---|
| SCR-DS-001 · en/light/compact | 0 | 3055 | passed |
| SCR-DS-009 · ar/dark/compact | 0 | 998 | passed، RTL |
| SCR-DS-003 · en/dark/expanded | 0 | 2696 | passed |
| SCR-DS-004 · ar/light/compact/1.3 · quarantined | 0 | 1577 | passed، RTL |
| SCR-DS-006 · ar/light/expanded/1.3 | 0 | 1477 | passed، RTL |
| SCR-DS-010 · en/dark/compact | 0 | 1040 | passed |

كان stderr فارغاً في الحالات الست.

## اللقطات المرئية التاريخية

| الملف | التكوين | SHA-256 |
|---|---|---|
| `design-preview.png` | Dashboard · en/light/compact | `0DEFA6818E693479771FFCE64592FADC41B6A3E866F291C7FF0235D1E0FAE1B1` |
| `design-preview-space-ar.png` | Choose space · ar/dark/compact | `21B0CAFCB8179B94D743ACC3CD27ABF6FAC6BDB2D0855FDD0048999520529D61` |
| `design-preview-library-wide.png` | Library · en/dark/expanded | `4DD84F5D34A1423E3593EF12BA1C7C05F2BCB880FFD32C6C71298AE734A7B960` |
| `design-preview-results-ar-wide.png` | Results · ar/light/expanded/1.3 | `53D877BA072AD083BF504444AC7DAD11D4D509451EEE33BFC0DF13900E4715CA` |
| `design-preview-replace-dark.png` | Replace warning · en/dark/compact | `7F27A32309388BC6FF524A9AD0D8B134D9D6B089AA64869D8057C921D5B01C34` |

فُحصت هذه اللقطات في التحقق السابق. تظهر عناوين ووصف بطاقات الإضافة في أسطر منفصلة، وسلامة الملف وفهرسة البحث كسطرين مستقلين، ولا يوجد قص ظاهر في العربية 1.3 أو الحوارات. لكنها تسبق توحيد التوزيع اللوني ولا تُستخدم كمرجع اعتماد للون الحالي؛ المرجع الحالي هو HTML ذو البصمة أعلاه.

## مطابقة عقد API في التصميم

- `space_id`: اختيار مساحة موجودة وحالة no-space مانعة؛ لا إنشاء أو إدارة.
- الرفع: تسجيل → رابط رفع موقع → complete → `uploaded/scanning/ready/quarantined`.
- الفهرسة: تبدأ بعد ready فقط → 202 → `pending/indexing/indexed/failed`.
- النص: اقتراح Android يحوله إلى UTF-8 `text/plain` ويمر بمسار الملف نفسه؛ لا endpoint نص مختلق.
- المكتبة: دمج `FileOut.id` مع `DocumentOut.file_id`، وcursor مستقل للقائمتين.
- البحث: top-k داخل مساحة واحدة، افتراضي 5 وحد 50، بلا cursor في الإدخال.
- النتيجة: text وfile_name وpage_number وsection عند توفرها؛ لا score ظاهر ولا إجابة مولدة.
- الحذف: حذف ناعم للملف وتطهير غير قابل للتراجع للمستندات والقطع والفهرس.
- تطابق الاسم: تحذير best-effort قبل التسجيل مع تصريح باحتمال السباق.

## قيود الدليل

- لم يُنفذ Android build أو Compose preview أو screenshot test أو اختبار جهاز.
- لم يُختبر TalkBack أو IME أو split-screen فعلياً.
- لم تُلتقط صور متصفح جديدة بعد توحيد اللون؛ فحوص هذا التحديث ساكنة ومحلية كما حُددت ميزانية التحقق.
- لقطة API ليست تأكيداً على `master`، ولا تثبت مفتاح Data Search أو أسماء صلاحيات الملفات الدقيقة أو MIME/limits أو استعادة upload URL منتهٍ.
- التحقق الحالي من تنفيذ `ui-designer` وليس مراجعة `design-qa` مستقلة؛ تبقى المطابقة الفعلية بعد التنفيذ لمرحلة لاحقة.
- لم تُعدّل ملفات التطبيق أو الباك إند.
