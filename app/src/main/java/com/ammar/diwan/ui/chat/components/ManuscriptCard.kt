package com.ammar.diwan.ui.chat.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.data.models.Poem
import com.ammar.diwan.ui.theme.DiwanMotion
import com.ammar.diwan.ui.theme.DiwanTheme
import com.ammar.diwan.ui.theme.gildedSweep
import com.ammar.diwan.ui.theme.tactilePress
import kotlinx.coroutines.delay

@Composable
fun ManuscriptCard(
    poem: Poem,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false,
    onSaveToFolio: (Poem) -> Unit = {}
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    var localSaved by remember(isSaved) { mutableStateOf(isSaved) }
    var sweepTriggerKey by remember { mutableIntStateOf(0) }
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(1500L)
            isCopied = false
        }
    }

    val outerShape = RoundedCornerShape(20.dp)
    val innerShape = RoundedCornerShape(16.dp)

    // Outer double-frame L3 Manuscript surface
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = outerShape,
                ambientColor = colors.lapisInk.copy(alpha = 0.10f),
                spotColor = colors.lapisInk.copy(alpha = 0.14f)
            )
            .clip(outerShape)
            .background(colors.surfaceArtifact)
            .border(BorderStroke(1.dp, colors.artifactRim), outerShape)
            .gildedSweep(triggerKey = sweepTriggerKey)
            .padding(4.dp)
    ) {
        // Inner thin gold frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(innerShape)
                .border(
                    BorderStroke(0.75.dp, colors.goldAccent.copy(alpha = 0.22f)),
                    innerShape
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Metadata Bar: Poet name + Meter / Form tag pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val poetDisplay = when {
                        poem.poetEnglish.isNotBlank() -> poem.poetEnglish
                        poem.poetUrdu.isNotBlank() -> poem.poetUrdu
                        else -> "Original Calligraphy"
                    }

                    Text(
                        text = poetDisplay.uppercase(),
                        style = typo.caption.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.1.sp
                        ),
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val tagDisplay = poem.meter ?: poem.tags.firstOrNull() ?: "Ghazal · Hazaj"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.lapisSoftTint)
                            .border(0.5.dp, colors.lapisInk.copy(alpha = 0.18f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tagDisplay,
                            style = typo.caption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.lapisInk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Poem Body: Preview couplets in authentic Nastaliq (20sp, 42sp line height, RTL)
                val previewVerses = poem.verses.take(3)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    previewVerses.forEach { verse ->
                        if (verse.urdu.isNotBlank()) {
                            Text(
                                text = verse.urdu,
                                style = typo.nastaliq(
                                    size = 20.sp,
                                    lineHeightMultiplier = 2.1f,
                                    color = colors.textPrimary
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Footer Divider (0.5dp hairline)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(colors.hairline)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar: Secondary "Copy Urdu" + Primary "Save to My Folio"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Secondary Ghost Button: Copy Urdu
                    val allUrduText = poem.verses.joinToString("\n") { it.urdu }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .tactilePress(pressedScale = 0.94f) {
                                clipboardManager.setText(AnnotatedString(allUrduText))
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isCopied = true
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        AnimatedContent(
                            targetState = isCopied,
                            transitionSpec = {
                                (fadeIn(DiwanMotion.enterFade(160)) + scaleIn(0.9f)) togetherWith
                                    (fadeOut(DiwanMotion.exitFade(120)) + scaleOut(0.9f))
                            },
                            label = "copy_state"
                        ) { copied ->
                            if (copied) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = colors.emerald,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Copied",
                                        style = typo.caption.copy(fontWeight = FontWeight.Medium),
                                        color = colors.emerald
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Copy Urdu",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Copy Urdu",
                                        style = typo.caption.copy(fontWeight = FontWeight.Medium),
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Primary Button: "Save to My Folio" with Gilded Sweep Micro-Interaction
                    val buttonFill by animateColorAsState(
                        targetValue = if (localSaved) colors.goldSoftTint else colors.lapisInk,
                        animationSpec = tween(240, easing = DiwanMotion.FastEaseOut),
                        label = "folio_btn_fill"
                    )
                    val buttonContentColor by animateColorAsState(
                        targetValue = if (localSaved) colors.goldAccent else colors.onLapisInk,
                        animationSpec = tween(240, easing = DiwanMotion.FastEaseOut),
                        label = "folio_btn_content"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .animateContentSize(DiwanMotion.SnappySpring)
                            .clip(RoundedCornerShape(50))
                            .background(buttonFill)
                            .then(
                                if (localSaved) {
                                    Modifier.border(
                                        1.dp,
                                        colors.goldAccent.copy(alpha = 0.40f),
                                        RoundedCornerShape(50)
                                    )
                                } else Modifier
                            )
                            .tactilePress(
                                pressedScale = 0.95f,
                                onClick = {
                                    if (!localSaved) {
                                        localSaved = true
                                        sweepTriggerKey++
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSaveToFolio(poem)
                                    }
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        AnimatedContent(
                            targetState = localSaved,
                            transitionSpec = {
                                (fadeIn(DiwanMotion.enterFade(180)) + scaleIn(0.92f)) togetherWith
                                    (fadeOut(DiwanMotion.exitFade(120)) + scaleOut(0.92f))
                            },
                            label = "save_btn_state"
                        ) { saved ->
                            if (saved) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bookmark,
                                        contentDescription = null,
                                        tint = buttonContentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Saved to Folio",
                                        style = typo.caption.copy(fontWeight = FontWeight.SemiBold),
                                        color = buttonContentColor
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.BookmarkBorder,
                                        contentDescription = null,
                                        tint = buttonContentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Save to My Folio",
                                        style = typo.caption.copy(fontWeight = FontWeight.SemiBold),
                                        color = buttonContentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
