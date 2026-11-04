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

## QR kod ile sunucu ekle + yedek (v4.16)
Ayarlar > QR Kodu ile Sunucu Ekle: kamerayla (veya galeriden resim / yapıştırılan metinle) QR okunur. Desteklenenler: Xtream bağlantısı (get.php?username=…&password=…, /live/kullanıcı/şifre/…), M3U adresi, xtream://sunucu|kullanıcı|şifre, JSON {server,user,pass,name}. Okunan sunucu önizlenir, kaydedilir (ve istenirse yüklenir). “Sunucuları yedekle / Yedekten yükle” ile sunucular ve M3U bağlantıları metin veya .json dosyası olarak dışa/içe aktarılır. Android: CAMERA izni eklendi (kamera ilk kullanımda izin ister). Cihazda BarcodeDetector yoksa QR çözücü (jsQR) internetten yüklenir.

## Android TV uyumu (v4.17)
Ayarlar ve tüm pencerelerde kumanda (D-pad) ile gezinme doğrulandı: tüm kutulara ulaşılır, pencere açılınca odak pencerenin içine geçer, Geri tuşu kapatır. Kamerası olmayan TV’de QR penceresi kamera düğmesini gizler ve resim / metin yapıştırma / yedekten yükleme yollarını önerir. Pencere yeniden çizilince odak kaybolması giderildi.

## TV kumanda iyileştirmeleri (v4.18)
Ana ekranda arama kutusundan sağ tuşla üst düğmelere (yenile, liste ekle, sunucu değiştir, ayarlar) geçilebilir (imleç metnin sonundaysa). Pencere (ör. QR, sunucu) kapanınca odak, onu açan kutuya döner. Ayarlar’daki 29 öğenin tamamına yön tuşlarıyla ulaşılır; geri okuna yukarı tuşuyla çıkılır.

## TV: küçük ekran / büyük ekran (v4.19)
TV’de kanal seçince yayın sağ üstteki küçük ekranda açılır, liste solda kalır. Aynı kanala tekrar OK’a basınca (veya ekrandaki “Büyük ekran” düğmesiyle, ya da F tuşuyla) yayın yeniden başlamadan büyük ekrana geçer; Geri / “Küçük ekran” küçük ekrana döner ve odak listeye geri gelir. Büyük ekranda denetimler gizliyken ↑/↓ kanal değiştirir.

## TV + telefon uyum taraması (v4.20)
7 ekran boyutunda (telefon 360×640, 390×844, yatay 844×390, tablet dikey/yatay, TV 1280×720 ve 1920×1080) ana ekran, liste, Ayarlar ve pencereler taşma/kırpılma için tarandı. Düzeltmeler: TV 1280×720’de üst simgeler taşıyordu (küçültüldü); küçük dikey telefonda kanal listesi ekran altında kalıyordu (kompakt başlık/arama, gizlenen hız çubuğu ve program ayrıntısı).

## Ayrıntılı kategoriler (v4.21)
Kategori penceresi her kategori için kanal sayısı çubuğu, favori / 4K / izlenen sayıları, küçük logolar ve örnek kanal adlarını gösterir. Üstte toplam kategori ve kanal sayısı, “Sıra: A–Z / çok kanal” ve “Görünüm: ayrıntılı / kompakt” seçenekleri vardır (tercih hatırlanır). Pencereler açık sistem temasında da okunaklı (koyu) görünür.

## Oynatıcıyı etkinleştir (v4.22)
Ayarlar > Oynatıcıyı Etkinleştir: etkin oynatıcı (VLC / MX Player / uygulama içi) seçilir ve “Test yayınını oynat” ile herkese açık bir HLS test yayını seçili motorla açılır. Uygulama içi motorda 12 sn içinde görüntü gelmezse yedek test adresi denenir ve sonuç (çalışıyor / çalışmıyor) gösterilir; VLC/MX’te görüntü gelip gelmediğini kullanıcı görür. “Sorun mu var?” düğmesi tek dokunuşla uygulama içi oynatıcıya geçirir.

## Yeni ana ekran (v4.23)
- Örnek ekrana göre başlatıcı: Canlı / Filmler / Seri / Hesap / Çalma Listesini Değiştir + Ayarlar / Yeniden Yükle / ÇIKIŞ.
- Sağ üstte liste süre dolumu (Xtream), sağ altta sürüm. TV'de kumanda ile gezinme, telefonda alt alta düzen.
- ÇIKIŞ: Android'de uygulamayı kapatır (Native.exitApp).

## Yeni Ayarlar ekranı (v4.24)
- Örnek ekrana göre mor zemin, pembe kutular, 4 sütun; sıra örnektekiyle aynı, cihaz kimliği/anahtarı 20. kutudan sonra.

## Yeni Canlı ekran (v4.25)
- Örnek ekrana göre: üstte Anasayfa | Canlı | Filmler | Seri + arama, solda grup çubukları, ortada kanal listesi, sağda yayın + program listesi, altta Yakala / Favorilere Ekle / Arama.

## Yeni Filmler / Seri ekranı (v4.26)
- Afiş ızgarası: solda kategori listesi + Geri/Arama, sağda sıralama kutusu, grup başlığı ve puan etiketli afişler. Bir afişe basınca tam ekran oynatır; Geri ile dönünce yayın durur.

## Seri ekranı (v4.27)
- Filmlerle aynı afiş ızgarası, solda klaket simgesi; ızgara geniş ekranda 5 sütun. Bir diziye basınca bölüm listesi açılır.

## Oynatıcı seçimi (v4.28)
- Canlı ekranda ve tam ekran çubuğunda "Oynatıcı: VLC/MX/Dahili" düğmesi: VLC, MX Player veya uygulama içi oynatıcıyı seçip kanalı hemen o oynatıcıyla açar.

## Profesyonel oynatıcı (v4.29)
- Tam ekran çubuğunda ⚙ Pro paneli: hız, kalite (HLS), ses dili, altyazı, görüntü oranı, uyku zamanlayıcı, istatistik, PiP. ±10 sn düğmeleri, ses kaydırıcısı, çift dokunuşla ileri/geri, J/L/[ ]/I kısayolları, film ve bölümlerde kaldığın yerden devam.

## Harici Oynatıcılar penceresi (v4.30)
- Örnek düzen: Varsayılan / VLC Oynatıcı / MX Oynatıcı onay kutuları, TAMAM / İPTAL. Canlı ekrandaki "Oynatıcı" düğmesi de aynı pencereyi açar.

## Player Değiştir (v4.31)
- Üst çubukta (tüm bölümlerde), canlı ekran düğmelerinde ve tam ekran çubuğunda "Player Değiştir" düğmesi; seçili oynatıcı yanında görünür.

## Oynatıcı Ayarları (v4.32)
- Ayarlar > Oynatıcı Ayarları: oynatıcı seçimi, varsayılan görüntü oranı ve hız, arabellek, tam ekran, düşük gecikme, kaldığın yerden devam, altyazı / format kısayolları, test yayını.

## Harici Oynatıcılar düzeltmesi (v4.33)
- Kumandayla bir satır seçince odak başa atlıyordu; düzeltildi. Hiç seçim yapılmamışsa "Varsayılan" işaretli gelir (örnek ekran gibi).

## İntroyu atla (v4.34)
- Açılış animasyonunda "İntroyu Atla" düğmesi; kumandada herhangi bir tuş da atlar. Ayarlar > Açılış İntrosu ile tamamen kapatılabilir.
- Dizi bölümlerinde tam ekranda ilk 4 dakikada "İntroyu Atla" düğmesi (N tuşu): varsayılan 85 sn ileri; süre Oynatıcı Ayarları'ndan değişir.

## Eklenen Sunucular (v4.35)
- Ayarlar > Eklenen Sunucular: her Xtream sunucusu ve M3U bağlantısı ayrı kart; durum, bitiş tarihi, bağlantı sayısı, Aktif et / Denetle / Sil.

## Sunucu kaydet / sil (v4.36)
- Eklenen Sunucular: kaydedilmemiş etkin sunucu/bağlantıda Kaydet düğmesi, Düzenle, onaylı Sil ve Tümünü sil.

## Yeni logo görünümü (v4.37)
- Ana ekranda dönen renkli halka, yörünge kıvılcımı, nabız gibi parlayan hale ve krom OPTİMUS yazısı; liste ekranında renk değiştiren logo parıltısı. Robot efektleri (FX) kapalıyken animasyonlar durur.

## 4K ve Ses Ayarları (v4.38)
- Ayarlar > 4K ve Ses Ayarları: kalite tercihi (otomatik / en yüksek / 1080p / 720p), 4K-HEVC yayınlarda otomatik VLC, ses seviyesi (kalıcı), tercih edilen ses dili, ses testi, 4K testi, varsayılanlara sıfırla.

## 4K Desteği (v4.39)
- "4K ve Ses Ayarları" penceresinde "Bu cihazın 4K desteği" bölümü: ekran çözünürlüğü, HDR, H.264/HEVC/VP9/AV1 desteği, 4K HEVC akıcılık testi.
- PlayerActivity (VLC): 4K/UHD görünümlü yayınlarda 5 sn önbellek; video yüksekliği ≥2000 ise ekranın en yüksek çözünürlük/yenileme moduna geçiş (Android 6+).

## Sunucu Seç (v4.40)
- Ayarlar'da "Sunucu Seç" kutusu; ana ekran sunucu simgesi ve "Çalma Listesini Değiştir" de bu pencereyi açar. Kayıtlı Xtream sunucuları ve M3U bağlantıları işaretlenerek seçilir, TAMAM ile yüklenir; aktif olan "● aktif" görünür.

## HDR10+ Desteği (v4.41)
- "4K ve Ses Ayarları"nda ekranın HDR10 / HDR10+ / Dolby Vision / HLG desteği listelenir (Android'de `Native.hdrTypes()`, API 24+).
- Adında HDR, HDR10+, Dolby Vision, DV veya HLG geçen yayınlar otomatik VLC ile açılır (ayarlardan kapatılabilir).

## Görüntü ve Ses Biçimleri (v4.42)
- Kanal adı/kategorisinde geçen 4K, HDR10, HDR10+, HLG, Dolby Vision, Dolby Atmos, DTS:X ve Dolby Audio (DD / DD+ / AC3 / E-AC-3) etiketleri listede rozet olarak gösterilir.
- HDR ve Dolby/DTS etiketli yayınlar Android'de otomatik VLC ile açılır (4K ve Ses Ayarları'ndan kapatılabilir).

## Sunucu Listesi Kalıcı (v4.43)
- Giriş yapılan her Xtream sunucusu otomatik olarak kayıtlı listeye eklenir.
- Sunucu, M3U bağlantısı, aktif kaynak ve favoriler Android'de ayrıca yerel belleğe (SharedPreferences `optimus_kv`, `Native.saveKV/loadKV`) yazılır; WebView verisi silinirse açılışta geri yüklenir. Uygulamayı kaldırıp yeniden kurunca bellek de silinir; bunun için "Sunucuları yedekle" kullanın.

## Film ve Dizi İndirme (v4.44)
- Filmler posterlerinde sol üstte ⬇ düğmesi (indirirken yüzde, bitince ✓, tekrar dokununca oynat/iptal).
- Dizi posterindeki ⬇ diziyi açar; bölümlerde ⬇ ve "Sezonu indir" kullanılır.
- Tam ekran oynatıcıda film/bölüm için "⬇ İndir" düğmesi.
- İndirilenler sekmesi/ana ekran kutusu ve Wi-Fi kısıtı daha önce vardı; indirmeler Android DownloadManager ile sırayla yapılır.

## 4K Yayın Oynatma (v4.45)
- Tarayıcı oynatıcısı bir yayını çözemezse (HEVC/4K kalite, ölümcül medya hatası, açılamayan biçim, 6 sn sonra hâlâ siyah ekran) Android'de otomatik VLC'ye geçilir; her yayın için bir kez denenir.
- HLS listesi yalnızca HEVC/4K sunuyorsa ve tarayıcı HEVC çözemiyorsa doğrudan VLC açılır.
- 4K ve Ses Ayarları'ndaki "4K / HEVC yayınları otomatik VLC ile aç" anahtarı bu geçişi de kapatır.

## Yeni Logo Arka Planı (v4.46)
- Uygulama simgesi (adaptif ikon) arka planı koyu lacivertten mor→macenta→kırmızı degradeye (+ merkezde yumuşak ışık) değiştirildi: `res/drawable/ic_bg.xml`.
- Android TV afişi (`banner.png`) aynı degrade ile yeniden çizildi (ızgarasız).
- Rengi değiştirmek için `ic_bg.xml` içindeki startColor/centerColor/endColor değerlerini düzenleyin.

## QR ile Uzaktan Erişim (v4.47)
- Ayarlar > "QR ile Uzaktan Erişim": cihaz yerel ağda küçük bir sunucu açar (`RemoteServer.java`, port 8765+) ve ekranda QR kodu gösterir.
- Telefon kamerasıyla QR okutulunca `assets/remote.html` açılır: yön tuşları, TAMAM, Geri, Ana, CH ±, ses, oynat/duraklat, tam ekran ve "Sunucu / M3U gönder" formu.
- Adres her başlatmada değişen 6 haneli anahtar içerir; yalnızca aynı Wi-Fi ağından erişilir. Durdur düğmesiyle kapatılır.
- Not: Sunucu cihazın kendi ağında düz HTTP kullanır; ortak/güvenilmez ağlarda açık bırakmayın.

## QR ile Sunucu Al / Paylaş (v4.48)
- "QR Kodu ile Sunucu Al": kamera / galeri / yapıştırma ile Xtream sunucusu veya M3U adresini içe alır (jsQR gerekirse internetten yüklenir).
- Yeni: "Eklenen Sunucular"da her sunucu ve M3U bağlantısında "QR göster" düğmesi; başka cihaz bu kodu okutarak sunucuyu alır. Kod `xtream://sunucu|kullanıcı|şifre|ad` biçimindedir (şifre içerir!).
- QR üretici sürüm 1–10'a genişletildi (~210 bayta kadar).

## Telefon Kamerasıyla QR Tarama (v4.49)
- "QR Kodu ile Sunucu Al" penceresinde "📷 Telefon kamerasıyla tara": Google Play Hizmetleri kod tarayıcısı (`play-services-code-scanner`, `Native.scanQr()`) açılır; kamera izni gerekmez, sonuç `__qrResult` ile sayfaya gelir.
- Cihazda Play Hizmetleri yoksa veya tarayıcı açılamazsa uygulama içi kameraya (getUserMedia) otomatik geçilir. Android TV'de düğme gösterilmez.
- build.gradle'a yeni bağımlılık eklendi; ilk derlemede Gradle bunu indirir.

## Film ve Dizi Konu Bilgisi (v4.50)
- Film/dizi posterlerinde ve bölüm satırlarında ℹ düğmesi; dizi bölüm listesinde "ℹ Konu"; tam ekran oynatıcıda "ℹ Konu".
- Pencere: afiş, puan, tür, çıkış tarihi, süre, ülke, yönetmen, oyuncular, konu ve fragman kodu; Oynat / İndir / Favori düğmeleri.
- Bilgi Xtream sunucusundan (`get_vod_info`, `get_series_info`) alınır ve önbelleğe konur; sunucu vermezse "konu bilgisi bulunamadı" yazar. Yalnızca Xtream girişinde çalışır (M3U listelerinde konu bilgisi yoktur).

## İndirme Düğmeleri (v4.51)
- Dizi posterindeki ⬇ artık "Diziyi İndir" penceresini açar: "Tüm dizi (N bölüm)" ve her sezon için ayrı düğme.
- Dizi bölüm listesinin başında "Sezonu indir" yanında "⬇ Tüm dizi"; dizi bilgi penceresinde "⬇ İndir".
- Film posterinde ⬇, bölüm/film satırlarında büyütülmüş ⬇ düğmesi, tam ekranda "⬇ İndir" (v4.44). İndirme yalnızca Android uygulamasında çalışır.

## ExoPlayer (v4.52)
- Yeni oynatıcı seçeneği: ExoPlayer (AndroidX Media3 1.4.1; HLS, DASH, MP4/MKV, HEVC, HDR; `ExoActivity.java`).
- Seçim: Player Değiştir / Harici Oynatıcılar / Oynatıcıyı Etkinleştir ve "Motor" düğmesi (VLC → ExoPlayer → MX → Web).
- Ses, altyazı ve kalite seçimi oynatıcı çubuğundaki ayar düğmesindedir. ExoPlayer yayını çözemezse otomatik VLC'ye geçilir. İndirilen dosyalar da seçili oynatıcıyla açılır.
- build.gradle'a media3 bağımlılıkları eklendi; ilk derlemede Gradle indirir.

## Ayrıntılı Görünüm (v4.53)
- Canlı kanallar: her satırda şu anki program (saat aralığı + başlık), ilerleme çubuğu ve "Sonra:" programı (Xtream EPG; ekrandaki satırlar için yavaş yavaş yüklenir). Listenin üstündeki "Ayrıntı: Açık/Kapalı" düğmesiyle kapatılır.
- Filmler / Diziler: poster kartlarında puan · yıl · tür satırı; "☰ Ayrıntılı liste" düğmesiyle afiş + başlık + puan/yıl/tür/süre + konu özeti olan liste görünümü.
- Veriler en fazla 3 eşzamanlı istekle ve yalnızca görünen satırlar için alınır.

## Tam Ekranda Sol Kanal Listesi (v4.54)
- Canlı yayın tam ekranken sol tarafta kanal listesi paneli: ← tuşu (kumanda), alt çubuktaki "☰ Kanallar" düğmesi veya ekranın sol kenarından sağa kaydırma ile açılır.
- ↑/↓ ile gezilir, TAMAM'la kanal değişir (yayın kesilmeden tam ekranda kalır); → / ← / Geri ile kapanır; 9 sn işlem olmazsa kendiliğinden kapanır.
- Her satırda numara, logo, kanal adı ve (Xtream'de) şu an oynayan program; mevcut kanal vurgulanır. Çok uzun listelerde "Önceki / Sonraki kanallar" düğmeleri.

## Notlar
- Alt sürüm: Android 8.0 (API 26) ve üstü. Android TV'de de açılır.
- İmza anahtarı (`app/optimus.keystore`) herkese açık örnek bir anahtardır. Kendi anahtarınızı kullanmak için
  `app/build.gradle` içindeki `signingConfigs` bölümünü değiştirin.
- Oynatıcı kütüphaneleri (hls.js, mpegts.js) internetten yüklenir, cihaz çevrimiçi olmalı.

## Geri Tuşunda Küçük Ekran (v4.55)
Tam ekran canlı yayında (VLC veya ExoPlayer) **Geri** tuşuna basınca oynatıcı kapanmaz; yayın küçük bir pencereye (Android "Resim içinde Resim") geçer.
- Android TV'de pencere sağ üst köşeye yerleşir; telefonda sürüklenebilir (konum sistem tarafından belirlenir).
- Küçük pencere kapatılırsa oynatıcı da kapanır. Pencereye dokununca tam ekrana döner.
- Uygulamada başka kanal seçilirse küçük pencerede yayın değişir.
- Cihaz PiP desteklemiyorsa Geri eskisi gibi kapatır.

## Sağ Üstte Mini Oynatıcı (v4.56)
Tam ekran canlı yayında (web oynatıcı) **Geri / Esc** veya "Mini ekran" düğmesi yayını sağ üst köşede yüzen küçük bir ekrana alır; yayın durmaz, arkada kanal listesi kullanılabilir.
- ⤢ veya mini ekrana tıklama: tam ekrana döner. ✕: mini ekranı kapatır (yayın durur). Mini ekran açıkken Geri de kapatır.
- Mini ekran açıkken başka kanal seçilirse yayın değişir (telefonda otomatik tam ekran açıksa tam ekrana geçer).
- Sadece canlı yayında; film/dizide Geri eskisi gibi çalışır. VLC/ExoPlayer'da v4.55'teki Android PiP kullanılır.

## Kanal Yayın Akışı (v4.57)
Canlı kanalların program rehberi (yayın akışı). Tam ekran çubuğundaki **📅 Yayın Akışı** veya canlı görünümdeki **Yayın Akışı** düğmesi açar.
- Günlere ayrılmış program listesi; şu an yayındaki program vurgulu (▶), biten programlar soluk. Açılışta şimdiki programa kaydırır.
- TV kumandasıyla yukarı/aşağı kaydırılır, Geri kapatır.
- Veri Xtream sunucusunun EPG bilgisinden gelir (get_simple_data_table, yoksa get_short_epg). M3U kaynaklarında gösterilmez.

## Yayın Bilgileri (v4.58)
Tam ekran çubuğundaki **ℹ Yayın Bilgisi** veya canlı görünümdeki **Yayın Bilgileri** düğmesi, oynayan yayının ayrıntılarını saniyede bir güncellenen bir pencerede gösterir.
- Ad, grup, tür, oynatıcı (VLC/ExoPlayer/MX/Web), adres (kullanıcı adı ve şifre gizli), biçim, kalite etiketleri (4K, HDR, Dolby...).
- Web oynatıcıda: çözünürlük, görüntü/ses kodeği, kare hızı, bit hızı, kalite kademeleri, bant genişliği, ses izleri, tampon, kare kaybı, süre, durum.
- Xtream canlı kanalda şimdi/sonraki program. **Kopyala** düğmesi bilgileri panoya alır.
- VLC/ExoPlayer/MX'te çözünürlük ve kodek bilgisi o oynatıcının kendi ekranındadır.

## Tarayıcıda Film ve Dizi İndirme (v4.59)
Android uygulaması dışında (tarayıcıda/HTML) de **⬇ İndir** düğmeleri çalışır: film ve bölüm doğrudan indirme bağlantısıyla tarayıcıya indirilir. Dizi düğmesi sezon/bölüm listesini açar; tarayıcıda bölümler tek tek indirilir.
- Android uygulamasında eski davranış sürer: kuyruk, ilerleme, İndirilenler ekranı, toplu sezon/dizi indirme.
- Tarayıcı, başka bir siteden gelen dosyada dosya adını sunucudan alabilir ya da videoyu yeni sekmede açabilir (sunucu izin vermiyorsa).
