package com.ammar.diwan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LapisNavy,
    onPrimary = ParchmentBackground,
    primaryContainer = ParchmentSurfaceVariant,
    onPrimaryContainer = LapisNavyDark,
    secondary = FoilGold,
    onSecondary = InkPrimary,
    background = ParchmentBackground,
    onBackground = InkPrimary,
    surface = ParchmentSurface,
    onSurface = InkPrimary,
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = ParchmentBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = FoilGold,
    onPrimary = NocturneBackground,
    primaryContainer = NocturneSurface,
    onPrimaryContainer = NocturneTextPrimary,
    secondary = LapisNavyLight,
    onSecondary = NocturneTextPrimary,
    background = NocturneBackground,
    onBackground = NocturneTextPrimary,
    surface = NocturneSurface,
    onSurface = NocturneTextPrimary,
    surfaceVariant = NocturneSurface,
    onSurfaceVariant = NocturneTextSecondary,
    outline = NocturneBorder
)

@Composable
fun DiwanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DiwanTypography,
        content = content
    )
}
