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

## Notlar
- Alt sürüm: Android 8.0 (API 26) ve üstü. Android TV'de de açılır.
- İmza anahtarı (`app/optimus.keystore`) herkese açık örnek bir anahtardır. Kendi anahtarınızı kullanmak için
  `app/build.gradle` içindeki `signingConfigs` bölümünü değiştirin.
- Oynatıcı kütüphaneleri (hls.js, mpegts.js) internetten yüklenir, cihaz çevrimiçi olmalı.
