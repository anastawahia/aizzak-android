# تحقق تصميم Data Search Agent — مواءمة API

الحالة: `passed` للتحقق الوثائقي والبصري قبل نقطة اعتماد التصميم  
التاريخ: 2026-11-06  
النطاق: `DS-REQ-01..13` و`SCR-DS-001..010`

## سلطة العقد

قُرئت حزمة `api-documents` قبل هذا التحديث. مصدرها المعلن:

- الفرع: `chore/public-file-links-closing`
- الالتزام: `ff547ad8228741a532077b8eaf8fa2eb928be782`
- تاريخ التوليد: 2026-10-22

ليست الحزمة دليلاً على `master` العامل. لذلك يعكس التصميم المسارات والحقول الموثقة، ويعرض في أدوات المراجعة تحذيراً بأن التنفيذ ينتظر تأكيد الباك إند. لم يُعدل الباك إند.

## الملفات والبصمات

| الملف | SHA-256 |
|---|---|
| `ux-journeys.md` | `2D2CE448C9851A3CBF1E89570B7030373D019DE1B9321B1732DAFF4429C34D41` |
| `wireframes.md` | `E9DEE56A346E5C4129562FDF1DDDF1FF4BB81B1BC8F823CFA33F5DDE1B098509` |
| `design-reference.html` | `FA9A0980E1FDF1270C035055374D7B9960FD70B01470406BA15B3DC17DE54067` |
| `design-tokens.md` | `41073B0EA869C18CB407021911BF83D99CA64F4C55548EE7E747F75C29721CD5` |
| `screen-inventory.json` | `D13B637E885437ECB69BD7F2DE3B1BCF4E12FBA4816EC2E21412655A708A8816` |

## فحص الجرد

الأمر:

```powershell
python "C:\Users\anast\.codex\plugins\cache\android-software-house-marketplace\android-software-house\2.0.0\scripts\suite.py" inventory --file "C:\Users\anast\Desktop\aizzak-android\.android-suite\tasks\data-search-agent\screen-inventory.json"
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

## تشغيل فعلي في Microsoft Edge

شُغّل `msedge.exe --headless=new --dump-dom` بملف مستخدم مؤقت منفصل. فُحص DOM بعد تنفيذ JavaScript لا المصدر الساكن فقط:

| الشاشة/التكوين | Exit | أحرف `#product-screen` | النتيجة |
|---|---:|---:|---|
| SCR-DS-001 · en/light/compact | 0 | 3055 | passed |
| SCR-DS-009 · ar/dark/compact | 0 | 998 | passed، RTL |
| SCR-DS-003 · en/dark/expanded | 0 | 2696 | passed |
| SCR-DS-004 · ar/light/compact/1.3 · quarantined | 0 | 1577 | passed، RTL |
| SCR-DS-006 · ar/light/expanded/1.3 | 0 | 1477 | passed، RTL |
| SCR-DS-010 · en/dark/compact | 0 | 1040 | passed |

كان stderr فارغاً في الحالات الست.

## اللقطات المرئية

| الملف | التكوين | SHA-256 |
|---|---|---|
| `design-preview.png` | Dashboard · en/light/compact | `0DEFA6818E693479771FFCE64592FADC41B6A3E866F291C7FF0235D1E0FAE1B1` |
| `design-preview-space-ar.png` | Choose space · ar/dark/compact | `21B0CAFCB8179B94D743ACC3CD27ABF6FAC6BDB2D0855FDD0048999520529D61` |
| `design-preview-library-wide.png` | Library · en/dark/expanded | `4DD84F5D34A1423E3593EF12BA1C7C05F2BCB880FFD32C6C71298AE734A7B960` |
| `design-preview-results-ar-wide.png` | Results · ar/light/expanded/1.3 | `53D877BA072AD083BF504444AC7DAD11D4D509451EEE33BFC0DF13900E4715CA` |
| `design-preview-replace-dark.png` | Replace warning · en/dark/compact | `7F27A32309388BC6FF524A9AD0D8B134D9D6B089AA64869D8057C921D5B01C34` |

فُحصت اللقطات بصرياً. تظهر عناوين ووصف بطاقات الإضافة في أسطر منفصلة، وسلامة الملف وفهرسة البحث كسطرين مستقلين، ولا يوجد قص ظاهر في العربية 1.3 أو الحوارات.

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
- لقطة API ليست تأكيداً على `master`، ولا تثبت مفتاح Data Search أو أسماء صلاحيات الملفات الدقيقة أو MIME/limits أو استعادة upload URL منتهٍ.
- تعذر إكمال دور `ui-designer` المفوض بسبب حد استخدام الأداة؛ أُنجز تحديث المرجع والجرد في الجلسة الرئيسية وفق fallback المسموح. لا يؤثر ذلك على نقطة اعتماد المالك، لكنه لا يعد مراجعة مستقلة.
- لم تُعدّل ملفات التطبيق أو الباك إند.
