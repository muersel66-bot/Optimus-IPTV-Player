package com.iptvplayerpro.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayerpro.data.Channel
import com.iptvplayerpro.data.PlaylistRepository
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PlaylistRepository(app)

    var channels by mutableStateOf<List<Channel>>(emptyList()); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var current by mutableStateOf<Channel?>(null); private set

    val savedUrl get() = repo.savedUrl
    val savedName get() = repo.savedName

    init {
        if (repo.savedUrl.isNotBlank()) loadPlaylist(repo.savedName, repo.savedUrl)
    }

    fun loadPlaylist(name: String, url: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            loading = true; error = null
            try {
                channels = repo.load(url.trim())
                repo.savedUrl = url.trim()
                repo.savedName = name.trim()
                onDone()
            } catch (e: Exception) {
                error = e.message ?: "Could not load playlist"
            } finally {
                loading = false
            }
        }
    }

    fun play(channel: Channel) { current = channel }

    fun zapBy(delta: Int) {
        val cur = current ?: return
        if (channels.isEmpty()) return
        val idx = channels.indexOfFirst { it.number == cur.number }
        current = channels[(idx + delta).mod(channels.size)]
    }

    fun clearError() { error = null }
}
