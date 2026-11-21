package com.ammar.diwan.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.ui.components.DiwanGroupedSection
import com.ammar.diwan.ui.components.DiwanHairline
import com.ammar.diwan.ui.components.DiwanKeyValueInputRow
import com.ammar.diwan.ui.components.DiwanLayer
import com.ammar.diwan.ui.components.DiwanNastaliqText
import com.ammar.diwan.ui.components.DiwanPrimaryButton
import com.ammar.diwan.ui.components.DiwanSegmentedControl
import com.ammar.diwan.ui.components.DiwanSurface
import com.ammar.diwan.ui.theme.DiwanDimens
import com.ammar.diwan.ui.theme.DiwanMotion
import com.ammar.diwan.ui.theme.DiwanTheme
import com.ammar.diwan.ui.theme.DiwanThemeMode
import com.ammar.diwan.ui.theme.Gulzar
import com.ammar.diwan.ui.theme.JameelNooriNastaleeq
import com.ammar.diwan.ui.theme.LocalEnglishFont
import com.ammar.diwan.ui.theme.LocalUrduFont
import com.ammar.diwan.ui.theme.NotoNastaliqUrdu
import com.ammar.diwan.ui.theme.ProvideDiwanTheme
import com.ammar.diwan.ui.theme.gildedSweep
import com.ammar.diwan.ui.theme.staggeredEntrance
import com.ammar.diwan.ui.theme.tactilePress

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    BackHandler(enabled = state.currentAct > 0) {
        viewModel.prevAct()
    }

    val activeThemeMode = when (state.themeIndex) {
        1 -> DiwanThemeMode.LIGHT
        2 -> DiwanThemeMode.DARK
        else -> DiwanThemeMode.SYSTEM
    }

    val activeUrduFont = when (state.urduFontIndex) {
        1 -> JameelNooriNastaleeq
        2 -> Gulzar
        else -> NotoNastaliqUrdu
    }

    // Dynamic living theme wrapper so toggling Atmosphere smoothly morphs the entire canvas
    ProvideDiwanTheme(
        themeMode = activeThemeMode,
        typography = DiwanTheme.typography.copy(urduFamily = activeUrduFont)
    ) {
        val colors = DiwanTheme.colors

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.canvas)
                .drawBehind {
                    val haloColor = if (colors.isDark) {
                        colors.goldAccent.copy(alpha = 0.05f)
                    } else {
                        colors.lapisInk.copy(alpha = 0.06f)
                    }
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(haloColor, Color.Transparent),
                            center = Offset(size.width * 0.5f, size.height * 0.15f),
                            radius = size.width * 0.85f
                        )
                    )
                }
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = DiwanDimens.MaxOnboardingWidth)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shared Progress Rail Top Bar
                OnboardingTopRail(
                    currentAct = state.currentAct,
                    onBack = { viewModel.prevAct() },
                    onSkip = { viewModel.skipAndComplete(onComplete) }
                )

                // Living 3-Act Animated Body
                AnimatedContent(
                    targetState = state.currentAct,
                    transitionSpec = {
                        val forward = targetState > initialState
                        if (forward) {
                            (slideInHorizontally(spring(dampingRatio = 0.88f, stiffness = 380f)) { it / 6 } +
                                fadeIn(DiwanMotion.enterFade(240)) + scaleIn(initialScale = 0.98f)).togetherWith(
                                slideOutHorizontally(tween(180)) { -it / 8 } +
                                    fadeOut(DiwanMotion.exitFade(140))
                            )
                        } else {
                            (slideInHorizontally(spring(dampingRatio = 0.88f, stiffness = 380f)) { -it / 6 } +
                                fadeIn(DiwanMotion.enterFade(240)) + scaleIn(initialScale = 0.98f)).togetherWith(
                                slideOutHorizontally(tween(180)) { it / 8 } +
                                    fadeOut(DiwanMotion.exitFade(140))
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    label = "OnboardingActTransition"
                ) { act ->
                    when (act) {
                        0 -> Act1CalligraphicReveal(
                            onOpen = { viewModel.setAct(1) }
                        )
                        1 -> Act2ScriptAtmosphere(
                            state = state,
                            onThemeChange = viewModel::setThemeIndex,
                            onFontChange = viewModel::setUrduFontIndex,
                            onContinue = { viewModel.setAct(2) }
                        )
                        2 -> Act3ScholarsDesk(
                            state = state,
                            onUpdateBaseUrl = viewModel::updateBaseUrl,
                            onUpdateApiKey = viewModel::updateApiKey,
                            onUpdateModel = viewModel::updateChatModel,
                            onVerify = { viewModel.verifyAndComplete(onComplete) },
                            onSkip = { viewModel.skipAndComplete(onComplete) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingTopRail(
    currentAct: Int,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Left: Back Button
        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.CenterStart) {
            androidx.compose.animation.AnimatedVisibility(
                visible = currentAct > 0,
                enter = fadeIn(DiwanMotion.enterFade(180)),
                exit = fadeOut(DiwanMotion.exitFade(140))
            ) {
                Text(
                    text = "Back",
                    style = typo.caption.copy(fontWeight = FontWeight.Medium),
                    color = colors.textSecondary,
                    modifier = Modifier.tactilePress(pressedScale = 0.94f, onClick = onBack)
                )
            }
        }

        // Top Center: Fluid Indicator Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (0..2).forEach { index ->
                val isActive = currentAct == index
                val isCompleted = currentAct > index
                val pillWidth by animateDpAsState(
                    targetValue = if (isActive) 24.dp else 6.dp,
                    animationSpec = DiwanMotion.SnappyDpSpring,
                    label = "rail_pill_width_$index"
                )
                val pillColor = when {
                    isActive -> colors.lapisInk
                    isCompleted -> colors.goldAccent
                    else -> colors.hairline
                }

                Box(
                    modifier = Modifier
                        .size(width = pillWidth, height = 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(pillColor)
                )
            }
        }

        // Top Right: Skip Button
        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.CenterEnd) {
            androidx.compose.animation.AnimatedVisibility(
                visible = currentAct == 2,
                enter = fadeIn(DiwanMotion.enterFade(180)),
                exit = fadeOut(DiwanMotion.exitFade(140))
            ) {
                Text(
                    text = "Skip",
                    style = typo.caption.copy(fontWeight = FontWeight.Medium),
                    color = colors.textTertiary,
                    modifier = Modifier.tactilePress(pressedScale = 0.94f, onClick = onSkip)
                )
            }
        }
    }
}

/**
 * Act 1 — The Calligraphic Reveal (Welcome)
 * Right-to-Left unmasking of دیوان wordmark + calm editorial entrance.
 */
@Composable
private fun Act1CalligraphicReveal(
    onOpen: () -> Unit
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography
    val density = LocalDensity.current

    val unmaskProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        unmaskProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = DiwanMotion.FastEaseOut)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.9f))

        // Right-to-Left Animated Calligraphic Unmask Wordmark
        val translationYPx = with(density) { 8.dp.toPx() }
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = (1f - unmaskProgress.value) * translationYPx
                }
                .drawWithContent {
                    clipRect(
                        left = size.width * (1f - unmaskProgress.value),
                        top = 0f,
                        right = size.width,
                        bottom = size.height
                    ) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            DiwanNastaliqText(
                text = "دیوان",
                size = 54.sp,
                lineHeightMultiplier = 1.95f,
                color = colors.lapisInk
            )
        }

        Spacer(Modifier.height(18.dp))

        // Ornamental gold divider (staggered entrance)
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(1.dp)
                .staggeredEntrance(delayMs = 260)
                .background(colors.goldAccent.copy(alpha = 0.5f))
        )

        Spacer(Modifier.height(22.dp))

        // Editorial Title
        Text(
            text = "A Quiet Sanctuary for Urdu Verse",
            style = typo.editorialTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .staggeredEntrance(delayMs = 340)
                .fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        // Subtitle
        Text(
            text = "Read calligraphy, explore meter and Persian roots, and build your personal folio.",
            style = typo.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 340.dp)
                .staggeredEntrance(delayMs = 420)
        )

        Spacer(Modifier.weight(1.2f))

        // Bottom CTA
        DiwanPrimaryButton(
            label = "Open the Diwan",
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth()
                .staggeredEntrance(delayMs = 520)
        )

        Spacer(Modifier.height(28.dp))
    }
}

/**
 * Act 2 — Script & Atmosphere (Live Manuscript Calibration)
 * Real-time preview card reacting to theme and Urdu calligraphy switches.
 */
@Composable
private fun Act2ScriptAtmosphere(
    state: OnboardingUiState,
    onThemeChange: (Int) -> Unit,
    onFontChange: (Int) -> Unit,
    onContinue: () -> Unit
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Atmosphere & Script",
                style = typo.editorialTitle,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Fine-tune your reading parchment and Urdu calligraphy style.",
                style = typo.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Live Interactive Preview Card (L3 Manuscript Artifact)
            DiwanSurface(
                layer = DiwanLayer.L3_ARTIFACT,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(0.75.dp, colors.goldAccent.copy(alpha = 0.22f)),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AnimatedContent(
                            targetState = state.urduFontIndex,
                            transitionSpec = {
                                (fadeIn(DiwanMotion.enterFade(180)) + scaleIn(initialScale = 0.98f))
                                    .togetherWith(fadeOut(DiwanMotion.exitFade(120)))
                            },
                            label = "live_couplet_preview"
                        ) {
                            DiwanNastaliqText(
                                text = "خودی کو کر بلند اتنا کہ ہر تقدیر سے پہلے\nخدا بندے سے خود پوچھے بتا تیری رضا کیا ہے",
                                size = 21.sp,
                                lineHeightMultiplier = 2.1f,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                        }

                        DiwanHairline(modifier = Modifier.padding(vertical = 14.dp))

                        Text(
                            text = "Allama Muhammad Iqbal · Bal-e-Jibril",
                            style = typo.caption.copy(fontFamily = LocalEnglishFont.current, fontSize = 13.sp),
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Grouped Segmented Switchers
            DiwanGroupedSection(title = "PAPER & SCRIPT") {
                // Atmosphere row
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Atmosphere",
                        style = typo.caption.copy(fontWeight = FontWeight.Medium),
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DiwanSegmentedControl(
                        options = listOf("System", "Parchment", "Nocturne"),
                        selectedIndex = state.themeIndex,
                        onOptionSelected = onThemeChange
                    )
                }

                DiwanHairline(insetStart = 16.dp)

                // Urdu Script row
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Urdu Calligraphy",
                        style = typo.caption.copy(fontWeight = FontWeight.Medium),
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DiwanSegmentedControl(
                        options = listOf("Noto", "Jameel", "Gulzar"),
                        selectedIndex = state.urduFontIndex,
                        onOptionSelected = onFontChange
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 28.dp)) {
            DiwanPrimaryButton(
                label = "Continue",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Act 3 — The Scholar's Desk (Intelligence & API Setup)
 * Borderless Apple-grade inputs, clipboard detection, and gilded handshake verification.
 */
@Composable
private fun Act3ScholarsDesk(
    state: OnboardingUiState,
    onUpdateBaseUrl: (String) -> Unit,
    onUpdateApiKey: (String) -> Unit,
    onUpdateModel: (String) -> Unit,
    onVerify: () -> Unit,
    onSkip: () -> Unit
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography
    val clipboardManager = LocalClipboardManager.current

    var clipboardText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val clip = clipboardManager.getText()?.text.orEmpty().trim()
        if (clip.startsWith("sk-") || clip.startsWith("http://") || clip.startsWith("https://")) {
            clipboardText = clip
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Connect Your Scholar",
                style = typo.editorialTitle,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Bring your own OpenAI-compatible endpoint for calligraphy OCR and literary dialogue.",
                style = typo.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 380.dp)
            )

            Spacer(Modifier.height(18.dp))

            // Smart Clipboard Detection Pill
            if (clipboardText.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .staggeredEntrance(delayMs = 80)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surfaceElevated)
                        .border(0.5.dp, colors.lapisInk.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .tactilePress {
                            if (clipboardText.startsWith("http")) {
                                onUpdateBaseUrl(clipboardText)
                            } else {
                                onUpdateApiKey(clipboardText)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentPaste,
                        contentDescription = null,
                        tint = colors.lapisInk,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Detected ${if (clipboardText.startsWith("http")) "URL" else "key"} in clipboard · Tap to fill",
                        style = typo.caption.copy(fontWeight = FontWeight.Medium),
                        color = colors.lapisInk
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Grouped Technical Input Card with gilded sweep on handshake success
            DiwanGroupedSection(
                title = "INTELLIGENCE ENGINE",
                modifier = Modifier.gildedSweep(triggerKey = state.sweepTrigger)
            ) {
                DiwanKeyValueInputRow(
                    label = "Base URL",
                    value = state.baseUrl,
                    onValueChange = onUpdateBaseUrl,
                    placeholder = "https://api.openai.com/v1"
                )

                DiwanHairline(insetStart = 16.dp)

                DiwanKeyValueInputRow(
                    label = "API Key",
                    value = state.apiKey,
                    onValueChange = onUpdateApiKey,
                    placeholder = "sk-••••••••",
                    isSecret = true
                )

                DiwanHairline(insetStart = 16.dp)

                DiwanKeyValueInputRow(
                    label = "Model",
                    value = state.chatModel,
                    onValueChange = onUpdateModel,
                    placeholder = "deepseek-flash-1017"
                )
            }

            // Verification error message
            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn(DiwanMotion.enterFade(160)),
                exit = fadeOut(DiwanMotion.exitFade(120))
            ) {
                Text(
                    text = state.errorMessage.orEmpty(),
                    style = typo.caption,
                    color = colors.vermilion,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 8.dp, end = 8.dp)
                )
            }
        }

        // Bottom Actions
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 24.dp)
        ) {
            DiwanPrimaryButton(
                label = "Verify & Enter Diwan",
                onClick = onVerify,
                isLoading = state.isVerifying,
                isSuccess = state.isVerified,
                successLabel = "Connected",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Configure later — explore offline folio",
                style = typo.caption.copy(fontWeight = FontWeight.Medium),
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .tactilePress(pressedScale = 0.96f, onClick = onSkip)
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            )
        }
    }
}

/**
 * Backward-compatible FluidButton delegate using Diwan tactile tokens.
 */
@Composable
fun FluidButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(if (enabled) colors.lapisInk else colors.trackRecessed)
            .tactilePress(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = if (enabled) colors.onLapisInk else colors.textTertiary
            )
            if (icon != null) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) colors.onLapisInk else colors.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

