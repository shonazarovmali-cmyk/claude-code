# HAN FOOD Ombor

**HAN FOOD** oziq-ovqat ombori uchun Android ilova: yuk kirim-chiqimini hisoblash,
mijozlarga yuk berish va qaytarishni kuzatish, fakturalarni yig'ish va
hisobotlarni ko'rish hamda telefon kamerasi orqali shtrix-kod/QR-kod
skanerlash.

Ilova **to'liq oflayn** ishlaydi — barcha ma'lumotlar qurilmaning o'zida
(Room/SQLite) saqlanadi, internet talab qilinmaydi.

## Asosiy imkoniyatlar

- **Mahsulotlar**: nomi, shtrix-kodi, **rasmi**, o'lchov birligi, joriy/minimal
  qoldiq, tannarx va sotish narxi bilan boshqarish. Mahsulot qo'shishda
  telefon galereyasidan rasm biriktirish mumkin, rasm ro'yxatda va tahrirlash
  ekranida ko'rinadi.
- **Mijozlar**: nomi, telefoni, manzili bilan mijozlar bazasi. Mijoz
  qo'shishda **GPS joylashuvini olish** tugmasi bilan koordinatalarni
  saqlash mumkin — keyin "Xaritada ko'rish" orqali Google Maps ilovasida
  ochiladi (alohida Maps API kaliti/billing shart emas).
- **Kirim** — ta'minotchidan yuk qabul qilish (qoldiqqa qo'shiladi).
- **Chiqim (yuk berish)** — mijozga yuk berish (qoldiqdan ayiriladi, yetarli
  bo'lmasa xatolik ko'rsatiladi).
- **Qaytarish** — mijozdan yukning qaytishi (qoldiqqa qayta qo'shiladi).
- **Fakturalar**: har bir kirim/chiqim/qaytarish avtomatik faktura raqami
  bilan (masalan `CHQ-20260812-0001`) qayd etiladi, PDF sifatida eksport va
  ulashish mumkin. Har bir faktura uchun ixtiyoriy **nom** kiritish va
  **fayl/rasm biriktirish** (masalan tovar-transport hujjati skani yoki yuk
  fotosi) mumkin — biriktirilgan fayllar faktura tafsilotlarida ko'rinadi va
  bosilganda ochiladi.
- **Hisobotlar**: davr bo'yicha (bugun/hafta/oy/barcha vaqt) kirim-chiqim
  summasi, eng ko'p berilgan mahsulotlar, eng faol mijozlar, kam qolgan
  mahsulotlar ro'yxati.
- **Shtrix-kod/QR skaneri**: CameraX + ML Kit orqali telefon kamerasi bilan
  jonli skanerlash — mahsulot qo'shishda ham, faktura tuzishda ham
  ishlatiladi.
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

## Dizayn / brend

- Ilova logotipi (`HAN FOOD` yozuvi + teal/oltin rangli geometrik gul nishoni)
  mijoz yuborgan animatsiyadan (`HAN_FOOD_animacja.mp4`) olingan. To'liq
  logotip ilova ochilganda **splash ekran**ida va Sozlamalar → Ilova haqida
  bo'limida ko'rsatiladi (`res/drawable-nodpi/han_food_logo.png`).
- Ilova ikonkasi logotipdagi gul nishonining vektor qayta chizilgan versiyasi
  (manba video past piksel zichlikda bo'lgani uchun to'g'ridan-to'g'ri
  ishlatib bo'lmadi — barcha o'lchamlarda aniq ko'rinishi uchun vektor
  sifatida qayta yaratildi: `res/drawable/ic_launcher_foreground.xml`).
- Rang sxemasi — **yashil va oq** (mijoz talabiga ko'ra): asosiy rang
  logotipdagi to'q yashil siyoh rangidan olingan (`BrandGreen #1E6F4C`),
  fon/sirtlar oq, teal/oltin/lojuvard faqat kichik brend urg'ulari sifatida
  (ikonka) qoldirilgan. Sozlamalar → Til kabi qorong'i rejim uchun ham mos
  palitra bor.
- **Sarlavha (header)**: haqiqiy `HAN FOOD` logotip-yozuvi (gul nishoni +
  "HAN FOOD" so'zi, tagline'siz — `res/drawable-nodpi/han_food_header_lockup.png`)
  toza oq fonda, yetarlicha katta o'lchamda ko'rsatiladi (MoySklad CRM
  uslubidagi biznes-ilova ko'rinishi uchun qorong'i gradient banner o'rniga).
- **Yuqori menyu**: navigatsiya pastki panel o'rniga ilovaning **tepasida**
  joylashgan (`ui/components/TopTabMenu.kt`) — tanlangan bo'lim rangi
  to'qlashadi (qalin, asosiy yashil rangda), boshqalari xira ko'rinadi.

## Texnologiyalar

- Kotlin + Jetpack Compose (Material 3)
- Room (SQLite) — lokal ma'lumotlar bazasi, `Migration(1,2)` va `Migration(2,3)`
  bilan (mahsulot rasmi, faktura nomi/biriktirmalar, mijoz GPS
  koordinatalari ustunlari qo'shildi)
- CameraX + ML Kit Barcode Scanning
- Coil — mahsulot rasmi va faktura biriktirmalarini ko'rsatish
- Google Play Services — `FusedLocationProviderClient` (mijoz GPS
  joylashuvini olish uchun; alohida Maps API kaliti/billing shart emas —
  saqlangan nuqta oddiy `geo:` intent orqali Google Maps ilovasida ochiladi)
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
├── security/            # PinManager (EncryptedSharedPreferences), BiometricHelper
├── ui/
│   ├── theme/           # Rang, tipografiya, Material3 tema
│   ├── navigation/      # Routes, ScannerBus, HanFoodNavGraph
│   ├── components/      # Umumiy composable'lar
│   └── screens/         # dashboard, products, clients, stock (kirim/chiqim/
│                         # qaytarish), scanner, invoices, reports,
│                         # settings, auth (PIN)
├── util/                # Formatlash, ViewModel factory, PDF/DB eksport, LanguageManager
├── HanFoodApp.kt         # Application — repository/pinManager
└── MainActivity.kt       # Yagona Activity, Compose Navigation host
```

### Ombor harakati mantig'i

`WarehouseRepository.recordStockIn/recordStockOut/recordReturn` barcha
yozuvlarni bitta Room tranzaksiyasi ichida bajaradi: faktura sarlavhasi
(`StockTransaction`) va qatorlari (`TransactionItem`) yoziladi, so'ng har bir
mahsulotning qoldig'i (`Product.quantity`) yangilanadi. Chiqim (`STOCK_OUT`)
uchun avval barcha qatorlarning qoldig'i yetarli ekani tekshiriladi — aks
holda `InsufficientStockException` otiladi va hech narsa yozilmaydi.

## Ko'p tillilik arxitekturasi

- Barcha matnlar `res/values*/strings.xml` fayllarida (`values` — inglizcha
  standart, `values-uz`, `values-ru`, `values-pl`, `values-tr`, `values-uk`,
  `values-de`). Android qurilma tiliga eng mos keladigan faylni avtomatik
  tanlaydi.
- Til tanlash `util/LanguageManager.kt` orqali (`AppCompatDelegate.setApplicationLocales`)
  — Sozlamalar ekranidagi almashtirish darhol qo'llanadi, ilovani qayta
  ishga tushirish shart emas.
- Pul birligi — **złoty (zł)**. Barcha summalar ilova ichida shu valyutada
  ko'rsatiladi (raqamlar guruhlash formati polyakcha standartga mos: `1 250 000 zł`),
  UI tilidan qat'i nazar — xuddi boshqa ilovalarda "$" yoki "€" belgisi
  tarjima qilinmagani kabi, "zł" ham doim shunday qoladi.

## Ma'lum cheklovlar / keyingi qadamlar

- Ilova bitta qurilmada oflayn ishlaydi — bir nechta xodim/qurilma
  o'rtasida real-vaqtli sinxronizatsiya yo'q (kerak bo'lsa, Firebase yoki
  boshqa bulutli backend qo'shish mumkin).
- Release build hozircha imzolanmagan (debug signing bilan qurilgan) —
  Google Play'ga chiqarish uchun o'z release kalitingizni sozlang.
- Ilova ikonkasi vektor asosida yaratilgan (minSdk 26, adaptive icon) —
  xohlasangiz haqiqiy brend logotipi bilan almashtiring
  (`app/src/main/res/drawable/ic_launcher_*.xml`).
