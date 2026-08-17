# HAN FOOD Ombor

**HAN FOOD** oziq-ovqat ombori uchun Android ilova: yuk kirim-chiqimini hisoblash,
mijozlarga yuk berish va qaytarishni kuzatish, fakturalarni yig'ish va
hisobotlarni ko'rish, telefon kamerasi orqali shtrix-kod/QR-kod skanerlash
hamda ombor holati bo'yicha savolларга javob beradigan lokal AI yordamchisi.

Ilova **to'liq oflayn** ishlaydi — barcha ma'lumotlar qurilmaning o'zida
(Room/SQLite) saqlanadi, internet talab qilinmaydi.

## Asosiy imkoniyatlar

- **Mahsulotlar**: nomi, shtrix-kodi, o'lchov birligi, joriy/minimal qoldiq,
  tannarx va sotish narxi bilan boshqarish.
- **Mijozlar**: nomi, telefoni, manzili bilan mijozlar bazasi.
- **Kirim** — ta'minotchidan yuk qabul qilish (qoldiqqa qo'shiladi).
- **Chiqim (yuk berish)** — mijozga yuk berish (qoldiqdan ayiriladi, yetarli
  bo'lmasa xatolik ko'rsatiladi).
- **Qaytarish** — mijozdan yukning qaytishi (qoldiqqa qayta qo'shiladi).
- **Fakturalar**: har bir kirim/chiqim/qaytarish avtomatik faktura raqami
  bilan (masalan `CHQ-20260812-0001`) qayd etiladi, PDF sifatida eksport va
  ulashish mumkin.
- **Hisobotlar**: davr bo'yicha (bugun/hafta/oy/barcha vaqt) kirim-chiqim
  summasi, eng ko'p berilgan mahsulotlar, eng faol mijozlar, kam qolgan
  mahsulotlar ro'yxati.
- **Shtrix-kod/QR skaneri**: CameraX + ML Kit orqali telefon kamerasi bilan
  jonli skanerlash — mahsulot qo'shishda ham, faktura tuzishda ham
  ishlatiladi.
- **AI Yordamchi**: ombor ma'lumotlari asosida ishlaydigan, internetsiz
  ishlaydigan tahlil motori — "Qaysi mahsulotlar tugab qolyapti?", "Bu oy
  qancha yuk berildi?", "Eng faol mijozlar kim?" kabi savollarga real
  raqamlar bilan javob beradi.
- **Xavfsizlik**: ilova PIN-kod bilan himoyalangan, ixtiyoriy ravishda
  barmoq izi (biometrik) orqali ham kirish mumkin. PIN faqat tuzlangan xesh
  ko'rinishida, Android Keystore bilan shifrlangan xotirada saqlanadi.
- **Zaxira nusxa**: Sozlamalar bo'limidan bazani fayl sifatida eksport
  qilib, istalgan joyga (Telegram, Google Drive va h.k.) ulashish mumkin.
- **Ko'p tillilik**: Ilova 7 tilda ishlaydi — **inglizcha, o'zbekcha, ruscha,
  polyakcha, turkcha, ukraincha va nemischa**. Til qurilma tiliga qarab
  avtomatik tanlanadi; Sozlamalar → Til bo'limidan qo'lda ham o'zgartirish
  mumkin (ilovani qayta o'rnatmasdan, darhol qo'llanadi). AI Yordamchi ham
  tanlangan tilda javob beradi.

## Texnologiyalar

- Kotlin + Jetpack Compose (Material 3)
- Room (SQLite) — lokal ma'lumotlar bazasi
- CameraX + ML Kit Barcode Scanning
- Navigation Compose
- EncryptedSharedPreferences + BiometricPrompt — xavfsizlik
- `android.graphics.pdf` — tashqi kutubxonasiz PDF faktura yaratish
- Qo'lda DI (Hilt/Dagger ishlatilmagan — `HanFoodApp` ichida lazy singletonlar)

## Loyihani qurish (build)

Talablar: JDK 17+, Android SDK (compileSdk 34, build-tools 34.0.0).

```bash
cd android/hanfood-warehouse
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk (~43 MB, siqilmagan)

./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk (~12 MB, R8 + resurs siqish bilan)
# Hozircha debug kaliti bilan imzolangan — to'g'ridan-to'g'ri o'rnatsa bo'ladi,
# lekin Google Play'ga chiqarish uchun o'z release kalitingiz kerak bo'ladi.
```

Android Studio orqali ochish uchun: `File → Open` → shu papkani tanlang,
Gradle sinxronlanishini kuting, so'ng `Run`.

Release (imzolangan) APK/AAB yasash uchun o'z imzolash kalitingizni
`app/build.gradle.kts`dagi `signingConfigs`ga qo'shing.

## Arxitektura qisqacha

```
app/src/main/java/com/hanfood/warehouse/
├── data/
│   ├── local/          # Room: entity, dao, AppDatabase, Converters
│   └── repository/     # WarehouseRepository — yagona yozish/o'qish darvozasi
├── ai/                 # AiEngine interfeysi + LocalAiAnalysisEngine
├── security/            # PinManager (EncryptedSharedPreferences), BiometricHelper
├── ui/
│   ├── theme/           # Rang, tipografiya, Material3 tema
│   ├── navigation/      # Routes, ScannerBus, HanFoodNavGraph
│   ├── components/      # Umumiy composable'lar
│   └── screens/         # dashboard, products, clients, stock (kirim/chiqim/
│                         # qaytarish), scanner, invoices, reports, assistant,
│                         # settings, auth (PIN)
├── util/                # Formatlash, ViewModel factory, PDF/DB eksport, LanguageManager
├── HanFoodApp.kt         # Application — repository/pinManager/aiEngine
└── MainActivity.kt       # Yagona Activity, Compose Navigation host
```

### Ombor harakati mantig'i

`WarehouseRepository.recordStockIn/recordStockOut/recordReturn` barcha
yozuvlarni bitta Room tranzaksiyasi ichida bajaradi: faktura sarlavhasi
(`StockTransaction`) va qatorlari (`TransactionItem`) yoziladi, so'ng har bir
mahsulotning qoldig'i (`Product.quantity`) yangilanadi. Chiqim (`STOCK_OUT`)
uchun avval barcha qatorlarning qoldig'i yetarli ekani tekshiriladi — aks
holda `InsufficientStockException` otiladi va hech narsa yozilmaydi.

### AI yordamchi haqida muhim eslatma

Joriy AI yordamchi **haqiqiy til modeli (LLM) emas** — u ombordagi
ma'lumotlarni to'g'ridan-to'g'ri SQL so'rovlari orqali hisoblab, tayyor
qoida asosida javob shakllantiradigan lokal tahlil motori
(`LocalAiAnalysisEngine`). Bu tanlov ataylab qilindi: internetsiz ishlaydi,
tezkor, va hech qachon noto'g'ri raqam "o'ylab topmaydi" (gallyutsinatsiya
yo'q).

Agar kelajakda haqiqiy LLM (masalan Claude API) ulashni xohlasangiz:

1. `ai/AiEngine` interfeysini amalga oshiruvchi yangi klass yozing
   (masalan `ClaudeAiEngine`, ombor ma'lumotlarini kontekst sifatida
   API'ga yuborib, javobni qaytaradi).
2. `HanFoodApp.aiEngine` propertysida shu yangi klassni qaytaring.
3. Ekran va ViewModel kodini o'zgartirish shart emas.

Buning uchun internet ruxsati (`INTERNET` permission — hozir ML Kit
kutubxonasi tomonidan avtomatik qo'shilgan, lekin ishlatilmaydi) va API
kalitini xavfsiz saqlash kerak bo'ladi.

## Ko'p tillilik arxitekturasi

- Barcha matnlar `res/values*/strings.xml` fayllarida (`values` — inglizcha
  standart, `values-uz`, `values-ru`, `values-pl`, `values-tr`, `values-uk`,
  `values-de`). Android qurilma tiliga eng mos keladigan faylni avtomatik
  tanlaydi.
- Til tanlash `util/LanguageManager.kt` orqali (`AppCompatDelegate.setApplicationLocales`)
  — Sozlamalar ekranidagi almashtirish darhol qo'llanadi, ilovani qayta
  ishga tushirish shart emas.
- `ai/LocalAiAnalysisEngine.kt` — AI yordamchi javoblari ham `Context.getString(...)`
  orqali joriy tilda shakllantiriladi. Erkin matn kiritilganda mavzu
  (kam qoldiq, kirim/chiqim va h.k.) barcha 7 tildagi kalit so'zlar bo'yicha
  aniqlanadi — foydalanuvchi qaysi tilda yozishidan qat'i nazar tushuniladi.
- Pul birligi ("so'm") ataylab tarjima qilinmagan — bu O'zbekiston so'mining
  o'zi, xuddi boshqa ilovalarda "$" yoki "€" belgisi tarjima qilinmagani
  kabi.

## Ma'lum cheklovlar / keyingi qadamlar

- Ilova bitta qurilmada oflayn ishlaydi — bir nechta xodim/qurilma
  o'rtasida real-vaqtli sinxronizatsiya yo'q (kerak bo'lsa, Firebase yoki
  boshqa bulutli backend qo'shish mumkin).
- Release build hozircha imzolanmagan (debug signing bilan qurilgan) —
  Google Play'ga chiqarish uchun o'z release kalitingizni sozlang.
- Ilova ikonkasi vektor asosida yaratilgan (minSdk 26, adaptive icon) —
  xohlasangiz haqiqiy brend logotipi bilan almashtiring
  (`app/src/main/res/drawable/ic_launcher_*.xml`).
