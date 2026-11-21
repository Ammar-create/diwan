package com.ammar.diwan.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

object DiwanMotion {
    // 1. Snappy tactile controls (buttons, segmented thumb, chips, badges)
    val SnappySpring = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 520f
    )
    val SnappyDpSpring = spring<Dp>(
        dampingRatio = 0.85f,
        stiffness = 520f
    )

    // 2. Fluid spatial expansion (ThinkingBlock, attachment strip, Folio card unfurl)
    val FluidSpatial = spring<IntSize>(
        dampingRatio = 0.88f,
        stiffness = 380f
    )
    val FluidSpatialFloat = spring<Float>(
        dampingRatio = 0.88f,
        stiffness = 380f
    )
    val FluidSpatialInt = spring<Int>(
        dampingRatio = 0.88f,
        stiffness = 380f
    )
    val FluidSpatialOffset = spring<IntOffset>(
        dampingRatio = 0.88f,
        stiffness = 380f
    )
    val FluidSpatialDp = spring<Dp>(
        dampingRatio = 0.88f,
        stiffness = 380f
    )

    // 3. Calm editorial settle (onboarding choreography, empty state, modal sheet)
    val GentleSettle = spring<Float>(
        dampingRatio = 0.92f,
        stiffness = 280f
    )
    val GentleSettleDp = spring<Dp>(
        dampingRatio = 0.92f,
        stiffness = 280f
    )

    // 4. Asymmetric opacity/color curves (never spring opacity)
    val FastEaseOut = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    val FastEaseIn = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    fun <T> enterFade(durationMs: Int = 220) = tween<T>(durationMs, easing = FastEaseOut)
    fun <T> exitFade(durationMs: Int = 140) = tween<T>(durationMs, easing = FastEaseIn)
}

/**
 * Draw-phase tactile press feedback (replaces bulky default Material ripples).
 * Zero layout or recomposition overhead during press.
 */
fun Modifier.tactilePress(
    enabled: Boolean = true,
    pressedScale: Float = 0.965f,
    haptic: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = DiwanMotion.SnappySpring,
        label = "tactile_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.84f else 1f,
        animationSpec = DiwanMotion.exitFade(100),
        label = "tactile_alpha"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = if (enabled) alpha else 0.45f
        }
        .clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                if (haptic) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                onClick()
            }
        )
}

/**
 * Choreographed entrance for onboarding elements, welcome chips, and grouped cards.
 * Uses pure draw-phase translationY + alpha.
 */
fun Modifier.staggeredEntrance(
    visible: Boolean = true,
    delayMs: Int = 0,
    slideOffsetDp: Dp = 12.dp
): Modifier = composed {
    val density = LocalDensity.current
    val slidePx = with(density) { slideOffsetDp.toPx() }
    val progress = remember { Animatable(if (visible) 0f else 1f) }

    LaunchedEffect(visible) {
        if (visible) {
            if (delayMs > 0) delay(delayMs.toLong())
            progress.animateTo(1f, animationSpec = DiwanMotion.GentleSettle)
        } else {
            progress.animateTo(0f, animationSpec = DiwanMotion.exitFade(140))
        }
    }

    this.graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        translationY = (1f - p) * slidePx
    }
}

/**
 * One-shot diagonal gilded light sweep across a surface (e.g., when "Saved to Folio"
 * or when onboarding API verification succeeds). Increment [triggerKey] to fire.
 */
fun Modifier.gildedSweep(
    triggerKey: Int,
    sheenColor: Color = Color(0x4DD4AF37),
    durationMs: Int = 520
): Modifier = composed {
    val progress = remember { Animatable(-0.5f) }

    LaunchedEffect(triggerKey) {
        if (triggerKey > 0) {
            progress.snapTo(-0.5f)
            progress.animateTo(
                targetValue = 1.6f,
                animationSpec = tween(durationMillis = durationMs, easing = DiwanMotion.FastEaseOut)
            )
        }
    }

    this.drawWithContent {
        drawContent()
        val p = progress.value
        if (p > -0.45f && p < 1.55f && size.width > 0f) {
            val bandWidth = size.width * 0.45f
            val centerX = size.width * p
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, sheenColor, Color.Transparent),
                    start = Offset(centerX - bandWidth, 0f),
                    end = Offset(centerX + bandWidth, size.height)
                )
            )
        }
    }
}

/**
 * Lightweight 60fps breathing opacity modifier for active ink dots or subtle halos.
 */
fun Modifier.inkBreath(active: Boolean = true): Modifier = composed {
    if (!active) return@composed this
    val infinite = rememberInfiniteTransition(label = "ink_breath")
    val pulse by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    this.graphicsLayer {
        alpha = pulse
        val s = 0.88f + (pulse * 0.16f)
        scaleX = s
        scaleY = s
    }
}
