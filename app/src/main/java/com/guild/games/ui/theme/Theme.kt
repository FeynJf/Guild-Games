package com.guild.games.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF0A0A0F)
val Surface = Color(0xFF14141F)
val NeonPurple = Color(0xFFB026FF)
val NeonBlue = Color(0xFF00D4FF)
val TextDim = Color(0xFF8A8AA3)
val Gold = Color(0xFFFFC83D)
val ErrorRed = Color(0xFFFF5470)

private val Scheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonBlue,
    background = Bg,
    surface = Surface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun GuildTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
