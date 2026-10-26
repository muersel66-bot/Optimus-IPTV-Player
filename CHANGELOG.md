## OPTİMUS İptv Player PRO 1.4.0

### Welche APK soll ich installieren?
| Datei | Für |
|---|---|
| `…-arm64-v8a.apk` | **Fast alle aktuellen Handys** (empfohlen, kleiner) |
| `…-armeabi-v7a.apk` | Ältere 32-Bit-Handys / viele TV-Boxen |
| `…-universal.apk` | Läuft überall (größer) – nimm diese, wenn du unsicher bist |

Installation: APK auf dem Handy öffnen → „Aus dieser Quelle installieren" erlauben. Updates lassen sich über eine ältere Version installieren (gleiche Signatur).

### Neu & verbessert
- **Handy-Anzeige:** Randlose Darstellung ab Android 11 – nichts liegt mehr unter Kamera-Loch, Statusleiste oder Gestenleiste. Statusleisten-Symbole passen sich dem Theme an, kein weißes Aufblitzen beim Start im Dunkelmodus.
- **Zurück-Geste:** Schließt zuerst Suche, Kategorie-Filter oder das „Liste yükle"-Panel; die App beendet sich erst nach zweimaligem Zurück („Çıkmak için tekrar geri tuşuna basın").
- **VLC-Player:** Neue Steuerleiste (Pause/Weiter, Vor/Zurück, Ses, **Altyazı**, **Bildformat** Sığdır/Doldur/16:9/4:3/Orijinal – wird gemerkt), Auflösung + Audio-Codec in der Infozeile, **automatisches Neuverbinden** bei Abbrüchen, **Bild-im-Bild (PiP)** beim Wechsel auf den Homescreen, Bedienelemente respektieren das Kamera-Loch.
- **Offline-fähig:** hls.js und mpegts.js sind jetzt in der App enthalten (vorher aus dem Internet geladen).
- **Theme:** Sistem / Açık / Koyu.
- Größere Touch-Flächen (Favoriten-Stern, Buttons, Tabs), 16-px-Eingabefelder ohne Zoom-Sprung, passende Tastaturen.
- Xtream-Server und Benutzername werden gemerkt.
- Fix: Kompilierfehler mit libVLC 3.6 (`IMedia`) behoben; Stream-Abfragen schließen Verbindungen sauber.
- Build: getrennte APKs pro Prozessor + Universal-APK, GitHub Actions baut bei jedem Tag automatisch ein Release, optional eigener Signaturschlüssel über Secrets.
