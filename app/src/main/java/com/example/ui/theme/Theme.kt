package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val T1ColorScheme = darkColorScheme(
  primary = T1Yellow,
  onPrimary = Color(0xFF101010),
  primaryContainer = Color(0xFF2A2600),
  onPrimaryContainer = T1YellowBright,
  secondary = T1Amber,
  onSecondary = Color(0xFF101010),
  secondaryContainer = Color(0xFF332000),
  onSecondaryContainer = Color(0xFFFFD180),
  tertiary = CyberCyan,
  onTertiary = Color(0xFF002026),
  background = CyberBlack,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = DarkBorder,
  outlineVariant = DarkBorderYellow,
  error = StatusRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Gaming utilities default to esports dark theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = T1ColorScheme,
    typography = Typography,
    content = content
  )
}

