package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// T1 Esports Signature Colors (Vibrant Yellow & Deep Carbon Cyber Dark)
val T1Yellow = Color(0xFFFFE500)
val T1YellowBright = Color(0xFFFFF155)
val T1YellowDark = Color(0xFFC7B200)
val T1Amber = Color(0xFFFF9500)

val CyberBlack = Color(0xFF090A0F)
val DarkSurface = Color(0xFF10121A)
val DarkSurfaceElevated = Color(0xFF161924)
val DarkSurfaceCard = Color(0xFF1C202F)
val DarkBorder = Color(0xFF262C3F)
val DarkBorderYellow = Color(0x66FFE500)

val CyberCyan = Color(0xFF00E5FF)
val CyberElectricBlue = Color(0xFF00B0FF)
val NeonPurple = Color(0xFF7C4DFF)
val NeonPink = Color(0xFFFF2A6D)
val StatusGreen = Color(0xFF00E676)
val StatusRed = Color(0xFFFF3366)
val TextPrimary = Color(0xFFF0F3FA)
val TextSecondary = Color(0xFF98A1B8)
val TextMuted = Color(0xFF646E85)

// 100000x UI Gradients
val T1GoldGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFFE500), Color(0xFFFF9500))
)

val CyberCyanGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00E5FF), Color(0xFF00B0FF))
)

val NeonFireGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF2A6D), Color(0xFFFF7700), Color(0xFFFFE500))
)

val DarkCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF191D2B), Color(0xFF10131D))
)

val CardBorderGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFE500).copy(alpha = 0.5f),
        Color(0xFF00E5FF).copy(alpha = 0.2f),
        Color(0xFF262C3F).copy(alpha = 0.6f)
    )
)


