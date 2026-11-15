# IPTV Player Pro – Android starter (phone · tablet · Android TV)

First iteration: M3U link -> channel list (with groups) -> ExoPlayer (Media3) playback.

## Build the APK
1. Open this folder in Android Studio (Koala or newer). It downloads the Android SDK and Gradle for you.
2. Build ▸ Build APK(s). Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
3. Install on a phone, or on Android TV via `adb connect <tv-ip>` then `adb install app-debug.apk`.

## Included
- M3U / M3U8 parser (group-title, tvg-logo, channel names) and saved playlist
- Compose UI in the design colours; pink focus frame for the TV remote (D-pad, OK, Back)
- Player: Up/Down or CH+/CH- change channel, OK pauses, hand-off to MX Player / VLC if installed
- Manifest ready for phone, tablet and Android TV (leanback launcher)

## Not yet included (next iterations)
- Xtream Codes and Stalker Portal login, QR add-server (needs a small backend)
- Movies / Series screens with posters, EPG, catch-up
- Dolby Atmos / DTS audio passthrough settings, language switcher
- App icon and TV banner (the default icon is used now; add them before publishing)
