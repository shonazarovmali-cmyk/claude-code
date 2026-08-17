# HAN FOOD Ombor

**HAN FOOD** oziq-ovqat ombori uchun Android ilova: yuk kirim-chiqimini hisoblash,
mijozlarga yuk berish va qaytarishni kuzatish, fakturalarni yig'ish va
hisobotlarni ko'rish hamda telefon kamerasi orqali shtrix-kod/QR-kod
skanerlash.

Ilova **to'liq oflayn** ishlaydi — barcha ma'lumotlar qurilmaning o'zida
(Room/SQLite) saqlanadi, internet talab qilinmaydi.

## Asosiy imkoniyatlar

- **Mahsulotlar**: nomi, shtrix-kodi, **bitta mahsulotga o'ntagacha rasm**,
  o'lchov birligi, joriy/minimal qoldiq, tannarx va sotish narxi bilan
  boshqarish. Rasmlar Instagram'dagi ko'p-rasmli post kabi utkazib-utkazib
  ko'riladi (nuqta indikatorlar bilan), har birini alohida o'chirish mumkin.
  Mahsulot va mijozlarni **o'chirish** mumkin (fakturalar/hisobotlardagi
  tarixi buzilmasligi uchun ro'yxatdan yashiriladi).
- **Mijozlar**: nomi, telefoni, manzili bilan mijozlar bazasi. Joylashuvni
  uch xil usulda belgilash mumkin: **GPS joylashuvni olish** (qurilma
  joriy koordinatasini avtomatik oladi), **xaritadan qo'lda belgilash**
  (xaritani surib pinni kerakli nuqtaga qo'yish — OpenStreetMap asosida,
  Google Maps API kaliti/billing shart emas) yoki **manzildan avtomatik
  topish** (manzil maydoniga yozib qidiruv belgisini bosish — OpenStreetMap
  Nominatim orqali, bepul). Saqlangan nuqta "Xaritada ko'rish" orqali
  Google Maps ilovasida ochiladi.
- **Excel'dan import**: Mahsulotlar bo'limida `.xlsx` jadval tanlab, undagi
  qatorlarni (nomi, shtrix-kodi, o'lchov birligi, miqdori, minimal qoldiq,
  tannarx, sotish narxi) avtomatik o'qib omborga qo'shadi — mavjud
  mahsulotlar shtrix-kod/nomi bo'yicha topilib miqdori qo'shiladi, yangilari
  yaratiladi, hammasi bitta kirim fakturasi sifatida yoziladi (summasi
  avtomatik hisoblanadi).
- **Import/optom savdo maydonlari** (ixtiyoriy): mahsulot qo'shishda artikul
  raqami (Art-Nr), bojxona H.S. kodi, quti/karobkadagi dona soni, evro
  narxi va erkin holat matni ham kiritish mumkin. Evro narxidan "zł'ga
  o'tkazish" tugmasi bilan Sozlamalar → Narx standartlaridagi kursga ko'ra
  tannarxni avtomatik hisoblaydi; "Sotish narxini hisoblash" tugmasi esa
  standart ustama foizi bo'yicha sotish narxini taklif qiladi. Quti dona
  soni kiritilsa, quti narxi va jami narx (barcha qutilar, ustamali/
  ustamasiz) jonli hisoblab ko'rsatiladi.
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
  mahsulotlar ro'yxati. Bosh sahifada oxirgi 30 kunda **eng aktiv
  (ko'p sotilgan) mahsulotlar** reklama-banner sifatida ko'rinadi.
- **Fakturalarni o'chirish**: noto'g'ri kiritilgan kirim/chiqim/qaytarish
  fakturasini butunlay o'chirish mumkin — mahsulot qoldig'iga qilgan
  ta'siri avtomatik bekor qilinadi (kirim/qaytarish ayiriladi, chiqim
  qaytariladi).
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
  mumkin — tanlash zahoti butun interfeysga qo'llanadi (ilovani qayta
  o'rnatmasdan/ishga tushirmasdan).

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
  joylashgan (`ui/components/TopTabMenu.kt`, `ScrollableTabRow` asosida —
  har bir tab o'z matniga mos kenglikda, uzun so'zlar ikki qatorga
  bo'linmaydi) — tanlangan bo'lim rangi to'qlashadi (qalin, asosiy yashil
  rangda), boshqalari xira ko'rinadi.

## Texnologiyalar

- Kotlin + Jetpack Compose (Material 3), `HorizontalPager` — mahsulot
  rasmlari galereyasi
- Room (SQLite) — lokal ma'lumotlar bazasi, `Migration(1,2)` → `Migration(4,5)`
  bilan (mahsulot rasmlari — endi ro'yxat, faktura nomi/biriktirmalar,
  mijoz GPS koordinatalari, artikul/H.S. kod/quti dona soni/evro narxi/
  holat ustunlari qo'shildi)
- CameraX + ML Kit Barcode Scanning
- Coil — mahsulot rasmlari va faktura biriktirmalarini ko'rsatish
- Google Play Services — `FusedLocationProviderClient` (mijoz GPS
  joylashuvini olish uchun; alohida Maps API kaliti/billing shart emas —
  saqlangan nuqta oddiy `geo:` intent orqali Google Maps ilovasida ochiladi)
- osmdroid (OpenStreetMap) — xaritadan qo'lda joylashuv belgilash ekrani;
  Nominatim — manzildan avtomatik geokodlash. Ikkalasi ham bepul, API
  kaliti/billing shart emas
- Qo'lda yozilgan minimal `.xlsx` o'quvchi (`util/ExcelImporter.kt`,
  `android.util.Xml` orqali) — Apache POI Android'da barqaror ishlamaydi,
  fastexcel-reader esa Android'da mavjud bo'lmagan `javax.xml.stream`
  (StAX) ga tayanadi va R8 bilan mos kelmadi, shu sabab kutubxonasiz
  yechim tanlandi
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
  ishga tushirish shart emas. **Muhim**: bu mexanizm faqat `AppCompatActivity`
  turidagi Activity'larni kuzatib avtomatik qayta yaratadi (API < 33'da) —
  shu sababdan `MainActivity` aynan `AppCompatActivity`dan meros oladi
  (oddiy `FragmentActivity` bo'lganda til tanlansa ham UI yangilanmas edi).
- Pul birligi — **złoty (zł)**. Barcha summalar ilova ichida shu valyutada
  ko'rsatiladi (raqamlar guruhlash formati polyakcha standartga mos: `1 250 000 zł`),
  UI tilidan qat'i nazar — xuddi boshqa ilovalarda "$" yoki "€" belgisi
  tarjima qilinmagani kabi, "zł" ham doim shunday qoladi.

## Excel'dan import qilish formati

Mahsulotlar ekranidagi yuklash tugmasi orqali tanlangan `.xlsx` faylning
**birinchi varag'i** o'qiladi, **birinchi qator sarlavha** deb hisoblanib
o'tkazib yuboriladi. Ustunlar tartibi (A dan G gacha):

| Ustun | Maydon | Majburiymi | Standart qiymat |
|---|---|---|---|
| A | Nomi | Ha | — |
| B | Shtrix-kod | Yo'q | (bo'sh) |
| C | O'lchov birligi | Yo'q | `dona` |
| D | Miqdor (kirim) | Yo'q | `0` |
| E | Minimal qoldiq | Yo'q | `0` |
| F | Tannarx | Yo'q | `0` |
| G | Sotish narxi | Yo'q | `0` |

Har bir qator uchun avval shtrix-kod (bo'lsa), keyin nomi bo'yicha mavjud
mahsulot izlanadi — topilsa unga miqdor qo'shiladi, topilmasa yangi
mahsulot yaratiladi. Barcha qatorlar **bitta kirim (STOCK_IN) fakturasi**
sifatida yoziladi, shuning uchun umumiy summa avtomatik hisoblanadi va
Fakturalar/Hisobotlar bo'limlarida ko'rinadi.

## Ma'lum cheklovlar / keyingi qadamlar

- Ilova bitta qurilmada oflayn ishlaydi — bir nechta xodim/qurilma
  o'rtasida real-vaqtli sinxronizatsiya yo'q (kerak bo'lsa, Firebase yoki
  boshqa bulutli backend qo'shish mumkin).
- Release build hozircha o'z release kaliti bilan emas, repo ichidagi
  doimiy `app/debug.keystore` bilan imzolanadi (Google Play'ga chiqarish
  uchun o'z release kalitingizni sozlang). **Ataylab qurilma-mustaqil**:
  standart Android debug kaliti odatda har bir qurilgan mashinaning o'z
  `~/.android/debug.keystore`'iga bog'liq bo'lib, alohida sessiyalarda
  qurilgan APK'lar boshqa-boshqa imzo bilan chiqib ketishi mumkin edi —
  natijada foydalanuvchi eski versiyani telefonidan avval **qo'lda
  o'chirmasdan** yangisini o'rnata olmasdi ("App not installed" xatosi).
  Endi barcha build'lar committed keystore orqali bir xil imzo bilan
  chiqadi, shu bilan yangilanishlar to'g'ridan-to'g'ri ustidan o'rnatiladi.

  > ⚠️ Agar telefoningizda shu ilovaning **v1.5.0 yoki undan eski** versiyasi
  > o'rnatilgan bo'lsa, u hali eski (mashinaga bog'liq) kalit bilan
  > imzolangan — shuning uchun **bir martalik** o'chirib-qayta o'rnatish
  > kerak bo'ladi. v1.5.1'dan boshlab bu muammo umuman qaytmaydi.
- Ilova ikonkasi vektor asosida yaratilgan (minSdk 26, adaptive icon) —
  xohlasangiz haqiqiy brend logotipi bilan almashtiring
  (`app/src/main/res/drawable/ic_launcher_*.xml`).
