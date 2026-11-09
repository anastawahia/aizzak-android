# تحقق تصميم Data Search — وكيل RAG محادثي

الحالة: `passed` للفحص الوثائقي والساكن قبل نقطة اعتماد التصميم
التاريخ: 2026-11-06  
النطاق: `DS-REQ-01..15` و`SCR-DS-001..010`

## التصحيح المفحوص

- أصبحت المحادثة وسجل الرسائل والمؤلف هي سطح العمل الرئيس.
- أزيلت رحلة البحث ونتائج `top-k` المستقلة من الواجهة الأساسية.
- أصبحت الملفات والفهرسة ونطاق الاسترجاع أدوات مساندة.
- وُثّق الافتراضي: لا تثبيتات = كل المصادر المفهرسة في المساحة؛ وجود تثبيتات = المجموعة المثبتة فقط.
- لا تظهر مراجع المصادر إلا إذا أعادها عقد الرسالة بصورة منظمة مؤكدة.
- لا توجد أفعال إنشاء/إعادة تسمية/حذف مساحة في Android.

## بصمات حزمة التصميم

| الملف | SHA-256 |
|---|---|
| `ux-journeys.md` | `04778D7C18358C452290BA14B14D9ADDB443E7AEAEFFCC5B0391E2B1797E8A26` |
| `wireframes.md` | `1AE73A1E04F6DEF53DD107ADDDCF4921EE674B1A2D6E61FD958B7D1681987199` |
| `design-reference.html` | `A980BABC438FBD667DBF621A7A0E246DE27B4178A4D071B90065336F4B28A884` |
| `design-tokens.md` | `2122771E81D0FDDB8B9B11516D82D34B1D0AFB958F26664A276D0C99D70667F2` |
| `screen-inventory.json` | `4B32D7A09513F43ECB11CBA817FC41C7AB3294E542D7B24BF2266C6F2602C1C8` |

## فحص الجرد

الأمر:

```powershell
python "C:\Users\anast\Desktop\aizzak-android\.codex\android-software-house\runtime\scripts\suite.py" inventory --file "C:\Users\anast\Desktop\aizzak-android\docs\delivery\tasks\data-search-agent\screen-inventory.json"
```

النتيجة: exit `0`، `status: passed`، و`errors: []`.

- الشاشات: 10 (`SCR-DS-001..010`).
- العناصر: 90، بمعرفات فريدة.
- اللغات: الإنجليزية والعربية.
- التكوينات: EN/light/390/1.0، AR/dark/390/1.0، EN/dark/840/1.0، AR/light/840/1.3.
- فحص ربط المعرفات بالمرجع: 10 شاشات، 90 عنصراً، 0 معرف شاشة مفقود.

## JavaScript والمرجع التفاعلي

قُرئ `<script>` من `design-reference.html` وشُغّل عبر `new Function` في Node دون كتابة ملف مؤقت:

```powershell
node -e "const fs=require('fs'); const h=fs.readFileSync('docs/delivery/tasks/data-search-agent/design-reference.html','utf8'); const s=h.match(/<script>([\s\S]*?)<\/script>/); if(!s) throw new Error('script missing'); new Function(s[1]); console.log('script syntax passed')"
```

النتيجة: exit `0` و`script syntax passed`.

يدعم fragment صيغة `SCR|locale|theme|layout|scale|state` ويغطي حالات content وstreaming وpartial وempty وoffline وpermission وsession.

## لقطات Edge الحالية

استُخدم:

```powershell
"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" --headless=new --disable-gpu --hide-scrollbars --no-first-run --window-size=<w>,<h> --screenshot=<output> "file:///.../design-reference.html#<config>"
```

نجحت الحالات الخمس، exit `0` لكل منها:

| الملف | التكوين | SHA-256 |
|---|---|---|
| `design-preview.png` | محادثة · EN/light/compact/content | `A47AF3B8A5196F4816B6BBBBED32275ACC2A3258C6576EA402DF56C938A017F8` |
| `design-preview-space-ar.png` | اختيار مساحة · AR/dark/compact/content | `87853501AA4AB61DE85245AA6DC09FB119AE3C006A23B46470C403C101799563` |
| `design-preview-library-wide.png` | مصادر · EN/dark/expanded/content | `70CB1ECA320BFFCE6F105C5A61BE1D5915CE3423FC94A4E14A42242601AFC11D` |
| `design-preview-results-ar-wide.png` | محادثة · AR/light/expanded/1.3/streaming | `040A2B1E218DE2CA27C95E4F25DF8B4330500C7E8A2C3B78888092051D888CA9` |
| `design-preview-replace-dark.png` | نطاق إجابة · EN/dark/compact/content | `43D6A1E8ED72C008F2F9A9D595F109FCD05E446E26577FAA1A2B6BE1C1D73F53` |

أسماء اللقطتين `results` و`replace` محفوظة لاستقرار المسارات التاريخية فقط؛ محتواهما الحالي هو محادثة تبث ونطاق إجابة، وليس نتائج بحث أو تحذير استبدال.

فُحصت بصرياً لقطة المحادثة compact الإنجليزية ولقطة المحادثة expanded العربية عند 1.3. ظهرت الرسائل والمؤلف وحالة البث والأجزاء الثلاثة بلا قص مرئي. هذا فحص مرجع HTML، وليس اختبار Compose أو جهاز.

## فحص diff

`git diff --check` نجح، exit `0`. ظهرت تحذيرات تحويل LF إلى CRLF فقط، بلا أخطاء whitespace.

## سلطة العقد والقيود

- تطبيق الويب مرجع سلوك منتج للقراءة فقط، وليس إثباتاً لعقد `master`.
- لم يُعدل تطبيق Android أو تطبيق الويب أو الباك إند.
- لا يثبت التصميم مفتاح Data Search أو الصلاحيات أو message content أو SSE events أو citations أو pagination أو model routes أو pins أو MIME/limits أو recovery.
- لم يُنفذ Android build أو Compose preview أو screenshot test أو TalkBack أو IME أو جهاز فعلي.
- التحقق الحالي فحص ذاتي لمخرجات UI designer؛ لا يحل محل `design-qa` المستقل بعد التنفيذ.
