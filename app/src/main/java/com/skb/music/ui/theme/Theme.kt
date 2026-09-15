package com.skb.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Dark = darkColorScheme(
    primary = Color(0xFF1DB954),
    background = Color(0xFF000000),
    surface = Color(0xFF0E0E0E),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val Light = lightColorScheme(
    primary = Color(0xFF1DB954),
    background = Color(0xFFFAFAFA),
    surface = Color.White
)

@Composable
fun SkbMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) Dark else Light,
        content = content
    )
}
