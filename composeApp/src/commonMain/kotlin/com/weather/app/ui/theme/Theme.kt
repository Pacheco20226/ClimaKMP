package com.weather.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy        = Color(0xFF070B18)
val NavyMid     = Color(0xFF0D1527)
val Card        = Color(0xFF131D33)
val CardBorder  = Color(0x22FFFFFF)
// Act 2.1 — Color principal cambiado de Cyan (0xFF4DD9E8) a Violeta (0xFFB983FF)
val Cyan        = Color(0xFFB983FF)
val Blue        = Color(0xFF3B82F6)
val Amber       = Color(0xFFF59E0B)
val TextPri     = Color(0xFFF0F4FF)
val TextSec     = Color(0xFF8BA0C4)
val TextMuted   = Color(0xFF4A6080)
val RainBlue    = Color(0xFF60A5FA)

private val Colors = darkColorScheme(
    primary          = Cyan,
    onPrimary        = Navy,
    background       = Navy,
    onBackground     = TextPri,
    surface          = Card,
    onSurface        = TextPri,
    onSurfaceVariant = TextSec,
    outline          = TextMuted,
    error            = Color(0xFFEF4444)
)

@Composable
fun WeatherTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
