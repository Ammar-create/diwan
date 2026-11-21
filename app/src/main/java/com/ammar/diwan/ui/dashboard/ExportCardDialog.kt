package com.ammar.diwan.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.data.models.Poem
import com.ammar.diwan.ui.onboarding.FluidButton
import com.ammar.diwan.ui.theme.DiwanTypography
import com.ammar.diwan.ui.theme.FoilGold
import com.ammar.diwan.ui.theme.FoilGoldSubtle
import com.ammar.diwan.ui.theme.InkMuted
import com.ammar.diwan.ui.theme.InkPrimary
import com.ammar.diwan.ui.theme.InkSecondary
import com.ammar.diwan.ui.theme.InstrumentSerif
import com.ammar.diwan.ui.theme.LapisNavy
import com.ammar.diwan.ui.theme.NotoNastaliqUrdu
import com.ammar.diwan.ui.theme.ParchmentBackground
import com.ammar.diwan.ui.theme.ParchmentBorder
import com.ammar.diwan.ui.theme.ParchmentSurface
import com.ammar.diwan.ui.theme.ParchmentSurfaceVariant

enum class CardRatio {
    STORY_9_16,
    SQUARE_1_1
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportCardDialog(
    poem: Poem,
    onDismiss: () -> Unit,
    onShareImage: () -> Unit
) {
    var ratio by remember { mutableStateOf(CardRatio.STORY_9_16) }
    var includeRoman by remember { mutableStateOf(true) }
    var includeGoldRim by remember { mutableStateOf(true) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = ParchmentSurface,
            border = BorderStroke(1.dp, ParchmentBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "طغریٰ (Export Card)",
                        fontFamily = InstrumentSerif,
                        fontSize = 20.sp,
                        color = InkPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = InkSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (ratio == CardRatio.STORY_9_16) 0.78f else 0.95f)
                        .aspectRatio(if (ratio == CardRatio.STORY_9_16) 9f / 16f else 1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ParchmentBackground)
                        .then(
                            if (includeGoldRim) Modifier.padding(1.dp) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ParchmentBackground,
                        border = if (includeGoldRim) BorderStroke(1.dp, FoilGoldSubtle) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${poem.titleUrdu} — ${poem.poetUrdu}",
                                fontFamily = NotoNastaliqUrdu,
                                fontSize = 18.sp,
                                lineHeight = 1.8.sp,
                                color = LapisNavy,
                                textAlign = TextAlign.Center
                            )

                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                poem.verses.take(if (ratio == CardRatio.SQUARE_1_1) 2 else 4).forEach { v ->
                                    Text(
                                        text = v.urdu,
                                        fontFamily = NotoNastaliqUrdu,
                                        fontSize = 16.sp,
                                        lineHeight = 2.0.sp,
                                        color = InkPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    if (includeRoman) {
                                        Text(
                                            text = v.roman,
                                            style = DiwanTypography.bodySmall,
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 11.sp,
                                            color = InkSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }

                            Text(
                                text = "دیوان · DIWAN ANTHOLOGY",
                                fontFamily = InstrumentSerif,
                                fontSize = 10.sp,
                                color = FoilGold,
                                letterSpacing = 0.05.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Ratio Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        CardRatio.STORY_9_16 to "9:16 Story",
                        CardRatio.SQUARE_1_1 to "1:1 Square"
                    ).forEach { (r, label) ->
                        val isSelected = ratio == r
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { ratio = r },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) LapisNavy else ParchmentBackground,
                            border = BorderStroke(0.5.dp, if (isSelected) LapisNavy else ParchmentBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = DiwanTypography.labelSmall,
                                    color = if (isSelected) ParchmentBackground else InkPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Layer Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { includeRoman = !includeRoman },
                        shape = RoundedCornerShape(10.dp),
                        color = if (includeRoman) ParchmentSurfaceVariant else ParchmentBackground,
                        border = BorderStroke(0.5.dp, if (includeRoman) LapisNavy else ParchmentBorder)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (includeRoman) "Roman: On" else "Roman: Off",
                                style = DiwanTypography.labelSmall,
                                color = if (includeRoman) LapisNavy else InkMuted
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { includeGoldRim = !includeGoldRim },
                        shape = RoundedCornerShape(10.dp),
                        color = if (includeGoldRim) ParchmentSurfaceVariant else ParchmentBackground,
                        border = BorderStroke(0.5.dp, if (includeGoldRim) LapisNavy else ParchmentBorder)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (includeGoldRim) "Gold Rim: On" else "Gold Rim: Off",
                                style = DiwanTypography.labelSmall,
                                color = if (includeGoldRim) LapisNavy else InkMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                FluidButton(
                    text = "Share Story Card",
                    icon = Icons.Rounded.Share,
                    onClick = {
                        onShareImage()
                        onDismiss()
                    }
                )
            }
        }
    }
}
