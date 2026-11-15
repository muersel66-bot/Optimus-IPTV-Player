package com.iptvplayerpro.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Pal {
    val Bg = Color(0xFF14051F)
    val Panel = Color(0xFF1F0A34)
    val Pink = Color(0xFFFF3D8B)
    val Cyan = Color(0xFF5EE6FF)
    val Orange = Color(0xFFFFB36B)
    val Muted = Color(0xFFCFC0DD)
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Pal.Pink,
            secondary = Pal.Cyan,
            background = Pal.Bg,
            surface = Pal.Panel,
        ),
        content = content,
    )
}
