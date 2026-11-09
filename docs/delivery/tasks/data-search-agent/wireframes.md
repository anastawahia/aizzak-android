# مخططات Data Search Agent — وكيل RAG محادثي

الحالة: بنية UX مصححة لنقطة اعتماد التصميم
التاريخ: 2026-11-06
المرجع: `ux-journeys.md`، ومتطلبات `DS-REQ-01..15`

## 1. قواعد المخطط

- `SCR-DS-002` (المحادثة) هي الشاشة الرئيسة؛ كل الشاشات الأخرى اختيار سياق أو أدوات مساندة.
- لا توجد شاشة بحث top-k أو قائمة نتائج مستقلة. السؤال والجواب داخل سجل محادثة.
- لا يعني وجود ملف أنه قابل للاسترجاع؛ تظهر سلامة ملفه وفهرسه كحالتين مستقلتين.
- لا تثبيتات = كل المصادر المفهرسة في المساحة. وجود تثبيتات = المثبت فقط.
- الترتيب أدناه ترتيب قراءة وتركيز، وليس لوناً أو قياساً بصرياً.

## 2. خريطة التنقل

```text
Home
 └─ SCR-DS-001 Choose space
     └─ SCR-DS-002 RAG conversation
         ├─ SCR-DS-003 Conversations
         │   ├─ SCR-DS-004 Create / rename
         │   └─ SCR-DS-005 Delete conversation
         ├─ SCR-DS-006 Knowledge sources
         │   ├─ SCR-DS-007 Add source / preparation
         │   ├─ SCR-DS-008 Source details
         │   └─ SCR-DS-010 Delete source
         └─ SCR-DS-009 Retrieval scope
```

## SCR-DS-001 — اختيار مساحة موجودة

**REQ:** DS-REQ-01..03، 14، 15

```text
┌──────────────────────────────────────┐
│ ←  DATA SEARCH                      │
│    Choose a space                   │
├──────────────────────────────────────┤
│ Conversations and sources stay      │
│ inside one space.                   │
│                                      │
│ ┌ Space name ─────────────────────┐ │
│ │ 8 sources · 3 conversations   › │ │
│ └─────────────────────────────────┘ │
│ ┌ Another space ──────────────────┐ │
│ │ 2 sources · 1 conversation    › │ │
│ └─────────────────────────────────┘ │
│ [Load more]                          │
└──────────────────────────────────────┘
```

1. App bar: Back، eyebrow، عنوان.
2. شرح أن المساحة تعزل محادثاتها ومصادرها.
3. قائمة cursor لمساحات موجودة، صف كامل الهدف لكل مساحة.
4. البيانات العددية تظهر فقط إن أعادها العقد.
5. Load more عند وجود cursor.
6. no-space: عنوان، شرح مانع، Refresh، Back؛ لا Create space.
7. loading/error/offline/denied/session تحجب الأسماء عند فقد التفويض.

**Back:** الرئيسة. **اختيار:** أحدث محادثة، أو إنشاء أول محادثة مرة واحدة.

## SCR-DS-002 — محادثة RAG النشطة

**REQ:** DS-REQ-03..09، 12..15

```text
┌──────────────────────────────────────┐
│ ← Space name          [Sources] [☰] │
│   Conversation title                │
│ [All indexed sources ▾] [Model ▾]*  │
├──────────────────────────────────────┤
│             [Load older]             │
│                                      │
│ You                                  │
│ ┌─────────────────────────────────┐ │
│ │ What changed in the policy?     │ │
│ └─────────────────────────────────┘ │
│                                      │
│ Data Search                          │
│ ┌─────────────────────────────────┐ │
│ │ Generated Markdown answer…      │ │
│ │ ▍ streaming                     │ │
│ │ [Source reference]*             │ │
│ │ [Copy] [Delete]                 │ │
│ └─────────────────────────────────┘ │
│ [Thinking… / partial / failed]       │
├──────────────────────────────────────┤
│ [ Write a message…             ] [↑]│
│ Draft saved · sending state          │
└──────────────────────────────────────┘
* only when returned by a confirmed contract
```

1. App bar: Back، اسم المساحة، عنوان المحادثة، Conversations، Sources.
2. شريحة نطاق واضحة: كل المصادر المفهرسة أو عدد المثبتات.
3. منتقي النموذج مشروط بعقد مؤكد.
4. تنبيه loading/offline/denied/session/no-indexed-sources عند الحاجة.
5. سجل من الأقدم للأحدث، مع Load older غير مكرر.
6. رسالة المستخدم: المحتوى، وقت/حالة الإرسال، حذف مشروط.
7. رسالة المساعد: Markdown/code/links، streaming/complete/partial/error، Copy/Delete.
8. مراجع مصادر مشروطة ببيانات منظمة؛ لا score أو صفحة مخترعة.
9. حالة «يفكر» منفصلة عن البث.
10. مؤلف متعدد الأسطر فوق IME، مسودة محفوظة، زر إرسال 48dp.

**الحالات:** empty transcript، waiting، streaming، interrupted، ambiguous send، offline read-only، denied/session. لا يعلن TalkBack كل token؛ يعلن الرد مرة عند اكتماله.

**Back:** `SCR-DS-001` أو الرئيسة بعد معالجة المسودة.

## SCR-DS-003 — محادثات المساحة

**REQ:** DS-REQ-05، 14، 15

```text
┌──────────────────────────────────────┐
│ Conversations                  [×]  │
│ [＋ New conversation]                │
├──────────────────────────────────────┤
│ ● Current conversation       [⋮]    │
│ ○ Previous conversation      [⋮]    │
│ [Load more]                          │
└──────────────────────────────────────┘
```

1. عنوان وعدد مؤكد إن توفر.
2. إنشاء محادثة.
3. قائمة محادثات المساحة فقط؛ النشطة محددة بنص وأيقونة لا لون فقط.
4. كل صف يفتح المحادثة؛ `⋮` يعرض Rename/Delete.
5. loading/empty/error/offline/denied/session.

**Compact:** modal drawer. **Expanded:** start pane ثابت. **Back/Close:** المحادثة دون فقد المسودة.

## SCR-DS-004 — إنشاء أو إعادة تسمية محادثة

**REQ:** DS-REQ-04، 05، 13..15

```text
┌──────────────────────────────────────┐
│ New conversation                    │
│ Conversation name                   │
│ [                               ]    │
│ Model* [Use agent default       ▾]   │
│ [Cancel]                 [Create]    │
└──────────────────────────────────────┘
```

1. عنوان create أو rename.
2. حقل باسم دائم؛ الحد يظهر فقط إن أكده العقد.
3. نموذج مشروط للإنشاء؛ فشل تثبيته لا يحذف المحادثة.
4. Cancel أولاً في ترتيب الأمان، Create/Save مع progress ومنع التكرار.
5. validation/server conflict/error يحافظ على النص.

## SCR-DS-005 — تأكيد حذف محادثة

**REQ:** DS-REQ-05، 14، 15

```text
┌──────────────────────────────────────┐
│ Delete “Conversation title”?        │
│ Its message history is removed.     │
│ Space files are not affected.       │
│ This cannot be undone.              │
│ [Cancel]                  [Delete]   │
└──────────────────────────────────────┘
```

1. اسم المحادثة، أثر الحذف، استثناء الملفات، وعدم التراجع.
2. Cancel initial focus.
3. Delete danger action؛ الصف يبقى حتى تأكيد الخدمة.
4. failure يعيد الحالة دون نجاح مبكر.

## SCR-DS-006 — مصادر معرفة المساحة

**REQ:** DS-REQ-09..11، 14، 15

```text
┌──────────────────────────────────────┐
│ ← Sources · Space name       [＋ Add]│
│ Files support answers in this space.│
├──────────────────────────────────────┤
│ [Search files…] [All|Indexed|Needs] │
│ ☑ policy.pdf                        │
│   File: ready · Index: indexed       │
│   Included in current scope   [⋮]   │
│ ☐ notes.txt                         │
│   File: ready · Index: not started   │
│   [Index]                     [⋮]   │
│ 2 pinned · [Use all]                 │
└──────────────────────────────────────┘
```

1. App bar مع اسم المساحة وAdd source.
2. شرح أن المصادر مشتركة للمساحة، والتثبيت يخص المحادثة الحالية.
3. بحث محلي/مرشح لا يرسل سؤال RAG.
4. قائمة موحدة تربط file وdocument بالمعرف، لا بالاسم.
5. checkbox نطاق يظهر مع محادثة مفتوحة؛ غير المفهرس معطل.
6. File safety وSearch index مكتوبتان مستقلتين.
7. Index/Retry/Rename/Delete/Summary مشروطة بالعقد والصلاحية.
8. شريط نطاق حالي: عدد المثبتات وUse all.
9. pagination الجزئية، empty، partial-read، offline، denied، session.

**Back:** المحادثة. **Expanded:** end pane مع بقاء المحادثة في الوسط.

## SCR-DS-007 — إضافة مصدر ومتابعة التجهيز

**REQ:** DS-REQ-10، 11، 14، 15

```text
┌──────────────────────────────────────┐
│ ← Add knowledge source · Space      │
├──────────────────────────────────────┤
│ [Choose file] [Add text as file]*   │
│ Selected: policy.pdf                 │
│ ✓ Local validation                   │
│ ◉ Registering                        │
│ ○ Uploading bytes                    │
│ ○ Safety scan                        │
│ ○ Ready                              │
│ ○ Indexing                           │
│ ○ Searchable                         │
│ [Cancel]*               [Continue]   │
└──────────────────────────────────────┘
```

1. مساحة ظاهرة لا تتغير أثناء العملية.
2. Android system picker؛ لا صلاحية تخزين واسعة.
3. Add text يظهر فقط إن أكد backend `text/plain` والحدود.
4. التسجيل/الرفع/الفحص/الفهرسة مراحل منفصلة مع أثر كل مرحلة.
5. replace-name warning فقط إن أكد العقد سلوكه.
6. process death يستعيد المعرفات ويصالح GET؛ لا POST/PUT عمياء.
7. quarantined/expired URL/failed/offline/denied/session لا تتحول إلى نجاح عام.

## SCR-DS-008 — تفاصيل المصدر وحالته

**REQ:** DS-REQ-09..11، 14، 15

```text
┌──────────────────────────────────────┐
│ ← Source details · Space name       │
│ policy.pdf                          │
├──────────────────────────────────────┤
│ File safety: Ready                   │
│ Search index: Indexed                │
│ Retrieval effect: searchable        │
│ [Include in this conversation]      │
│ [Index / Retry]* [Rename]*           │
│ [Delete source]                      │
└──────────────────────────────────────┘
```

1. الاسم والنوع والحجم/التاريخ المؤكد فقط.
2. سلامة الملف وحالة الفهرسة مستقلتان.
3. أثر واضح في RAG: قابل للاسترجاع، غير جاهز، أو خارج النطاق الحالي.
4. Pin/unpin للمحادثة الحالية؛ التثبيت يضيّق ولا يمنح صلاحية.
5. الأفعال مشروطة بالعقد/الصلاحية؛ لا reindex لمصدر سليم بلا قرار.
6. offline/partial/denied/session تخفي أو تجمد الأفعال بأمان.

## SCR-DS-009 — نطاق استرجاع المحادثة

**REQ:** DS-REQ-08..10، 14، 15

```text
┌──────────────────────────────────────┐
│ Answer scope                  [×]   │
│ ○ All indexed sources               │
│   Default: no files are pinned.      │
│ ● Selected sources only             │
│   ☑ policy.pdf · Indexed            │
│   ☑ guide.docx · Indexed             │
│   ☐ notes.txt · Not indexed [locked]│
│ [Use all]                    [Done]   │
└──────────────────────────────────────┘
```

1. شرح دلالتي النطاق قبل القائمة.
2. no pins = كل المفهرس.
3. أول تثبيت ينتقل إلى selected only؛ إزالة آخر تثبيت تعود إلى all.
4. غير المفهرس ظاهر مع سبب التعطيل ولا يدخل النطاق.
5. فشل التثبيت يعيد checkbox إلى حالة الخادم مع error inline.
6. Done يغلق فقط؛ Use all يزيل التثبيتات ولا يحذف ملفات.

**Compact:** bottom sheet/full screen عند font scale كبير. **Expanded:** end pane أو sheet.

## SCR-DS-010 — تأكيد حذف مصدر

**REQ:** DS-REQ-09، 10، 14، 15

```text
┌──────────────────────────────────────┐
│ Delete “policy.pdf”?                │
│ It will stop contributing to every  │
│ conversation in this space and be   │
│ removed from pinned scopes.         │
│ This cannot be undone.              │
│ [Cancel]                  [Delete]   │
└──────────────────────────────────────┘
```

1. اسم المصدر وأثره على كل محادثات المساحة.
2. أثره على الفهرس/القطع يكتب فقط وفق عقد مؤكد.
3. Cancel initial focus؛ Delete danger action.
4. الصف يبقى حتى نجاح الخادم؛ failure لا يمسح التثبيت محلياً.

## 3. مصفوفة الشاشات والحالات

| الشاشة | loading | empty | error | offline | denied/session | RTL/large font | هدّام |
|---|---:|---:|---:|---:|---:|---:|---:|
| SCR-DS-001 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | — |
| SCR-DS-002 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | حذف رسالة مشروط |
| SCR-DS-003 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | إلى 005 |
| SCR-DS-004 | progress | — | ✓ | ✓ | ✓ | ✓ | — |
| SCR-DS-005 | — | — | ✓ | ✓ | ✓ | ✓ | حذف محادثة |
| SCR-DS-006 | ✓ | ✓ | ✓/partial | ✓ | ✓ | ✓ | إلى 010 |
| SCR-DS-007 | ✓/stages | — | ✓ | ✓ | ✓ | ✓ | إلغاء مشروط |
| SCR-DS-008 | ✓ | — | ✓/partial | ✓ | ✓ | ✓ | إلى 010 |
| SCR-DS-009 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | — |
| SCR-DS-010 | — | — | ✓ | ✓ | ✓ | ✓ | حذف مصدر |

## 4. تسليم UI/inventory

- يحافظ UI designer على `SCR-DS-001..010` ومعرفات عناصر مستقرة.
- يصف inventory `SCR-DS-002` بوصفها الشاشة الرئيسة، ويغطي waiting/streaming/complete/partial/error/ambiguous send.
- يفصل inventory بين اختيار المساحة، محادثة RAG، قائمة المحادثات، المصادر، ونطاق الاسترجاع.
- كل شاشة تعرض اسم المساحة حين يلزم، ولا تعرض بيانات مساحتين في آن واحد.
- لقطات إلزامية: EN/LTR light compact، AR/RTL dark compact، EN dark expanded، AR light expanded مع `fontScale >= 1.3`، وحالة بث/انقطاع وحالة نطاق مثبت.
- لا يثبت inventory أسماء endpoint أو MIME/limits أو حقول استشهاد أو صلاحيات غير مؤكدة على `master`.
