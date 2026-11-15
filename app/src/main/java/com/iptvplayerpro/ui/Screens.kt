package com.iptvplayerpro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvplayerpro.data.Channel

/** Row/button that shows the pink "focused" look from the design when selected with a remote. */
@Composable
fun Modifier.tvFocus(shape: RoundedCornerShape = RoundedCornerShape(14.dp), onClick: () -> Unit): Modifier {
    var focused by remember { mutableStateOf(false) }
    return this
        .onFocusChanged { focused = it.isFocused }
        .clip(shape)
        .background(if (focused) Pal.Pink.copy(alpha = 0.85f) else Color.Transparent)
        .border(2.dp, if (focused) Color.White else Color.Transparent, shape)
        .clickable(onClick = onClick)
        .focusable()
}

@Composable
fun HomeScreen(
    channelCount: Int,
    playlistName: String,
    onLive: () -> Unit,
    onAdd: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(Pal.Bg).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text("IPTV Player Pro", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text("What are we watching tonight?", fontSize = 22.sp, color = Pal.Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            HomeCard("Live TV", if (channelCount > 0) "$channelCount channels" else "No playlist yet", onLive)
            HomeCard("Add Playlist", "M3U link", onAdd)
        }
        if (playlistName.isNotBlank()) Text("Playlist: $playlistName", color = Pal.Muted, fontSize = 13.sp)
    }
}

@Composable
private fun HomeCard(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        Modifier.width(220.dp).height(140.dp)
            .background(Pal.Panel, RoundedCornerShape(20.dp))
            .tvFocus(RoundedCornerShape(20.dp), onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(subtitle, fontSize = 13.sp, color = Pal.Muted)
    }
}

@Composable
fun AddPlaylistScreen(
    initialName: String,
    initialUrl: String,
    loading: Boolean,
    error: String?,
    onSubmit: (name: String, url: String) -> Unit,
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var url by remember { mutableStateOf(initialUrl) }
    Column(
        Modifier.fillMaxSize().background(Pal.Bg).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Add Playlist", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Playlist name") }, singleLine = true,
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
        )
        OutlinedTextField(
            value = url, onValueChange = { url = it },
            label = { Text("M3U link (URL)") }, singleLine = true,
            placeholder = { Text("https://…/playlist.m3u") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
        )
        Text("Get your .m3u or .m3u8 link from your IPTV provider.", color = Pal.Muted, fontSize = 13.sp)
        if (error != null) Text(error, color = Color(0xFFFF8A8A))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { onSubmit(name, url) }, enabled = !loading && url.isNotBlank()) {
                Text("Save & Load")
            }
            OutlinedButton(onClick = onBack) { Text("Cancel") }
            if (loading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
        }
    }
}

@Composable
fun ChannelListScreen(
    channels: List<Channel>,
    onPlay: (Channel) -> Unit,
    onBack: () -> Unit,
) {
    val groups = remember(channels) { listOf("All") + channels.map { it.group }.distinct() }
    var group by remember { mutableStateOf("All") }
    val shown = remember(channels, group) { if (group == "All") channels else channels.filter { it.group == group } }

    Column(Modifier.fillMaxSize().background(Pal.Bg).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Live TV", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(groups) { g ->
                val sel = g == group
                Text(
                    g,
                    color = if (sel) Pal.Bg else Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .tvFocus(RoundedCornerShape(18.dp)) { group = g }
                        .background(if (sel) Color.White else Color.Transparent, RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
        if (channels.isEmpty()) {
            Text("No channels yet. Add a playlist first.", color = Pal.Muted)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(shown, key = { it.number }) { ch ->
                Row(
                    Modifier.fillMaxWidth().tvFocus { onPlay(ch) }.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text("${ch.number}", color = Pal.Muted, modifier = Modifier.width(44.dp), fontSize = 13.sp)
                    Text(ch.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.weight(1f))
                    Text(ch.group, color = Pal.Muted, fontSize = 12.sp)
                }
            }
        }
    }
}
