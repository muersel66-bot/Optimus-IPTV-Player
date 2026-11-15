package com.iptvplayerpro.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class PlaylistRepository(context: Context) {
    private val prefs = context.getSharedPreferences("playlist", Context.MODE_PRIVATE)

    var savedUrl: String
        get() = prefs.getString("url", "") ?: ""
        set(v) = prefs.edit().putString("url", v).apply()

    var savedName: String
        get() = prefs.getString("name", "") ?: ""
        set(v) = prefs.edit().putString("name", v).apply()

    /** Downloads and parses an M3U/M3U8 link. Throws on network or format errors. */
    suspend fun load(url: String): List<Channel> = withContext(Dispatchers.IO) {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "IPTVPlayerPro/0.1")
        }
        try {
            if (conn.responseCode !in 200..299) error("Server returned HTTP ${conn.responseCode}")
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val channels = M3uParser.parse(text)
            if (channels.isEmpty()) error("No channels found in this playlist")
            channels
        } finally {
            conn.disconnect()
        }
    }
}
