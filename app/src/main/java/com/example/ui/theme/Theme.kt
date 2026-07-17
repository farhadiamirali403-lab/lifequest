package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GlowingCyan,
    secondary = AmethystPurple,
    tertiary = LegendaryGold,
    background = DarkVoid,
    surface = GlassSurface,
    onPrimary = DarkVoid,
    onSecondary = TextLight,
    onTertiary = DarkVoid,
    onBackground = TextLight,
    onSurface = TextLight,
    surfaceVariant = BorderSilver
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF720D93), // Beautiful imperial purple
    secondary = Color(0xFF9D4EDD), // Bright amethyst
    tertiary = Color(0xFFC5A030), // Classic gold
    background = Color(0xFFFAF7FF), // Pure light lavender background
    surface = Color(0xFFFFFFFF), // Crisp clean cards
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1B122C), // Contrast dark grey
    onSurface = Color(0xFF1B122C),
    surfaceVariant = Color(0xFFECE3F3)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
