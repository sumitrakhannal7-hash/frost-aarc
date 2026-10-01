package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FrostAccent,
    onPrimary = FrostBgDark,
    primaryContainer = FrostElevatedDark,
    onPrimaryContainer = IceHighlight,
    secondary = AuroraIndigo,
    onSecondary = Color.White,
    secondaryContainer = FrostElevatedDark,
    onSecondaryContainer = IceHighlight,
    tertiary = FrostSuccess,
    onTertiary = FrostBgDark,
    background = FrostBgDark,
    onBackground = TextPrimaryDark,
    surface = FrostSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = FrostElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = FrostBorderDark,
    error = FrostDanger,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepGlacier,
    onPrimary = Color.White,
    primaryContainer = IceHighlight,
    onPrimaryContainer = FrostBgDark,
    secondary = AuroraIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = FrostSuccess,
    onTertiary = Color.White,
    background = FrostBgLight,
    onBackground = TextPrimaryLight,
    surface = FrostSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = FrostElevatedLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = FrostBorderLight,
    error = FrostDanger,
    onError = Color.White
)

@Composable
fun FrostArcTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
