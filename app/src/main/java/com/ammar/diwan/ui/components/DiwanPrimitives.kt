package com.ammar.diwan.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.ui.theme.DiwanDimens
import com.ammar.diwan.ui.theme.DiwanMotion
import com.ammar.diwan.ui.theme.DiwanTheme
import com.ammar.diwan.ui.theme.inkBreath
import com.ammar.diwan.ui.theme.tactilePress

enum class DiwanLayer { L1_GROUPED, L2_CHROME, L3_ARTIFACT }

/**
 * Core tonal surface with 0.5dp specular hairline and zero muddy grey drop shadows.
 */
@Composable
fun DiwanSurface(
    modifier: Modifier = Modifier,
    layer: DiwanLayer = DiwanLayer.L1_GROUPED,
    shape: Shape = RoundedCornerShape(DiwanDimens.CardRadius),
    backgroundColor: Color = Color.Unspecified,
    border: BorderStroke? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = DiwanTheme.colors
    val fill = if (backgroundColor != Color.Unspecified) backgroundColor else when (layer) {
        DiwanLayer.L1_GROUPED -> colors.surfaceGrouped
        DiwanLayer.L2_CHROME -> colors.surfaceChrome
        DiwanLayer.L3_ARTIFACT -> colors.surfaceArtifact
    }
    val borderColor = when (layer) {
        DiwanLayer.L1_GROUPED -> colors.hairline
        DiwanLayer.L2_CHROME -> colors.specularBorder
        DiwanLayer.L3_ARTIFACT -> colors.artifactRim
    }
    val defaultBorderWidth = if (layer == DiwanLayer.L3_ARTIFACT) 1.dp else DiwanDimens.HairlineThickness
    val effectiveBorder = border ?: BorderStroke(defaultBorderWidth, borderColor)
    val shadowDp = when (layer) {
        DiwanLayer.L1_GROUPED -> 0.dp
        DiwanLayer.L2_CHROME -> 6.dp
        DiwanLayer.L3_ARTIFACT -> 10.dp
    }

    Box(
        modifier = modifier
            .then(
                if (shadowDp > 0.dp) {
                    Modifier.shadow(
                        elevation = shadowDp,
                        shape = shape,
                        ambientColor = colors.lapisInk.copy(alpha = 0.12f),
                        spotColor = colors.lapisInk.copy(alpha = 0.16f)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(fill)
            .border(effectiveBorder, shape = shape),
        content = content
    )
}

/**
 * Crisp 0.5dp horizontal hairline separator (Apple grouped-list style).
 */
@Composable
fun DiwanHairline(
    modifier: Modifier = Modifier,
    insetStart: Dp = 0.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = insetStart)
            .height(DiwanDimens.HairlineThickness)
            .background(DiwanTheme.colors.hairline)
    )
}

/**
 * 60fps replacement for live Modifier.blur() behind floating Top Bars and Chat Inputs.
 * Fades scrolling content smoothly into the canvas.
 */
@Composable
fun DiwanEdgeScrim(
    modifier: Modifier = Modifier,
    fromTop: Boolean = true
) {
    val canvas = DiwanTheme.colors.canvas
    val stops = if (fromTop) {
        listOf(canvas.copy(alpha = 0.96f), canvas.copy(alpha = 0.82f), Color.Transparent)
    } else {
        listOf(Color.Transparent, canvas.copy(alpha = 0.82f), canvas.copy(alpha = 0.96f))
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(stops))
    )
}

/**
 * Apple-style grouped section container with subtle uppercase header.
 */
@Composable
fun DiwanGroupedSection(
    title: String,
    modifier: Modifier = Modifier,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = typo.sectionHeader,
            color = colors.textSecondary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        DiwanSurface(
            layer = DiwanLayer.L1_GROUPED,
            shape = RoundedCornerShape(DiwanDimens.CardRadius),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
        if (footer != null) {
            Text(
                text = footer,
                style = typo.caption,
                color = colors.textTertiary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp)
            )
        }
    }
}

/**
 * Fluid spring-sliding segmented control for Theme, Urdu Font, and Latin Font switchers.
 */
@Composable
fun DiwanSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty()) return
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography
    val safeIndex = selectedIndex.coerceIn(0, options.lastIndex)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.trackRecessed)
            .padding(3.dp)
    ) {
        val segmentWidth = maxWidth / options.size
        val thumbOffset by animateDpAsState(
            targetValue = segmentWidth * safeIndex,
            animationSpec = DiwanMotion.SnappyDpSpring,
            label = "segment_thumb_offset"
        )

        // Sliding Elevated Thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(9.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.10f)
                )
                .clip(RoundedCornerShape(9.dp))
                .background(colors.surfaceElevated)
                .border(0.5.dp, colors.hairline, RoundedCornerShape(9.dp))
        )

        // Labels Row
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, label ->
                val isSelected = index == safeIndex
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) colors.textPrimary else colors.textSecondary,
                    animationSpec = DiwanMotion.enterFade(160),
                    label = "segment_label_color"
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .tactilePress(pressedScale = 0.97f) { onOptionSelected(index) }
                ) {
                    Text(
                        text = label,
                        style = typo.caption.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Borderless Apple-style key-value monospace input row (replaces clunky OutlinedTextField).
 */
@Composable
fun DiwanKeyValueInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isSecret: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingAction: (@Composable () -> Unit)? = null
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography
    var revealed by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = DiwanDimens.RowMinHeight)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            modifier = Modifier.width(88.dp)
        )
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.weight(1f)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = typo.monoTechnical,
                    color = colors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = typo.monoTechnical.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.lapisInk),
                visualTransformation = if (isSecret && !revealed) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (isSecret && value.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (revealed) "Hide" else "Show",
                style = typo.caption.copy(fontWeight = FontWeight.Medium),
                color = colors.lapisInk,
                modifier = Modifier
                    .tactilePress(pressedScale = 0.92f) { revealed = !revealed }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
        if (trailingAction != null) {
            Spacer(Modifier.width(6.dp))
            trailingAction()
        }
    }
}

/**
 * Tactile primary capsule button with fluid loading & confirmation morphing.
 */
@Composable
fun DiwanPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isSuccess: Boolean = false,
    successLabel: String = "Done"
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    val fill by animateColorAsState(
        targetValue = when {
            isSuccess -> colors.goldSoftTint
            else -> colors.lapisInk
        },
        animationSpec = DiwanMotion.enterFade(240),
        label = "btn_fill"
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            isSuccess -> colors.goldAccent
            else -> colors.onLapisInk
        },
        animationSpec = DiwanMotion.enterFade(240),
        label = "btn_content"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(fill)
            .then(
                if (isSuccess) {
                    Modifier.border(1.dp, colors.goldAccent.copy(alpha = 0.45f), RoundedCornerShape(50))
                } else Modifier
            )
            .tactilePress(enabled = enabled && !isLoading, onClick = onClick)
            .padding(horizontal = 24.dp)
    ) {
        val stateKey = when {
            isSuccess -> 2
            isLoading -> 1
            else -> 0
        }
        AnimatedContent(
            targetState = stateKey,
            transitionSpec = {
                (fadeIn(DiwanMotion.enterFade(180)) + scaleIn(initialScale = 0.92f)) togetherWith
                    (fadeOut(DiwanMotion.exitFade(120)) + scaleOut(targetScale = 0.92f))
            },
            label = "primary_btn_state"
        ) { state ->
            when (state) {
                1 -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .inkBreath(active = true)
                            .clip(CircleShape)
                            .background(contentColor)
                    )
                    Text(
                        text = "Verifying…",
                        style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = contentColor
                    )
                }
                2 -> Text(
                    text = "✓  $successLabel",
                    style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    color = contentColor
                )
                else -> Text(
                    text = label,
                    style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = contentColor
                )
            }
        }
    }
}

/**
 * Guardrailed Nastaliq composable. Use everywhere Urdu poetry or headers appear.
 */
@Composable
fun DiwanNastaliqText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 20.sp,
    lineHeightMultiplier: Float = 2.05f,
    weight: FontWeight = FontWeight.Normal,
    color: Color = DiwanTheme.colors.textPrimary,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        style = DiwanTheme.typography.nastaliq(
            size = size,
            lineHeightMultiplier = lineHeightMultiplier,
            weight = weight,
            color = color
        ),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
