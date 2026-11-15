package com.guild.games.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF07070C)
val BgTop = Color(0xFF16082E)
val Panel = Color(0xFF12121C)
val PanelHigh = Color(0xFF1B1B2B)
val Stroke = Color(0xFF2E2E48)
val NeonPurple = Color(0xFFB026FF)
val NeonBlue = Color(0xFF00D4FF)
val ElectricBlue = Color(0xFF2F6BFF)
val TextDim = Color(0xFF8A8AA3)
val Gold = Color(0xFFFFC83D)
val ErrorRed = Color(0xFFFF5470)
val Success = Color(0xFF2EE6A6)

val BgBrush = Brush.verticalGradient(listOf(BgTop, Bg, Bg))
val ButtonBrush = Brush.horizontalGradient(listOf(Color(0xFFA020F0), ElectricBlue))
val NeonBrush = Brush.horizontalGradient(listOf(NeonPurple, NeonBlue))

private val Scheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonBlue,
    background = Bg,
    surface = Panel,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun GuildTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
