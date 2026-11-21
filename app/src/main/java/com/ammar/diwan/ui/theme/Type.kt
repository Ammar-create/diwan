package com.ammar.diwan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ammar.diwan.R

val NotoNastaliqUrdu = FontFamily(
    Font(R.font.noto_nastaliq_urdu, FontWeight.Normal)
)

val InstrumentSerif = FontFamily(
    Font(R.font.instrument_serif_regular, FontWeight.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic)
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_bold, FontWeight.Bold)
)

val DiwanTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InstrumentSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.02).em
    ),
    displayMedium = TextStyle(
        fontFamily = InstrumentSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.01).em
    ),
    headlineLarge = TextStyle(
        fontFamily = NotoNastaliqUrdu,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 2.4.em,
        letterSpacing = 0.em
    ),
    headlineMedium = TextStyle(
        fontFamily = NotoNastaliqUrdu,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 2.3.em,
        letterSpacing = 0.em
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoNastaliqUrdu,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 2.2.em,
        lineBreak = LineBreak.Paragraph
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.em
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.01.em
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.01.em
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.02.em
    )
)
