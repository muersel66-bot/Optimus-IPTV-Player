# ⚡OPTİMUS™⚡ İptv Player PRO (Android)

IPTV-Player für Android-Handys und Android TV: M3U-Listen, **Xtream Codes** und **Stalker-Portale**, Wiedergabe mit dem eingebauten **VLC-Motor** (Dolby Digital, Dolby Digital Plus, DTS), alternativ Web-Player oder MX Player.

> Türkische Originalanleitung: [docs/README.tr.md](docs/README.tr.md)

## 📲 Download

Die fertige APK gibt es unter **[Releases](../../releases/latest)**:

- `OPTIMUS-IPTV-PRO-<version>-arm64-v8a.apk` – für fast alle aktuellen Handys (empfohlen)
- `OPTIMUS-IPTV-PRO-<version>-armeabi-v7a.apk` – ältere 32-Bit-Geräte / TV-Boxen
- `OPTIMUS-IPTV-PRO-<version>-universal.apk` – läuft überall

Auf dem Handy öffnen und „Installation aus unbekannten Quellen" für den Browser/Dateimanager erlauben. Mindestens Android 8.0.

## Funktionen

- M3U per Link, Datei oder Text · Xtream-Login · Stalker-Portal (MAC)
- Kategorien, Suche, Favoriten, letzter Sender wird gemerkt
- VLC-Player: Senderwechsel (◀ ▶, Fernbedienung ↑↓/CH±), Tonspur- und Untertitelwahl, Bildformat, Auto-Reconnect, Bild-im-Bild
- Audio-Codec-Anzeige (z. B. „Ses: Dolby Digital Plus 5.1")
- Einzelvideos (URL oder Datei) mit .srt/.vtt-Untertiteln im Web-Player
- Hell / Dunkel / System, randlose Darstellung mit Kamera-Loch-Unterstützung, Zurück-Geste mit Exit-Bestätigung

## Selbst bauen

**Ohne PC (GitHub Actions):** Jeder Push baut die APKs (Reiter *Actions* → Lauf → *Artifacts*).
Ein Release mit APKs entsteht automatisch, wenn ein Tag gepusht wird:

```bash
git tag v1.4.0 && git push origin v1.4.0
```

oder über *Actions → „APK bauen & veröffentlichen" → Run workflow* mit Release-Tag.

**Lokal / Android Studio:** JDK 17 + Android SDK 34, dann

```bash
./gradlew assembleRelease
# → app/build/outputs/apk/release/*.apk
```

## Signatur

`app/optimus.keystore` ist ein **öffentlicher Beispiel-Schlüssel** (damit Updates über ältere Versionen installierbar bleiben). Für einen eigenen Schlüssel in GitHub unter *Settings → Secrets and variables → Actions* anlegen:

| Secret | Inhalt |
|---|---|
| `SIGNING_KEYSTORE_BASE64` | `base64 -w0 mein.keystore` |
| `SIGNING_STORE_PASSWORD` | Keystore-Passwort |
| `SIGNING_KEY_ALIAS` | Alias |
| `SIGNING_KEY_PASSWORD` | Schlüssel-Passwort |

Hinweis: Nach einem Schlüsselwechsel muss die alte App einmal deinstalliert werden.

## Aufbau

- `app/src/main/assets/index.html` – Oberfläche (läuft in einer WebView), `assets/lib/` – hls.js & mpegts.js
- `MainActivity.java` – WebView-Hülle, nativer Netzwerk-Bridge (kein CORS), Systemleisten/Insets, Zurück-Logik
- `PlayerActivity.java` – Vollbild-VLC-Player

Lizenzen: App MIT (siehe `LICENSE`); hls.js Apache-2.0; mpegts.js Apache-2.0; libVLC LGPL-2.1+.
