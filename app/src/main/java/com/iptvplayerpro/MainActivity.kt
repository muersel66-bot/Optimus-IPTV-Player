package com.iptvplayerpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.iptvplayerpro.ui.*

private enum class Screen { Home, Add, Channels, Player }

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                var screen by remember { mutableStateOf(Screen.Home) }

                BackHandler(enabled = screen != Screen.Home) {
                    screen = when (screen) {
                        Screen.Player -> Screen.Channels
                        else -> Screen.Home
                    }
                }

                when (screen) {
                    Screen.Home -> HomeScreen(
                        channelCount = vm.channels.size,
                        playlistName = vm.savedName,
                        onLive = { screen = if (vm.channels.isEmpty()) Screen.Add else Screen.Channels },
                        onAdd = { vm.clearError(); screen = Screen.Add },
                    )
                    Screen.Add -> AddPlaylistScreen(
                        initialName = vm.savedName,
                        initialUrl = vm.savedUrl,
                        loading = vm.loading,
                        error = vm.error,
                        onSubmit = { name, url -> vm.loadPlaylist(name, url) { screen = Screen.Channels } },
                        onBack = { screen = Screen.Home },
                    )
                    Screen.Channels -> ChannelListScreen(
                        channels = vm.channels,
                        onPlay = { vm.play(it); screen = Screen.Player },
                        onBack = { screen = Screen.Home },
                    )
                    Screen.Player -> vm.current?.let {
                        PlayerScreen(it, onZap = vm::zapBy, onBack = { screen = Screen.Channels })
                    } ?: run { screen = Screen.Channels }
                }
            }
        }
    }
}
