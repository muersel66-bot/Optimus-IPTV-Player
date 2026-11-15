package com.iptvplayerpro.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.KeyEvent as AKeyEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.iptvplayerpro.data.Channel

@Composable
fun PlayerScreen(
    channel: Channel,
    onZap: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    val focus = remember { FocusRequester() }

    DisposableEffect(Unit) { onDispose { player.release() } }

    LaunchedEffect(channel.url) {
        player.setMediaItem(MediaItem.fromUri(channel.url))
        player.prepare()
        player.playWhenReady = true
    }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Box(
        Modifier.fillMaxSize().background(Color.Black)
            .focusRequester(focus)
            .focusable()
            .onPreviewKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (e.nativeKeyEvent.keyCode) {
                    AKeyEvent.KEYCODE_CHANNEL_UP, AKeyEvent.KEYCODE_DPAD_UP -> { onZap(+1); true }
                    AKeyEvent.KEYCODE_CHANNEL_DOWN, AKeyEvent.KEYCODE_DPAD_DOWN -> { onZap(-1); true }
                    AKeyEvent.KEYCODE_DPAD_CENTER, AKeyEvent.KEYCODE_ENTER,
                    AKeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ->
                        { player.playWhenReady = !player.playWhenReady; true }
                    else -> false
                }
            },
    ) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = false } },
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            Modifier.align(Alignment.TopStart).fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text("← Back", color = Color.White) }
            Text("${channel.number} · ${channel.name}", color = Color.White)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { openExternal(context, channel.url, "com.mxtech.videoplayer.ad", "MX Player") }) {
                Text("MX Player", color = Color.White)
            }
            TextButton(onClick = { openExternal(context, channel.url, "org.videolan.vlc", "VLC") }) {
                Text("VLC", color = Color.White)
            }
        }
    }
}

/** Hands the stream to an installed external player; tells the user if it is missing. */
private fun openExternal(context: Context, url: String, pkg: String, label: String) {
    val intent = Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(url), "video/*").setPackage(pkg)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "$label is not installed. Using the built-in player.", Toast.LENGTH_LONG).show()
    }
}
