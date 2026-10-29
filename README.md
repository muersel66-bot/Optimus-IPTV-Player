# ⚡OPTİMUS™⚡ İptv Player PRO (Android)

Oynatıcı sayfası (`app/src/main/assets/index.html`) bir WebView içinde çalışır.
Uygulamada M3U, Xtream ve Stalker girişleri doğrudan çalışır (CORS sınırı yoktur).

## APK'yı bilgisayarsız / Android Studio'suz derleme (GitHub)
1. github.com'da ücretsiz hesap açın, yeni bir **boş depo** oluşturun.
2. Bu klasörün içeriğini depoya yükleyin ("Add file > Upload files"). `.github` klasörü de yüklenmeli.
3. Depoda **Actions** sekmesini açın. "APK derle" çalışır (yaklaşık 3-5 dakika).
   Çalışmazsa "APK derle > Run workflow" düğmesine basın.
4. Bitince çalıştırmanın sayfasında **Artifacts** altından `OPTIMUS-IPTV-PRO-apk` dosyasını indirin, zip içindeki `app-release.apk` dosyasını telefona kurun.
   (Telefonda "bilinmeyen kaynaklardan kuruluma izin ver" açık olmalı.)

## Android Studio ile
Klasörü açın, Build > Build APK(s). Çıktı: `app/build/outputs/apk/release/app-release.apk`.
Gerekli: JDK 17, Android SDK 34.

## Ses: Dolby Digital, Dolby Digital Plus, DTS
Uygulama varsayılan olarak **VLC motoru** (libVLC) ile oynatır. AC-3, E-AC-3 (Dolby Digital Plus), DTS ve DTS-HD sesleri
cihazın donanım desteğine bakmadan yazılımla çözülür; RTMP, RTSP ve UDP akışları da açılır.
- Oynatma ekranında kodek görünür (örnek: "Ses: Dolby Digital Plus 5.1"). "Ses" düğmesi ses parçasını seçtirir.
- Kanal değiştirme: kumandada yukarı/aşağı, dokunmatikte "Önceki/Sonraki".
- Sayfanın üstündeki "Motor" düğmesi VLC ile tarayıcı oynatıcısı arasında geçiş yapar.
- Ses telefon/TV hoparlörüne stereo veya cihazın desteklediği kanal sayısıyla gider; HDMI "passthrough" yoktur.
- **MX Player Pro:** "Motor" düğmesini "MX Player"a getirirseniz yayınlar yüklü olan MX Player Pro'da (yoksa MX Player'da) açılır.
  Uygulama MX Player'ı içermez, ayrıca kurulması gerekir. Kanal değiştirmek için MX'ten çıkıp listeden başka kanal seçilir.
  Dolby/DTS desteği MX Player'ın kendi ayarlarına ve ses kodek paketine bağlıdır.
- VLC çözücüleri APK'yı birkaç on MB büyütür ve işlemci kullanır.

## Film ve dizi indirme (çevrimdışı izleme)
- Film ve dizi bölümlerinin yanındaki ⬇ düğmesi dosyayı Android'in indirme yöneticisiyle indirir; bildirim gösterir, kesilirse devam eder.
- Dizilerde sezon başlığındaki "Sezonu indir" tüm bölümleri sıraya alır (iki dokunuşla onay).
- İndirmeler **birer birer** yapılır (sağlayıcıların eşzamanlı bağlantı sınırı için). Kuyruk uygulama açıkken ilerler.
- **İndirilenler** bölümünde ilerleme, boş alan, "Yalnızca Wi-Fi'da indir", oynat, iptal ve sil vardır.
- İndirilen dosya, listeden açılınca otomatik olarak çevrimdışı (VLC ile) oynatılır.
- Dosyalar uygulamaya özel alana (Android/data/.../files/Movies) iner; uygulama silinirse dosyalar da silinir. Ek izin gerekmez.

## Robot efektleri
- Açılışta robotlu "SİSTEM BAŞLATILIYOR" animasyonu (oturum başına bir kez, dokununca geçilir) ve kısa robot sesleri.
- Neon parlayan logo, tarama çizgili ana ekran, parlayan düğmeler ve seçili kanal vurgusu.
- Başlıktaki **FX** düğmesi efektleri ve sesleri açar/kapatır. Cihazda "hareketi azalt" açıksa açılış animasyonu atlanır.

## 4K yayın
- Adında/kategorisinde 4K, UHD, 2160p, HEVC veya H.265 geçen kanal, film ve dizilerde sarı **4K** rozeti ve ayrı bir **🎞 4K** kategorisi çıkar.
- VLC motoru donanım HEVC/H.264 çözmeyi önce dener, olmazsa yazılıma geçer; önbellek 3 sn. Oynatıcı çubuğu çözünürlüğü ve kodeki gösterir (örn. "3840×2160 4K HEVC").
- Tarayıcı oynatıcısında HLS en yüksek kaliteye çıkar. HEVC'i çözemeyen cihazlarda uygulamada VLC önerilir.
- 4K akış için genellikle 25 Mbit/s veya üzeri bağlantı gerekir.

## Android TV ve telefon
- Aynı APK hem telefonda hem Android TV / TV box'ta çalışır. TV'de uygulama listesinde banner ile görünür (LEANBACK_LAUNCHER).
- TV otomatik algılanır: yazılar büyür, belirgin odak halkası çıkar. Kumanda yön tuşları ekrandaki düğme ve kanallar arasında gezinir, Tamam seçer, CH+/CH- (veya PageUp/PageDown) kanal değiştirir.
- Geri tuşu önce açık pencereyi, dizi sezonunu veya aramayı kapatır; başka adım yoksa uygulamadan çıkar.
- VLC oynatıcıda: Sol/Sağ ±10 sn (film/dizi), Tamam çubuğu açar, ▲▼ kanal değiştirir, Oynat/Duraklat ve ileri/geri sarma tuşları çalışır.
- Telefonda: dikey ekranda tek sütun, yatay ekranda iki sütun, dokunmatik hedefler en az 44 px.
- Not: TV'de dosya seçici (M3U dosyası yükle) bulunmayabilir; bağlantı, Xtream veya Stalker kullanın.

## Kategoriler
- Arama kutusunun altındaki **☰ Kategoriler** düğmesi, bölümdeki (Canlı/Film/Dizi) tüm kategorileri kanal sayılarıyla gösterir; arayıp seçebilirsiniz. Tümü, Favoriler, Geçmiş ve 4K en üstte durur.
- Kategorileri gizlemek/göstermek için pencerenin altındaki "Gizli kategorileri yönet" (Merkez → Kategoriler).

## M3U bağlantıları
- "M3U bağlantısı" modunda adresi yapıştırıp **Yükle** ile açın, **＋ Ekle** ile yalnızca kaydedin. Başarıyla yüklenen adresler de otomatik kaydedilir.
- Kayıtlı bağlantılar listelenir; dokununca liste yüklenir, ✕ siler. Kullanıcı adı/şifre ekranda gösterilmez (adres yalnızca bu cihazda saklanır).

## Android TV oynatıcı (VLC)
- Tam ekran oynatıcıda kumandayla kullanılan kontrol çubuğu: ◀◀ Kanal / ⏸ Duraklat / Kanal ▶▶ / ⏪10 / 10 ⏩, ikinci satırda Oran (Sığdır, Ekrana doldur, 16:9, 4:3, Orijinal), Ses, Altyazı, Kapat.
- Film/dizi için alt kısımda arama çubuğu ve süre; Sol/Sağ ile 10 sn (çubuk kapalıyken) ya da çubukta seçiliyken ince ayar. Canlı yayında sarma yoktur.
- Çubuk kapalıyken ▲▼ kanal değiştirir; Tamam çubuğu açar; çubuk açıkken yön tuşları düğmeler arasında gezer. Seçili düğme camgöbeği çerçeveyle belirir. Çubuk 5 sn sonra kapanır.
- Oynat/Duraklat, ileri/geri sarma ve CH+/CH- medya tuşları desteklenir.

## TV kumandası tuşları
| Tuş | Liste ekranı | VLC oynatıcı |
|---|---|---|
| ▲▼◀▶ + Tamam | gezin / seç | çubuk kapalıyken ▲▼ kanal, ◀▶ ±10 sn; açıkken düğmelerde gezin |
| Rakamlar (+Tamam) | kanal numarasına git (1,6 sn sonra otomatik) | aynı |
| Kırmızı | Kategoriler | Görüntü oranı |
| Yeşil | Arama | Ses parçası |
| Sarı | Favori aç/kapat | Altyazı |
| Mavi | Dil değiştir | Bilgi (kodek/çözünürlük) |
| Menü | Kontrol Merkezi | Bilgi çubuğu |
| Info | Kanal bilgisi | Bilgi çubuğu |
| Guide | Kategoriler | - |
| Son kanal | önceki kanala dön | aynı |
| CH+ / CH- | kanal değiştir | kanal değiştir |
| Oynat/Duraklat, ileri/geri sarma | çalışır | çalışır |
| Geri | pencereyi/aramayı kapat, yoksa çık | oynatıcıyı kapat |
Not: Kumandanızda renkli tuşlar veya bazı tuşlar yoksa o işlevler ekran düğmelerinden de yapılabilir. Tuş kodları üreticiye göre değişebilir.

## Ana ekran
- Açılışta robot başlıklı ana ekran gelir: büyük kutular (Canlı Yayın, Filmler, Diziler, Favoriler, Geçmiş, İndirilenler, 4K, Kategoriler, Liste ekle, Merkez), tarih/saat ve "Devam et" satırı (son izlenenler).
- Başlıktaki **Ana Ekran** düğmesi ve Android geri tuşu (liste ekranındayken) ana ekrana döner; ana ekranda geri tuşu uygulamadan çıkar.
- "Açılışta ana ekranı göster" ayarı ana ekranın altındadır.

### Görsel ana ekran (3.4)
- Büyük robot başlık (hareketli arka plan, HUD köşeleri, kanal/film/dizi sayısı) ve renkli gradyan kutular.
- Afiş satırları: Devam et, Favoriler, Filmler, Diziler, Canlı Kanallar (sağlayıcının afiş/logo resimleriyle; resim yoksa renkli harf kartı). 4K içerikte sarı rozet. Karta dokununca doğrudan açılır, "Tümünü gör" ilgili bölüme gider.

## Uygulama içi güncelleme
Merkez → Güncelleme → "Uygulama güncellemesi":
1. **Kaynak** olarak GitHub deponuzu (`sahip/depo`) yazın. Depo herkese açık olmalı. (Alternatif: `https://` ile bir JSON adresi: `{"version":"3.6","url":"https://.../app.apk","notes":"..."}`)
2. **Güncellemeyi kontrol et** son sürümü sorar. Yeni sürüm varsa notlarıyla birlikte **İndir ve kur** çıkar. İndirme sistem indirme yöneticisiyle yapılır, bitince Android kurulum ekranı açılır.
3. Kaynak girildiyse uygulama günde en fazla 2 kez kendiliğinden kontrol eder ve ana ekranda "Yeni sürüm mevcut" şeridi gösterir.
- Sürüm yayınlamak için: `app/build.gradle` içindeki `versionName`/`versionCode` değerini artırıp `main` dalına gönderin. `.github/workflows/apk.yml` APK'yı derler ve `v<sürüm>` adıyla bir GitHub Release oluşturur.
- Güncelleme ancak **aynı imzayla** (aynı `optimus.keystore`) derlenmiş APK'larda kurulur. Anahtar dosyasını değiştirmeyin.
- İlk kurulumda Android "bilinmeyen kaynaklardan kurulum" izni isteyebilir.
- Android TV'de kurulum ekranı kumandayla onaylanır.

### Ana ekran düzeni (3.6)
- Üst çubuk: robot logo + uygulama adı | büyük saat ve tarih | arama ("Ara": Canlı, Film, Dizi içinde eşleşen ilk bölümü açar) | yenile, liste ekle, ayarlar simgeleri.
- Üç büyük kart: Canlı Yayın (+N Kanal), Filmler (+N Film), Diziler (+N Dizi). Altında küçük kutular (Favoriler, Geçmiş, İndirilenler, 4K, Kategoriler, Liste ekle, Merkez) ve afiş satırları.
- Koyu mor cam görünümü; telefon dikeyde tek sütun, TV'de kumandayla gezilir.

### Liste ekranı (3.7): 3 bölmeli düzen
- Üst çubuk: **‹ Geri** (ana ekrana), bölüm hapları (Canlı Yayın / Filmler / Diziler / İndirilenler), **Ara** ve ayarlar simgesi (kaynak/ayar çekmecesi: liste ekleme, dil, motor, tema, FX).
- Sol: kanal arama ve sayılı kategori listesi. Orta: seçili kategorinin kanalları (seçili kanal beyaz). Sağ: oynatıcı ve **program bilgisi** kartı (Xtream girişinde canlı kanalın şu anki/sıradaki programı; yoksa "Program bilgisi yok").
- Dar telefonda (dikey) oynatıcı üstte, kategoriler yatay çiplerle, liste altta.
- Liste yüklenmemişse kaynak çekmecesi kendiliğinden açılır.

### 4K Ultra HD (3.8)
- 4K, UHD, Ultra HD, 2160p, HEVC/H.265 geçen içerikler altın **4K UHD** rozeti alır, listede **🎞 4K Ultra HD** kategorisinde ve ana ekranda ayrı kutu/satırda toplanır.
- VLC oynatıcı çözünürlüğü "3840×2160 4K Ultra HD HEVC" olarak gösterir.

### 4K görüntü testi (3.9)
Merkez → Kontrol Et → "4K görüntü testi":
- **4K testini çalıştır:** ekran çözünürlüğünü ve H.264 / HEVC / VP9 / AV1 4K çözme desteğini (akıcı mı, donanımlı mı) sınar.
- **Test desenini göster:** tam ekran 3840×2160 renk çubuğu, gri ton, 1 pikselli dama ve ızgara. Kapatmak için dokunun veya Geri.
- Yerel/tek video oynatılırken bilgi satırı çözünürlüğü etiketler (örn. "3840×2160 (4K Ultra HD)").

## Ayarlar ekranı (v4.0)
Ana ekrandaki dişli veya kumandanın MENU tuşu tam ekran **Ayarlar** sayfasını açar: çalma listesi ekle/değiştir, ebeveyn kontrolü, dil, düzen, kategori gizleme, geçmiş temizleme, canlı kanal sıralama, canlı yayın formatı (m3u8/ts), harici oynatıcı, otomatik güncelleme, 12/24 saat, altyazı boyutu/rengi, cihaz türü ve güncelleme.

## Tam ekran oynatıcı (v4.1)
Kanala dokununca uygulama içi oynatıcı tam ekran açılır (üst: geri/ad/saat; alt: önceki-sonraki kanal, oynat/duraklat, sığdır-doldur-uzat, ses, VOD için konum çubuğu). Ekrana dokununca denetimler gizlenir/gösterilir; Geri/Esc kapatır. Ayarlar > Düzeni Değiştir’den kapatılabilir; sahnedeki ⛶ düğmesiyle elle de açılır.

## Siyah ekran düzeltmesi (v4.2)
VLC oynatıcıda zorunlu donanım çözücü ayarı kaldırıldı (otomatik seçim). Oynatma başlayıp 6 sn içinde görüntü gelmezse bir kez yazılım çözücüyle otomatik yeniden denenir.

## Sunucu ekle (v4.3)
Ayarlar > Sunucu Ekle: ad, sunucu adresi, kullanıcı adı ve şifre ile birden fazla Xtream sunucusu kaydedilir; listeden tek dokunuşla yüklenir veya silinir.

## M3U listesi ekle (v4.4)
Ayarlar > M3U Listesi Ekle: liste adı + bağlantı (kaydet / kaydet ve yükle), dosyadan seçme, metin yapıştırma; kayıtlı listeler tek dokunuşla yüklenir veya silinir.

## Hata düzeltmeleri (v4.5)
TV’de tam ekran oynatıcı: OK ile açılınca yayının duraklaması giderildi; denetimler gizliyken ↑/↓ kanal değiştirir, ←/→ 10 sn atlar, OK denetimleri açar.

## M3U bağlantısı aktifleştirme (v4.6)
Kayıtlı M3U bağlantılarında “Aktifleştir” ile yükleme; kullanılan bağlantı “● Aktif” olarak işaretlenir ve yükleme sonrası “M3U bağlantısı aktif” bilgisi gösterilir.

## M3U bağlantılarını denetle (v4.7)
Ayarlar > M3U Listesi Ekle > “Bağlantıları denetle”: kayıtlı her bağlantı sırayla indirilir; çalışıyor (kanal sayısı, süre), boş, geçerli M3U değil, HTTP hatası veya ulaşılamıyor olarak gösterilir.

## Tüm verileri test et (v4.8)
Ayarlar > Tüm Verileri Test Et: kayıtlı verilerin bütünlüğü, aktif listedeki kanallar (boş ad, geçersiz/yinelenen adres), favori/geçmiş uyumu, tüm M3U bağlantıları, Xtream sunucuları (giriş, durum, bitiş tarihi, bağlantı sayısı) ve 8 rastgele canlı kanalın erişilebilirliği denetlenir.

## Sunucu değiştir (v4.9)
Ayarlar > Sunucu Değiştir: kayıtlı Xtream sunucuları listelenir, aktif olan “● Aktif” işaretlidir; birine dokununca o sunucuya geçilir ve kanalları yüklenir.

## Ana ekranda sunucu değiştir (v4.10)
Ana ekranın sağ üstündeki ⇄ düğmesi doğrudan “Sunucu Değiştir” penceresini açar.

## M3U ve Xtream tek yerden değiştirme (v4.11)
“Sunucu Değiştir” (ana ekrandaki ⇄ veya Ayarlar) hem Xtream sunucularını hem kayıtlı M3U bağlantılarını listeler; aktif olan “● Aktif” işaretlidir, dokununca geçilir.

## M3U / M3U8 oynatıcı (v4.12)
Ayarlar > M3U / M3U8 Oynatıcı: bağlantı yapıştırın. Tek yayın (.m3u8, .ts, .mp4 vb.) doğrudan tam ekran oynatılır; kanal listesi (.m3u) yüklenir. Yerel .m3u/.m3u8 dosyası da açılabilir.

## Sunucu kategorileri (v4.14)
Ayarlar > Sunucu Kategorileri: aktif listenin kategorileri (Canlı/Film/Dizi, kanal sayısıyla) listelenir, birine dokununca o kategoriye gidilir. Kayıtlı Xtream sunucularından birini seçince kategori adları sunucudan alınıp gösterilir; “Bu sunucuya geç” ile yüklenir.

## http:// adresleri (v4.15)
M3U adreslerinde http:// yazılmasa da otomatik eklenir (örn. sunucu:8080/liste.m3u); düz http bağlantılar desteklenir. Adres alanlarında tarayıcının katı URL doğrulaması kapatıldı, Enter ile gönderilebilir.

## Notlar
- Alt sürüm: Android 8.0 (API 26) ve üstü. Android TV'de de açılır.
- İmza anahtarı (`app/optimus.keystore`) herkese açık örnek bir anahtardır. Kendi anahtarınızı kullanmak için
  `app/build.gradle` içindeki `signingConfigs` bölümünü değiştirin.
- Oynatıcı kütüphaneleri (hls.js, mpegts.js) internetten yüklenir, cihaz çevrimiçi olmalı.
