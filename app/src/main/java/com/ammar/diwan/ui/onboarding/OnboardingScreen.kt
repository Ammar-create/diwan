package com.ammar.diwan.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.ui.theme.DiwanTypography
import com.ammar.diwan.ui.theme.FoilGold
import com.ammar.diwan.ui.theme.InkMuted
import com.ammar.diwan.ui.theme.LocalEnglishFont
import com.ammar.diwan.ui.theme.LocalUrduFont

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    BackHandler(enabled = state.currentStep != OnboardingStep.Intro) {
        viewModel.prevStep()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Quiet Top Progress Bar & Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (state.currentStep != OnboardingStep.Intro && state.currentStep != OnboardingStep.Completed) {
                    IconButton(
                        onClick = { viewModel.prevStep() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                // Step dots (Apple fluid style)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val steps = listOf(
                        OnboardingStep.Intro,
                        OnboardingStep.ProviderEndpoint,
                        OnboardingStep.ModelSelection,
                        OnboardingStep.PoetPersona
                    )
                    steps.forEach { step ->
                        val isCurrent = state.currentStep == step
                        val isDone = state.currentStep.ordinal > step.ordinal
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 16.dp else 6.dp, 6.dp)
                                .background(
                                    color = when {
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        isDone -> FoilGold
                                        else -> MaterialTheme.colorScheme.outline
                                    },
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.size(40.dp))
            }

            // Animated Body Content
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    val slideDistance = 300
                    if (forward) {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { slideDistance } + fadeIn()).togetherWith(
                            slideOutHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { -slideDistance } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { -slideDistance } + fadeIn()).togetherWith(
                            slideOutHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { slideDistance } + fadeOut()
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "OnboardingStepTransition"
            ) { step ->
                when (step) {
                    OnboardingStep.Intro -> StepIntro(onNext = { viewModel.nextStep() })
                    OnboardingStep.ProviderEndpoint -> StepProvider(
                        state = state,
                        onUpdateBaseUrl = viewModel::updateBaseUrl,
                        onUpdateApiKey = viewModel::updateApiKey,
                        onSelectPreset = viewModel::selectPreset,
                        onConnect = viewModel::fetchModelsAndProceed,
                        onSkip = { viewModel.nextStep() }
                    )
                    OnboardingStep.ModelSelection -> StepModels(
                        state = state,
                        onUpdateChatModel = viewModel::updateChatModel,
                        onUpdateVisionModel = viewModel::updateVisionModel,
                        onNext = { viewModel.nextStep() }
                    )
                    OnboardingStep.PoetPersona -> StepPersona(
                        state = state,
                        onSelectPreset = viewModel::selectPersonaPreset,
                        onUpdatePrompt = viewModel::updateSystemPrompt,
                        onFinish = {
                            viewModel.completeSetup(onComplete)
                        }
                    )
                    OnboardingStep.Completed -> StepCompleted(
                        onEnter = {
                            viewModel.completeSetup(onComplete)
                        }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Step 1: Intro with Material 3 Bouncy & Fluid Entrance Animation
// -------------------------------------------------------------
@Composable
private fun StepIntro(onNext: () -> Unit) {
    val scaleAnim = remember { Animatable(0.85f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
        ) {
            Text(
                text = "دیوان",
                fontFamily = LocalUrduFont.current,
                fontSize = 54.sp,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 60.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Diwan",
                fontFamily = LocalEnglishFont.current,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tactile Quote Card (Verses never overlap, proper line spacing)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alphaAnim.value),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "جُرأت آموز مری تابِ سخن ہے مجھ کو",
                    fontFamily = LocalUrduFont.current,
                    fontSize = 21.sp,
                    lineHeight = 44.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "شکوہ اللہ سے، خاکم بدہن، ہے مجھ کو!",
                    fontFamily = LocalUrduFont.current,
                    fontSize = 21.sp,
                    lineHeight = 44.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "❖",
                    color = FoilGold,
                    fontSize = 16.sp,
                    modifier = Modifier.scale(pulseScale)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Classical Urdu poetry anthology, calligraphy digitizer, and conversational companion.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        FluidButton(
            text = "Begin Setup",
            icon = Icons.AutoMirrored.Rounded.ArrowForward,
            onClick = onNext
        )
    }
}

// -------------------------------------------------------------
// Step 2: Provider & API Credentials (Dark Mode Optimized)
// -------------------------------------------------------------
@Composable
private fun StepProvider(
    state: OnboardingUiState,
    onUpdateBaseUrl: (String) -> Unit,
    onUpdateApiKey: (String) -> Unit,
    onSelectPreset: (String) -> Unit,
    onConnect: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Model Connection",
            fontFamily = LocalEnglishFont.current,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Connect an OpenAI-compatible endpoint for calligraphy OCR, verse analysis, and poet conversations.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Presets
        Text(
            text = "Preset Providers",
            style = MaterialTheme.typography.labelSmall,
            color = InkMuted
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Aqua", "OpenRouter", "OpenAI").forEach { preset ->
                val isSelected = (preset == "Aqua" && state.baseUrl.contains("aquadevs")) ||
                        (preset == "OpenRouter" && state.baseUrl.contains("openrouter")) ||
                        (preset == "OpenAI" && state.baseUrl.contains("openai"))

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectPreset(preset) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Base URL field
        OutlinedTextField(
            value = state.baseUrl,
            onValueChange = onUpdateBaseUrl,
            label = { Text("Base URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = diwanTextFieldColors()
        )

        // API Key field
        OutlinedTextField(
            value = state.apiKey,
            onValueChange = onUpdateApiKey,
            label = { Text("API Key") },
            placeholder = { Text("sk-...", color = InkMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = diwanTextFieldColors()
        )

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = Color(0xFFEF5350),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (state.statusMessage != null) {
            Text(
                text = state.statusMessage,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        FluidButton(
            text = if (state.isFetchingModels) "Connecting..." else "Test & Fetch Models",
            icon = Icons.Rounded.CloudSync,
            enabled = !state.isFetchingModels,
            onClick = onConnect
        )

        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text("Skip for Now", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// -------------------------------------------------------------
// Step 3: Model & Role Assignment
// -------------------------------------------------------------
@Composable
private fun StepModels(
    state: OnboardingUiState,
    onUpdateChatModel: (String) -> Unit,
    onUpdateVisionModel: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Model Assignments",
            fontFamily = LocalEnglishFont.current,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Assign which model parses calligraphy images (Vision) and which answers poetic inquiries (Chat).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = state.chatModel,
            onValueChange = onUpdateChatModel,
            label = { Text("Chat & Scholar Model") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = diwanTextFieldColors()
        )

        OutlinedTextField(
            value = state.visionModel,
            onValueChange = onUpdateVisionModel,
            label = { Text("Vision & OCR Model") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = diwanTextFieldColors()
        )

        if (state.availableModels.isNotEmpty()) {
            Text(
                text = "Detected Models (${state.availableModels.size})",
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    state.availableModels.take(6).forEach { modelId ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (modelId.contains("vision", ignoreCase = true) ||
                                        modelId.contains("flash", ignoreCase = true) ||
                                        modelId.contains("4o", ignoreCase = true)
                                    ) {
                                        onUpdateVisionModel(modelId)
                                    } else {
                                        onUpdateChatModel(modelId)
                                    }
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = modelId,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Select",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FluidButton(
            text = "Continue",
            icon = Icons.AutoMirrored.Rounded.ArrowForward,
            onClick = onNext
        )
    }
}

// -------------------------------------------------------------
// Step 4: Poet Companion Personality
// -------------------------------------------------------------
@Composable
private fun StepPersona(
    state: OnboardingUiState,
    onSelectPreset: (String) -> Unit,
    onUpdatePrompt: (String) -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Poet Companion",
            fontFamily = LocalEnglishFont.current,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Choose the voice and intellect of your poetry companion. You can fine-tune this in Settings anytime.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Iqbal", "Ghalib", "Jaun").forEach { poet ->
                val isSelected = state.systemPrompt.contains(poet)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectPreset(poet) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = poet,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.systemPrompt,
            onValueChange = onUpdatePrompt,
            label = { Text("Companion System Prompt") },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(12.dp),
            colors = diwanTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        FluidButton(
            text = "Finish & Enter Anthology",
            icon = Icons.Rounded.Check,
            onClick = onFinish
        )
    }
}

// -------------------------------------------------------------
// Step 5: Completed
// -------------------------------------------------------------
@Composable
private fun StepCompleted(onEnter: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = FoilGold,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "The Diwan is Open",
            fontFamily = LocalEnglishFont.current,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your anthology and calligraphy digitizer are ready. Immerse yourself in the verse.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        FluidButton(
            text = "Enter Anthology",
            icon = Icons.AutoMirrored.Rounded.ArrowForward,
            onClick = onEnter
        )
    }
}

// -------------------------------------------------------------
// Apple-Style Fluid Button with Instant Press Response
// -------------------------------------------------------------
@Composable
fun FluidButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "ButtonPressScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = InkMuted
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            if (icon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun diwanTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
