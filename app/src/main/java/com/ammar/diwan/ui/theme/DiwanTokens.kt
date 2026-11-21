package com.ammar.diwan.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class DiwanColors(
    val isDark: Boolean,
    // 4-Layer Elevation Architecture
    val canvas: Color,              // L0: Root background
    val surfaceGrouped: Color,      // L1: Settings cards, thinking block, verse wash
    val surfaceElevated: Color,     // L1.5: Inner cards / active segmented thumb
    val surfaceChrome: Color,       // L2: Floating chat capsule, top bar pills
    val surfaceArtifact: Color,     // L3: "Add to Folio" card, modals, onboarding preview
    val trackRecessed: Color,       // Recessed segmented control / input wells

    // Borders & Hairlines (depth without muddy black drop shadows)
    val hairline: Color,            // 0.5dp subtle internal divider
    val specularBorder: Color,      // Top-lit crisp edge for floating chrome
    val artifactRim: Color,         // Subtle lapis/gold manuscript frame

    // Ink & Semantic Accents
    val lapisInk: Color,            // Primary brand accent
    val onLapisInk: Color,          // Text/icon on lapis fill
    val lapisSoftTint: Color,       // Soft accent badge fill
    val goldAccent: Color,          // Saved Folio, gilding, celebration sheen
    val goldSoftTint: Color,        // Saved state button fill
    val vermilion: Color,           // Destructive / Stop streaming / Error
    val emerald: Color,             // Connected / Verified handshake

    // Typography Hierarchy
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,

    // User Bubble
    val userBubbleFill: Color,
    val userBubbleText: Color
)

val ParchmentLightColors = DiwanColors(
    isDark = false,
    canvas = Color(0xFFFDFBF7),
    surfaceGrouped = Color(0xFFF5F1E8),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceChrome = Color(0xF0FFFFFF),
    surfaceArtifact = Color(0xFFFFFDF9),
    trackRecessed = Color(0xFFECE6DA),

    hairline = Color(0x141A1D20),       // 8% Charcoal
    specularBorder = Color(0xCCFFFFFF), // 80% White
    artifactRim = Color(0x240D3B66),    // 14% Lapis

    lapisInk = Color(0xFF0D3B66),
    onLapisInk = Color(0xFFFDFBF7),
    lapisSoftTint = Color(0x140D3B66),
    goldAccent = Color(0xFFB38211),
    goldSoftTint = Color(0x1FB38211),
    vermilion = Color(0xFFB43C3C),
    emerald = Color(0xFF1F7A53),

    textPrimary = Color(0xFF1A1D20),
    textSecondary = Color(0xFF63686E),
    textTertiary = Color(0xFF93989E),

    userBubbleFill = Color(0xFF0D3B66),
    userBubbleText = Color(0xFFFDFBF7)
)

val DarkNocturneColors = DiwanColors(
    isDark = true,
    canvas = Color(0xFF0E1116),
    surfaceGrouped = Color(0xFF161B22),
    surfaceElevated = Color(0xFF1E252F),
    surfaceChrome = Color(0xEB1C222B),
    surfaceArtifact = Color(0xFF1D242E),
    trackRecessed = Color(0xFF10141A),

    hairline = Color(0x14FFFFFF),       // 8% White
    specularBorder = Color(0x1FFFFFFF), // 12% White
    artifactRim = Color(0x33D4AF37),    // 20% Antique Gold

    lapisInk = Color(0xFF7FB3E8),
    onLapisInk = Color(0xFF0B141E),
    lapisSoftTint = Color(0x1F7FB3E8),
    goldAccent = Color(0xFFE0B854),
    goldSoftTint = Color(0x24E0B854),
    vermilion = Color(0xFFE06C6C),
    emerald = Color(0xFF4FB885),

    textPrimary = Color(0xFFEDEAE4),
    textSecondary = Color(0xFF8C949E),
    textTertiary = Color(0xFF5D646E),

    userBubbleFill = Color(0xFF1D3B5E),
    userBubbleText = Color(0xFFF2F6FA)
)

@Immutable
object DiwanDimens {
    val ScreenPaddingH: Dp = 16.dp
    val CardRadius: Dp = 16.dp
    val ArtifactRadius: Dp = 20.dp
    val CapsuleRadius: Dp = 26.dp
    val HairlineThickness: Dp = 0.5.dp
    val RowMinHeight: Dp = 52.dp

    // Tablet (Lenovo Tab M9) proportions: never stretch forms or text across full 9" width
    val MaxChatColumnWidth: Dp = 680.dp
    val MaxSettingsColumnWidth: Dp = 580.dp
    val MaxOnboardingWidth: Dp = 480.dp
}

@Immutable
data class DiwanTypography(
    val urduFamily: FontFamily = FontFamily.Default,
    val latinDisplayFamily: FontFamily = FontFamily.Serif,
    val uiBodyFamily: FontFamily = FontFamily.SansSerif
) {
    /**
     * Mandatory Nastaliq text style factory.
     * Enforces >= 2.0x lineHeight, includeFontPadding=true, and RTL direction to eliminate clipping/overlap.
     */
    fun nastaliq(
        size: TextUnit = 20.sp,
        lineHeightMultiplier: Float = 2.05f,
        weight: FontWeight = FontWeight.Normal,
        color: Color = Color.Unspecified
    ): TextStyle = TextStyle(
        fontFamily = urduFamily,
        fontSize = size,
        lineHeight = size * lineHeightMultiplier,
        fontWeight = weight,
        color = color,
        textDirection = TextDirection.Rtl,
        platformStyle = PlatformTextStyle(includeFontPadding = true)
    )

    val sectionHeader: TextStyle
        get() = TextStyle(
            fontFamily = uiBodyFamily,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.25.sp
        )

    val editorialTitle: TextStyle
        get() = TextStyle(
            fontFamily = latinDisplayFamily,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.3).sp
        )

    val body: TextStyle
        get() = TextStyle(
            fontFamily = uiBodyFamily,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Normal
        )

    val caption: TextStyle
        get() = TextStyle(
            fontFamily = uiBodyFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal
        )

    val monoTechnical: TextStyle
        get() = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal
        )

    val tabularMetric: TextStyle
        get() = TextStyle(
            fontFamily = uiBodyFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFeatureSettings = "tnum"
        )

    companion object {
        val Default = DiwanTypography()
        val bodyMedium: TextStyle get() = Default.body
        val labelSmall: TextStyle get() = Default.caption
    }
}

val LocalDiwanColors = staticCompositionLocalOf { ParchmentLightColors }
val LocalDiwanTypography = staticCompositionLocalOf { DiwanTypography() }

object DiwanTheme {
    val colors: DiwanColors
        @Composable get() = LocalDiwanColors.current
    val typography: DiwanTypography
        @Composable get() = LocalDiwanTypography.current
}

enum class DiwanThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun ProvideDiwanTheme(
    themeMode: DiwanThemeMode = DiwanThemeMode.SYSTEM,
    typography: DiwanTypography = DiwanTypography(),
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        DiwanThemeMode.SYSTEM -> isSystemInDarkTheme()
        DiwanThemeMode.LIGHT -> false
        DiwanThemeMode.DARK -> true
    }
    val target = if (dark) DarkNocturneColors else ParchmentLightColors
    val spec = tween<Color>(durationMillis = 320, easing = DiwanMotion.FastEaseOut)

    // Smooth canvas & surface crossfade when user toggles Parchment <-> Nocturne
    val animatedColors = target.copy(
        canvas = animateColorAsState(target.canvas, spec, label = "canvas").value,
        surfaceGrouped = animateColorAsState(target.surfaceGrouped, spec, label = "surfaceGrouped").value,
        surfaceElevated = animateColorAsState(target.surfaceElevated, spec, label = "surfaceElevated").value,
        surfaceChrome = animateColorAsState(target.surfaceChrome, spec, label = "surfaceChrome").value,
        surfaceArtifact = animateColorAsState(target.surfaceArtifact, spec, label = "surfaceArtifact").value,
        trackRecessed = animateColorAsState(target.trackRecessed, spec, label = "trackRecessed").value,
        hairline = animateColorAsState(target.hairline, spec, label = "hairline").value,
        specularBorder = animateColorAsState(target.specularBorder, spec, label = "specularBorder").value,
        artifactRim = animateColorAsState(target.artifactRim, spec, label = "artifactRim").value,
        lapisInk = animateColorAsState(target.lapisInk, spec, label = "lapisInk").value,
        onLapisInk = animateColorAsState(target.onLapisInk, spec, label = "onLapisInk").value,
        lapisSoftTint = animateColorAsState(target.lapisSoftTint, spec, label = "lapisSoftTint").value,
        goldAccent = animateColorAsState(target.goldAccent, spec, label = "goldAccent").value,
        goldSoftTint = animateColorAsState(target.goldSoftTint, spec, label = "goldSoftTint").value,
        textPrimary = animateColorAsState(target.textPrimary, spec, label = "textPrimary").value,
        textSecondary = animateColorAsState(target.textSecondary, spec, label = "textSecondary").value,
        textTertiary = animateColorAsState(target.textTertiary, spec, label = "textTertiary").value,
        userBubbleFill = animateColorAsState(target.userBubbleFill, spec, label = "userBubbleFill").value,
        userBubbleText = animateColorAsState(target.userBubbleText, spec, label = "userBubbleText").value
    )

    CompositionLocalProvider(
        LocalDiwanColors provides animatedColors,
        LocalDiwanTypography provides typography,
        content = content
    )
}

@Composable
fun ProvideDiwanTheme(
    darkTheme: Boolean,
    typography: DiwanTypography = DiwanTypography(),
    content: @Composable () -> Unit
) = ProvideDiwanTheme(
    themeMode = if (darkTheme) DiwanThemeMode.DARK else DiwanThemeMode.LIGHT,
    typography = typography,
    content = content
)
