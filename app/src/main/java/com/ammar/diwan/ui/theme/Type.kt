package com.ammar.diwan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp
import com.ammar.diwan.R

// Urdu Fonts
val NotoNastaliqUrdu = FontFamily(
    Font(R.font.noto_nastaliq_urdu, FontWeight.Normal)
)

val JameelNooriNastaleeq = FontFamily(
    Font(R.font.jameel_noori_nastaleeq, FontWeight.Normal)
)

val Gulzar = FontFamily(
    Font(R.font.gulzar_regular, FontWeight.Normal)
)

// English / Latin Display Fonts
val InstrumentSerif = FontFamily(
    Font(R.font.instrument_serif_regular, FontWeight.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic)
)

val GoogleSansFlex = FontFamily(
    Font(R.font.google_sans_flex, FontWeight.Normal)
)

val ComicNeue = FontFamily(
    Font(R.font.comic_neue_regular, FontWeight.Normal)
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_bold, FontWeight.Bold)
)

fun getUrduFont(key: String): FontFamily = when (key) {
    "jameel_noori" -> JameelNooriNastaleeq
    "gulzar" -> Gulzar
    else -> NotoNastaliqUrdu
}

fun getEnglishFont(key: String): FontFamily = when (key) {
    "google_sans" -> GoogleSansFlex
    "comic" -> ComicNeue
    "inter" -> Inter
    else -> InstrumentSerif
}

fun createDiwanTypography(
    englishFont: FontFamily = InstrumentSerif,
    urduFont: FontFamily = NotoNastaliqUrdu
) = Typography(
    displayLarge = TextStyle(
        fontFamily = englishFont,
        fontWeight = FontWeight.Normal,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = englishFont,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = urduFont,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = urduFont,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 46.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = urduFont,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 48.sp,
        lineBreak = LineBreak.Paragraph,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = englishFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)

val DiwanTypography: Typography = createDiwanTypography()
