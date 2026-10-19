package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = ChurchLightGold,
    onPrimary = ChurchDarkBlue,
    primaryContainer = ChurchMediumBlue,
    onPrimaryContainer = ChurchPaleGold,
    secondary = ChurchGold,
    onSecondary = Color.White,
    background = ChurchDarkBlue,
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF132338),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E334D),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334E68)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ChurchDeepBlue,
    onPrimary = Color.White,
    primaryContainer = ChurchBlueBg,
    onPrimaryContainer = ChurchDeepBlue,
    secondary = ChurchGold,
    onSecondary = Color.White,
    secondaryContainer = ChurchAmberBg,
    onSecondaryContainer = ChurchDarkGold,
    background = ChurchIvory,
    onBackground = ChurchTextPrimary,
    surface = ChurchSurface,
    onSurface = ChurchTextPrimary,
    surfaceVariant = ChurchCardBg,
    onSurfaceVariant = ChurchTextSecondary,
    outline = ChurchBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
