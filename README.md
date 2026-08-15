# Sist

## Kişisel Finans ve Yatırım Takibi

Gelirlerini, giderlerini, hesaplarını, bütçelerini, borçlarını ve yatırımlarını tek bir yerde yönet.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpack-compose)](https://developer.android.com/jetpack/compose)
[![Latest Release](https://img.shields.io/github/v/release/batuhd/sist?label=release)](https://github.com/batuhd/sist/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)

Sist, Türkçe kullanıcı deneyimi ve kişisel veri gizliliği öncelikleriyle geliştirilen açık kaynak bir Android finans takip uygulamasıdır. Uygulama, günlük para hareketlerini anlamayı kolaylaştırırken yatırım varlıklarını ve finansal hedefleri tek bir akışta görmeyi sağlar.

> Sist yatırım tavsiyesi vermez. Gösterilen fiyatlar ve hesaplamalar yalnızca kişisel takip ve bilgilendirme amaçlıdır.

---

## İçindekiler

- [Öne Çıkanlar](#öne-çıkanlar)
- [Ekran Görüntüleri](#ekran-görüntüleri)
- [Özellikler](#özellikler)
- [Gizlilik ve Güvenlik](#gizlilik-ve-güvenlik)
- [Veri Kaynakları](#veri-kaynakları)
- [İndir](#indir)
- [Kaynak Kodundan Derleme](#kaynak-kodundan-derleme)
- [Proje Yapısı](#proje-yapısı)
- [Mimari Yaklaşım](#mimari-yaklaşım)
- [Tasarım Sistemi](#tasarım-sistemi)
- [Sık Sorulan Sorular](#sık-sorulan-sorular)
- [Yol Haritası](#yol-haritası)
- [Yasal Uyarı](#yasal-uyarı)
- [Lisans](#lisans)

---

## Öne Çıkanlar

| | |
|---|---|
| **Yerel veri** | Finansal kayıtlar cihazdaki Room veritabanında tutulur. |
| **Kapsamlı takip** | Hesap, işlem, bütçe, borç, taksit ve yatırım kayıtları tek uygulamada. |
| **Piyasa fiyatları** | Hisse, ETF ve Türkiye yatırım fonları için güncel fiyat desteği. |
| **Widget desteği** | Ana ekrandan portföy, bütçe, net varlık ve hızlı işlem görünümü. |
| **Modern arayüz** | Jetpack Compose ve Material 3 tabanlı, açık ve koyu tema desteği. |
| **Çevrimdışı kullanım** | İnternet olmadan kayıt ekleme ve yerel finans takibi. |
| **Biyometrik koruma** | Parmak izi ve yüz tanıma ile uygulama erişimi. |

---

## Ekran Görüntüleri

<p align="center">
  <img src="screenshots/1.jpg" alt="Sist ana sayfası" width="200"/>
  <img src="screenshots/2.jpg" alt="Sist işlemler ekranı" width="200"/>
  <img src="screenshots/3.jpg" alt="Sist portföy ekranı" width="200"/>
  <img src="screenshots/4.jpg" alt="Sist bütçeler ekranı" width="200"/>
</p>

---

## Özellikler

### Hesap Yönetimi

- Nakit, banka, yatırım ve diğer hesap türleri
- Birden fazla hesapla ayrı ayrı bakiye takibi
- Hesaplar arası transfer kaydı
- Varsayılan nakit ve portföy hesapları

### Gelir ve Gider Takibi

- Gelir, gider ve transfer işlemleri
- İşlem tarihi ve saati
- Kategori ve ödeme yöntemi seçimi
- Not ve etiket desteği
- Aylara göre listeleme
- Tarihe veya tutara göre sıralama
- Liste ve grafik görünümü
- Kategori bazlı gider dağılımı

### Tekrarlayan İşlemler

- Maaş, kira, fatura ve abonelik gibi düzenli işlemler
- Günlük, haftalık, aylık ve yıllık tekrar seçenekleri
- Başlangıç ve bitiş tarihi
- Aktif veya pasif durum kontrolü
- Otomatik işlem ve bildirim altyapısı

### Bütçe Planlama

- Aylık genel bütçe
- Kategori bazlı bütçeler
- Harcama ilerlemesi
- Limit aşımı kontrolü
- Ana ekran bütçe widget'ı

### Borç ve Taksit Takibi

- Verilen ve alınan borçlar
- Borç yönü ve durum takibi
- Taksit planları
- Taksit tutarı, vade ve ödeme durumu
- Yaklaşan ödemeler için bildirim altyapısı

### Portföy Takibi

- Hisse senedi, ETF ve yatırım fonu kaydı
- Alım ve satım geçmişi
- Varlık bazında maliyet ve miktar takibi
- Güncel fiyat ve toplam değer
- Günlük kar ve zarar görünümü
- Portföy dağılımı
- Varlık detay ekranı

### Bildirimler ve Arka Plan İşlemleri

- Günlük piyasa kapanış özeti
- Fiyat uyarıları
- Bütçe kontrolü
- Tekrarlayan işlem bildirimleri
- WorkManager ile planlı arka plan görevleri
- Cihaz yeniden başlatıldığında gerekli görevlerin yeniden planlanması

### Ana Ekran Widget'ları

- Portföy widget'ı
- Bütçe widget'ı
- Net varlık widget'ı
- Hızlı gelir veya gider ekleme widget'ı

### Görsel Deneyim

- Açık ve koyu tema
- Sistem temasına uyum
- Orman yeşili ve altın vurgulu tasarım dili
- Tutarlar için hizalı ve okunabilir tipografi
- Boş durumlarda yönlendirici aksiyonlar
- Küçük ekranlar için uyarlanmış alt navigasyon

---

## Gizlilik ve Güvenlik

Sist kişisel finans uygulaması olduğu için veri saklama davranışı özellikle sınırlı tutulmuştur.

### Veriler nerede tutulur?

Hesaplar, işlemler, bütçeler, borçlar, taksitler ve portföy kayıtları cihazdaki Room veritabanında tutulur. Uygulama, kişisel finans kayıtlarını kendi sunucusuna göndermez.

Fiyat güncellemesi yapılırken yalnızca seçilen varlığın sembolü ilgili fiyat sağlayıcısına gönderilir. Bu istek, kişisel hesap veya işlem kayıtlarını içermez.

### Cloud Backup davranışı

Uygulamanın Android Auto Backup desteği kapatılmıştır:

```xml
android:allowBackup="false"
```

Bu nedenle uygulama silindiğinde yerel finans kayıtları otomatik olarak geri yüklenmez. Önemli kayıtlarınızı korumak için gelecekte planlanan dışa aktarma özelliği kullanılabilir hale geldiğinde manuel yedek almanız önerilir.

### Release logları

HTTP gövde loglaması yalnızca debug build'lerinde açıktır. Release build'lerinde ağ loglaması kapalıdır. Böylece fiyat sağlayıcılarından dönen yanıtların veya geçici erişim bilgilerinin cihaz loglarına yazılma riski azaltılır.

### İmza anahtarı

v2.0.1 sürümü yeni bir release imza anahtarıyla oluşturulmuştur. Bu nedenle v2.0.0 ve önceki sürümlerden v2.0.1 üzerine doğrudan güncelleme yapılamaz. Eski uygulamanın kaldırılıp yeni APK'nın kurulması gerekir.

Keystore ve `keystore.properties` dosyaları `.gitignore` kapsamındadır ve repoya eklenmemelidir.

---

## Veri Kaynakları

Sist, piyasa fiyatlarını aşağıdaki kaynaklardan alır:

| Varlık türü | Kaynak |
|---|---|
| Hisse senedi | Yahoo Finance |
| ETF | Yahoo Finance |
| Türkiye yatırım fonu | FVT, Fintables |

Bu servisler yalnızca fiyat ve fon bilgisi almak için kullanılır. Kaynakların kullanım şartları, erişilebilirliği, gecikmeleri ve veri doğruluğu ilgili sağlayıcıların sorumluluğundadır.

---

## İndir

Hazır APK dosyalarını [GitHub Releases](https://github.com/batuhd/sist/releases) sayfasından indirebilirsiniz.

| Dosya | Kullanım |
|---|---|
| `sist-vX.Y.Z.apk` | İmzalı, optimize edilmiş release sürümü. Günlük kullanım için önerilir. |
| `sist-vX.Y.Z-debug.apk` | Geliştirme ve hata ayıklama amaçlı sürüm. |

### APK kurulumu

1. Releases sayfasından `sist-vX.Y.Z.apk` dosyasını indirin.
2. Dosyayı Android cihazınıza aktarın veya cihazdan indirin.
3. APK dosyasını açın.
4. Android isterse bilinmeyen kaynaklardan kurulum iznini verin.
5. Kurulumu tamamlayıp uygulamayı açın.

---

## Kaynak Kodundan Derleme

### Gereksinimler

- Android Studio'nun güncel bir sürümü
- JDK 17
- Android SDK 36
- Android SDK Build Tools
- Android SDK Platform Tools

### Projeyi çalıştırma

```bash
cd sist
./gradlew assembleDebug
```

Windows PowerShell için:

```powershell
git clone https://github.com/batuhd/sist.git
Set-Location sist
.\gradlew assembleDebug
```

Debug APK şu konumda oluşur:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Release derleme

Release derlemek için kendi keystore dosyanızı kullanın. Keystore bilgilerini repoya eklemeyin.

Proje kökünde `keystore.properties` oluşturun:

```properties
storeFile=release-key.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=sist-release
keyPassword=YOUR_KEY_PASSWORD
```

Yeni keystore oluşturmak için:

```bash
keytool -genkeypair \
  -v \
  -keystore release-key.jks \
  -alias sist-release \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

Release APK üretmek için:

```bash
./gradlew assembleRelease
```

Release APK şu konumda oluşur:

```text
app/build/outputs/apk/release/app-release.apk
```

### Doğrulama

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

Release APK imzasını Android SDK Build Tools içindeki `apksigner` ile kontrol edebilirsiniz:

```bash
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
```

---

## Proje Yapısı

```text
app/src/main/java/com/sinop/sist/
├── data/
│   ├── local/          # Room veritabanı, DAO ve entity sınıfları
│   ├── remote/         # Retrofit servisleri, DTO ve fiyat sağlayıcıları
│   ├── repository/     # Repository implementasyonları
│   └── mapper/         # Data ve domain modeli dönüşümleri
├── domain/
│   ├── model/          # Uygulama domain modelleri
│   ├── repository/     # Repository arayüzleri
│   └── usecase/        # Uygulama iş kuralları
├── presentation/       # Compose ekranları, ViewModel ve widget'lar
├── ui/theme/           # Tema, renk, tipografi ve tasarım token'ları
├── util/               # Ortak yardımcı fonksiyonlar
└── worker/             # WorkManager görevleri
```

---

## Mimari Yaklaşım

Sist, MVVM ve manuel dependency injection yaklaşımını kullanır.

```text
Compose UI
    ↓
ViewModel
    ↓
Use Case
    ↓
Repository interface
    ↓
Repository implementation
    ↓
Room DAO veya Remote API
```

- **Presentation:** Compose ekranları ve kullanıcı etkileşimleri
- **ViewModel:** UI state ve ekran davranışları
- **Domain:** Uygulama iş kuralları ve repository sözleşmeleri
- **Data:** Room, Retrofit, DTO, mapper ve repository implementasyonları
- **Worker:** Planlı bildirimler, fiyat yenileme ve otomatik işlemler
- **AppContainer:** Manuel dependency injection ve uygulama bağımlılıklarının merkezi

Projede Hilt kullanılmaz. Dependency injection, `SistApplication.kt` içindeki `AppContainer` ile yönetilir.

---

## Tasarım Sistemi

v2.0.0 ile uygulama genelinde merkezi bir tasarım sistemi kullanılmaya başlanmıştır.

- `SistColors`: Semantik renkler, kar/zarar renkleri, uyarılar ve kategori vurguları
- `SistTypography`: Tutarlar için okunabilir ve hizalı tipografi
- `SistDimens`: Tutarlı boşluk değerleri
- `SistRadius`: Ortak köşe yarıçapları
- `SistMotion`: Animasyon süreleri ve easing değerleri
- `SistTopBar`, `SummaryCards`, `TransactionItem`, `EmptyState`: Paylaşılan Compose bileşenleri

Detaylı tasarım token dokümantasyonu: [`docs/design-tokens.md`](docs/design-tokens.md)

---

## Sık Sorulan Sorular

### Uygulama kişisel finans verilerimi sunucuya gönderiyor mu?

Hayır. Hesap, işlem, bütçe, borç, taksit ve portföy kayıtları cihazda tutulur. Fiyat güncellemesi için yalnızca varlık sembolü kullanılır.

### Uygulamayı silersem veriler geri gelir mi?

Cloud Backup kapalı olduğu için otomatik olarak geri gelmez. Uygulama silindiğinde yerel veriler de silinir.

### İnternet olmadan kullanabilir miyim?

Yerel kayıt, hesap ve işlem özellikleri internet olmadan çalışır. Piyasa fiyatı yenileme, fiyat uyarıları ve bazı bildirimler ağ bağlantısı gerektirir.

### Eski sürümden v2.0.1'e güncelleme yapabilir miyim?

v2.0.1 yeni bir imza anahtarı kullandığı için v2.0.0 ve önceki sürümlerden doğrudan güncelleme yapılamaz. Eski uygulamayı kaldırıp yeni sürümü kurmanız gerekir.

### Yatırım fiyatları garanti edilir mi?

Hayır. Fiyatlar üçüncü taraf veri kaynaklarından alınır ve gecikmeli, eksik veya hatalı olabilir. Uygulama yatırım tavsiyesi vermez.

### Release ve debug APK arasındaki fark nedir?

Release APK optimize edilmiş ve günlük kullanım için hazırlanmış sürümdür. Debug APK geliştirme ve hata ayıklama amacıyla kullanılır; loglama ve geliştirme araçları içerebilir.

---

## Yol Haritası

- [x] Gelir ve gider takibi
- [x] Çoklu hesap desteği
- [x] Hesaplar arası transfer
- [x] Portföy yönetimi
- [x] Hisse, ETF ve yatırım fonu takibi
- [x] Bütçe planlama
- [x] Borç ve taksit takibi
- [x] Tekrarlayan işlemler
- [x] Ana ekran widget'ları
- [x] Bildirimler
- [x] Fiyat uyarıları
- [x] Biyometrik doğrulama altyapısı
- [x] Açık ve koyu tema
- [x] Android cloud backup'ın kapatılması
- [ ] CSV ve Excel dışa aktarma
- [ ] Manuel veri içe aktarma
- [ ] Daha fazla widget boyutu
- [ ] Gelişmiş portföy analizleri
- [ ] Özelleştirilebilir kategori simgeleri

---

## Yasal Uyarı

Sist yalnızca genel bilgilendirme ve kişisel finans takibi amacıyla geliştirilmiştir. Uygulamada yer alan hisse senedi, yatırım fonu, ETF, döviz, kripto varlık, emtia, endeks ve diğer finansal verilere ilişkin bilgiler yatırım tavsiyesi, yatırım danışmanlığı, alım satım önerisi veya finansal danışmanlık hizmeti değildir.

Uygulamada sunulan fiyatlar, grafikler, istatistikler, bildirimler ve diğer içerikler yalnızca bilgi amaçlıdır. Bu içeriklerin doğruluğu, güncelliği, eksiksizliği veya belirli bir amaca uygunluğu garanti edilmez. Veri sağlayıcılarından kaynaklanan gecikme, kesinti, eksiklik veya hatalardan doğabilecek sonuçlardan uygulama geliştiricisi sorumlu tutulamaz.

Yatırım kararları kişisel risk profili, finansal durum ve hedefler değerlendirilerek verilmelidir. Gerektiğinde Sermaye Piyasası Kurulu tarafından yetkilendirilmiş yatırım kuruluşlarından veya uzman danışmanlardan profesyonel destek alınmalıdır.

Gerçek para ile yapılacak işlemlerde tüm sorumluluk kullanıcıya aittir. Sist ve geliştiricileri, uygulamanın kullanımından doğabilecek doğrudan veya dolaylı maddi, manevi, finansal veya veri kaybından sorumlu değildir.

---

## Lisans

Bu proje [MIT Lisansı](LICENSE) ile lisanslanmıştır.

---

<div align="center">
  <sub>Türkiye'de özenle geliştirildi.</sub>
</div>
