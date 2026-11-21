package com.ammar.diwan.ui.chat.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.ui.theme.DiwanMotion
import com.ammar.diwan.ui.theme.DiwanTheme
import com.ammar.diwan.ui.theme.tactilePress
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun ThinkingBlock(
    thoughtText: String,
    isThinking: Boolean,
    elapsedSeconds: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (thoughtText.isBlank() && !isThinking) return

    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    var manuallyToggled by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(isThinking) }

    // Auto-collapse behavior: wait 350ms after thinking completes, unless manually toggled
    LaunchedEffect(isThinking) {
        if (isThinking) {
            if (!manuallyToggled) {
                isExpanded = true
            }
        } else {
            if (!manuallyToggled) {
                delay(350L)
                isExpanded = false
            }
        }
    }

    // Active rim pulse animation while streaming
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_anims")
    val rimPulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rim_pulse_progress"
    )

    val rimPulseAlpha by animateFloatAsState(
        targetValue = if (isThinking) 1f else 0f,
        animationSpec = tween(250),
        label = "rim_pulse_alpha"
    )

    // Breathing scale for tiny ink dot (0.8f .. 1.15f)
    val dotBreathProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_breath"
    )

    // Text shimmer sweep progress while active
    val textShimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "text_shimmer_progress"
    )

    // Chevron rotation
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = DiwanMotion.SnappySpring,
        label = "chevron_rot"
    )

    val cornerRadius = 14.dp
    val shape = RoundedCornerShape(cornerRadius)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGrouped)
    ) {
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)

        // Horizontal gradient sheen border
        val rimBrush = if (rimPulseAlpha > 0.01f) {
            val center = widthPx * rimPulseProgress
            val band = widthPx * 0.4f
            Brush.horizontalGradient(
                colors = listOf(
                    colors.lapisInk.copy(alpha = 0.10f * rimPulseAlpha + (1f - rimPulseAlpha) * 0.08f),
                    colors.lapisInk.copy(alpha = 0.45f * rimPulseAlpha + (1f - rimPulseAlpha) * 0.08f),
                    colors.lapisInk.copy(alpha = 0.10f * rimPulseAlpha + (1f - rimPulseAlpha) * 0.08f)
                ),
                startX = center - band,
                endX = center + band
            )
        } else {
            SolidColor(colors.hairline)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, brush = rimBrush, shape = shape)
        ) {
            // Header Row (40dp height, tactile press)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .tactilePress(pressedScale = 0.985f) {
                        manuallyToggled = true
                        isExpanded = !isExpanded
                    }
                    .padding(horizontal = 12.dp)
            ) {
                // Left: Custom tiny pulsing ink-dot (6dp) or static spark icon (14dp)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(16.dp)
                ) {
                    AnimatedContent(
                        targetState = isThinking,
                        transitionSpec = {
                            (fadeIn(DiwanMotion.enterFade(200)) + scaleIn(initialScale = 0.8f)) togetherWith
                                (fadeOut(DiwanMotion.exitFade(150)) + scaleOut(targetScale = 0.8f))
                        },
                        label = "dot_icon_content"
                    ) { active ->
                        if (active) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .graphicsLayer {
                                        val s = 0.8f + (dotBreathProgress * 0.35f)
                                        scaleX = s
                                        scaleY = s
                                    }
                                    .clip(CircleShape)
                                    .background(colors.lapisInk)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = colors.textTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Title: Shimmer "Contemplating..." -> "Thought for 4.2s"
                val formattedTime = remember(elapsedSeconds) {
                    String.format(Locale.US, "%.1fs", elapsedSeconds)
                }

                val titleShimmerBrush = remember(textShimmerProgress, widthPx, colors) {
                    val center = widthPx * 0.5f * textShimmerProgress
                    Brush.linearGradient(
                        colors = listOf(
                            colors.textSecondary.copy(alpha = 0.55f),
                            colors.lapisInk,
                            colors.textSecondary.copy(alpha = 0.55f)
                        ),
                        start = Offset(center - 120f, 0f),
                        end = Offset(center + 120f, 0f)
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState = isThinking,
                        transitionSpec = {
                            fadeIn(DiwanMotion.enterFade(220)) togetherWith fadeOut(DiwanMotion.exitFade(160))
                        },
                        label = "title_content"
                    ) { active ->
                        if (active) {
                            Text(
                                text = "Contemplating…",
                                style = typo.caption.copy(
                                    fontWeight = FontWeight.Medium,
                                    brush = titleShimmerBrush
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            val doneText = if (elapsedSeconds > 0.05f) {
                                "Thought for $formattedTime"
                            } else {
                                "Reasoning"
                            }
                            Text(
                                text = doneText,
                                style = typo.caption.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Right: Live elapsed timer + rotating chevron
                AnimatedVisibility(
                    visible = isThinking && elapsedSeconds > 0.05f,
                    enter = fadeIn(DiwanMotion.enterFade(150)),
                    exit = fadeOut(DiwanMotion.exitFade(100))
                ) {
                    Text(
                        text = formattedTime,
                        style = TextStyle(
                            fontFamily = typo.uiBodyFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFeatureSettings = "tnum"
                        ),
                        color = colors.textTertiary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = colors.textTertiary,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(chevronRotation)
                )
            }

            // Body: Animated expansion with 180dp max height and auto-scrolling tail
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = DiwanMotion.FluidSpatial,
                    expandFrom = Alignment.Top
                ) + fadeIn(DiwanMotion.enterFade(220)),
                exit = shrinkVertically(
                    animationSpec = DiwanMotion.FluidSpatial,
                    shrinkTowards = Alignment.Top
                ) + fadeOut(DiwanMotion.exitFade(140))
            ) {
                val scrollState = rememberScrollState()

                // Auto-scroll to tail while active streaming
                LaunchedEffect(thoughtText, isThinking) {
                    if (isThinking && isExpanded) {
                        scrollState.scrollTo(scrollState.maxValue)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = thoughtText,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = colors.textSecondary.copy(alpha = 0.90f)
                        )
                    }

                    // Soft internal top/bottom fade scrims
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(colors.surfaceGrouped, Color.Transparent)
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, colors.surfaceGrouped)
                                )
                            )
                    )
                }
            }
        }
    }
}
