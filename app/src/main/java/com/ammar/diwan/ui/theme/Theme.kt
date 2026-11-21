package com.ammar.diwan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

val LocalUrduFont = staticCompositionLocalOf<FontFamily> { NotoNastaliqUrdu }
val LocalEnglishFont = staticCompositionLocalOf<FontFamily> { InstrumentSerif }

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
    primary = Color(0xFFE5C158),           // Illuminated Gold
    onPrimary = Color(0xFF0D1117),
    primaryContainer = Color(0xFF161B22),
    onPrimaryContainer = Color(0xFFF2EFE9),
    secondary = Color(0xFF58A6FF),         // Celestial Lapis
    onSecondary = Color(0xFF0D1117),
    background = Color(0xFF0B0E14),        // Deep Nocturnal Ink
    onBackground = Color(0xFFF2EFE9),
    surface = Color(0xFF151B23),           // Nocturnal Surface Card
    onSurface = Color(0xFFF2EFE9),
    surfaceVariant = Color(0xFF1C2430),
    onSurfaceVariant = Color(0xFF9EA6B2),
    outline = Color(0xFF283442)
)

@Composable
fun DiwanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    englishFontKey: String = "instrument",
    urduFontKey: String = "noto_nastaliq",
    content: @Composable () -> Unit
) {
    val urduFont = getUrduFont(urduFontKey)
    val englishFont = getEnglishFont(englishFontKey)
    val mode = if (darkTheme) DiwanThemeMode.DARK else DiwanThemeMode.LIGHT

    val diwanTypography = remember(urduFont, englishFont) {
        DiwanTypography(
            urduFamily = urduFont,
            latinDisplayFamily = englishFont,
            uiBodyFamily = Inter
        )
    }
    val materialTypography = remember(urduFont, englishFont) {
        createMaterialTypography(englishFont, urduFont)
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalUrduFont provides urduFont,
        LocalEnglishFont provides englishFont
    ) {
        ProvideDiwanTheme(
            themeMode = mode,
            typography = diwanTypography
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = materialTypography,
                content = content
            )
        }
    }
}
