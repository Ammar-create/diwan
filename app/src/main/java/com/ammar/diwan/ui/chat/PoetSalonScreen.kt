package com.ammar.diwan.ui.chat

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.ammar.diwan.data.models.ChatMessage
import com.ammar.diwan.data.models.Poem
import com.ammar.diwan.ui.chat.components.DiwanChatInput
import com.ammar.diwan.ui.chat.components.ManuscriptCard
import com.ammar.diwan.ui.chat.components.ThinkingBlock
import com.ammar.diwan.ui.components.DiwanEdgeScrim
import com.ammar.diwan.ui.theme.DiwanDimens
import com.ammar.diwan.ui.theme.DiwanMotion
import com.ammar.diwan.ui.theme.DiwanTheme
import com.ammar.diwan.ui.theme.staggeredEntrance
import com.ammar.diwan.ui.theme.tactilePress
import kotlinx.coroutines.launch

@Composable
fun PoetSalonScreen(
    viewModel: PoetSalonViewModel,
    onNavigateBack: () -> Unit,
    onPoemSavedToFolio: (Poem) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var zoomImageUri by remember { mutableStateOf<String?>(null) }

    // Scroll-linked header hairline visibility
    val isScrolledPastTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 10
        }
    }

    // Sticky auto-scroll tracking
    val isAtBottom by remember {
        derivedStateOf {
            !listState.canScrollForward
        }
    }

    // Pin bottom while streaming unless user scrolled up
    LaunchedEffect(state.messages.lastOrNull()?.content, state.messages.lastOrNull()?.thinkingContent) {
        if (state.isSending && isAtBottom && state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.size - 1)
        }
    }

    // Scroll to bottom on newly added message by user
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        // --- 1. Message Stream / Empty Sanctuary Feed ---
        if (state.messages.isEmpty()) {
            EmptySanctuaryState(
                onPromptSelected = { prompt ->
                    viewModel.sendPrompt(prompt, context)
                },
                onPasteClipboard = {
                    viewModel.pasteFromClipboard(context)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.Center)
            )
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = 80.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
            ) {
                items(
                    items = state.messages,
                    key = { it.id }
                ) { message ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        contentAlignment = if (message.role == "user") Alignment.CenterEnd else Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = DiwanDimens.MaxChatColumnWidth)
                                .padding(horizontal = 16.dp)
                        ) {
                            if (message.role == "user") {
                                UserMessageBubble(
                                    message = message,
                                    onImageClick = { uri -> zoomImageUri = uri },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            } else {
                                AssistantManuscriptMessage(
                                    message = message,
                                    onSavePoem = { poem ->
                                        viewModel.savePoemToFolio(message.id, onSavePoemToFolio)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Feathered Edge Scrims (replacing live blurs over scroll) ---
        // Top edge scrim behind Top Bar
        DiwanEdgeScrim(
            fromTop = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .align(Alignment.TopCenter)
        )

        // Bottom edge scrim behind Input Capsule
        DiwanEdgeScrim(
            fromTop = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .align(Alignment.BottomCenter)
        )

        // --- 3. Dual-Script Header ---
        DualScriptHeader(
            isScrolled = isScrolledPastTop,
            onBack = onNavigateBack,
            onNewThread = viewModel::clearConversation,
            onSettings = onNavigateToSettings,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        // --- 4. Floating Jump to Latest Button ---
        AnimatedVisibility(
            visible = !isAtBottom && state.messages.isNotEmpty(),
            enter = scaleIn(initialScale = 0.8f, animationSpec = DiwanMotion.SnappySpring) +
                fadeIn(DiwanMotion.enterFade(180)),
            exit = scaleOut(targetScale = 0.8f, animationSpec = DiwanMotion.SnappySpring) +
                fadeOut(DiwanMotion.exitFade(120)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 84.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = colors.lapisInk.copy(alpha = 0.12f),
                        spotColor = colors.lapisInk.copy(alpha = 0.16f)
                    )
                    .clip(CircleShape)
                    .background(colors.surfaceChrome)
                    .border(0.75.dp, colors.specularBorder, CircleShape)
                    .tactilePress(pressedScale = 0.90f) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(state.messages.size - 1)
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Jump to latest",
                    tint = colors.lapisInk,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- 5. Error Banner (if any) ---
        state.errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 74.dp, start = 20.dp, end = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceElevated)
                    .border(0.5.dp, colors.vermilion.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        style = typo.caption,
                        color = colors.vermilion,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .tactilePress(pressedScale = 0.88f) {
                                viewModel.clearErrorMessage()
                            }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Dismiss",
                            tint = colors.vermilion,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // --- 6. Floating Diwan Chat Input ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .imePadding()
                .navigationBarsPadding()
        ) {
            DiwanChatInput(
                text = state.inputText,
                onTextChange = viewModel::updateInputText,
                onSendMessage = { viewModel.sendMessage(context) },
                isStreaming = state.isSending,
                onStopStreaming = viewModel::stopStreaming,
                stagedImages = state.stagedImages,
                onAddAttachments = viewModel::stageImages,
                onRemoveAttachment = viewModel::unstageImage,
                clipboardSuggestionUri = state.clipboardImageUri,
                showClipboardSuggestion = state.showClipboardSuggestion,
                onAcceptClipboard = viewModel::acceptClipboardImage,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // --- 7. Calligraphy Lightbox / Zoom Dialog ---
        zoomImageUri?.let { uri ->
            CalligraphyZoomDialog(
                imageUri = uri,
                onDismiss = { zoomImageUri = null }
            )
        }
    }
}

/**
 * Dual-Script Header: Stacked Column with Urdu Nastaliq top (lineHeight = 32sp) +
 * Latin subtitle with optical offset (-2dp). Contextual actions grouped in L2 capsule.
 */
@Composable
private fun DualScriptHeader(
    isScrolled: Boolean,
    onBack: () -> Unit,
    onNewThread: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    val dividerAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = tween(180),
        label = "header_divider_alpha"
    )

    Column(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp)
        ) {
            // Left: Minimal 36dp Back Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceChrome)
                    .border(0.5.dp, colors.hairline, CircleShape)
                    .tactilePress(pressedScale = 0.90f, onClick = onBack)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Center: Strictly stacked Column with optical offset
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "محفلِ سخن",
                    style = typo.nastaliq(
                        size = 18.sp,
                        lineHeightMultiplier = 1.8f,
                        color = colors.textPrimary
                    ),
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "POET'S SALON",
                    style = typo.caption.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.6.sp
                    ),
                    color = colors.textSecondary,
                    modifier = Modifier.offset(y = (-2).dp)
                )
            }

            // Right: Contextual actions inside single grouped L2 capsule pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.surfaceChrome)
                    .border(0.5.dp, colors.hairline, RoundedCornerShape(50))
                    .padding(horizontal = 4.dp)
            ) {
                // New Thread Action
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .tactilePress(pressedScale = 0.88f, onClick = onNewThread)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = "New Thread",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // 0.5dp vertical hairline divider
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(18.dp)
                        .background(colors.hairline)
                )

                // Settings Action
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .tactilePress(pressedScale = 0.88f, onClick = onSettings)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Scroll-linked bottom hairline divider
        if (dividerAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.hairline.copy(alpha = colors.hairline.alpha * dividerAlpha))
            )
        }
    }
}

/**
 * Calm Empty Sanctuary State: Center calm welcome with calamus emblem, Nastaliq greeting,
 * and 3 quick-start prompt chips with staggered entrance.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptySanctuaryState(
    onPromptSelected: (String) -> Unit,
    onPasteClipboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .widthIn(max = DiwanDimens.MaxChatColumnWidth)
            .padding(vertical = 40.dp)
    ) {
        // Calamus / Emblem vector motif (36dp, 70% opacity, 0ms delay)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .staggeredEntrance(delayMs = 0)
                .size(54.dp)
                .clip(CircleShape)
                .background(colors.lapisSoftTint)
                .border(0.75.dp, colors.lapisInk.copy(alpha = 0.18f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.HistoryEdu,
                contentDescription = null,
                tint = colors.lapisInk.copy(alpha = 0.85f),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Nastaliq Greeting (60ms delay)
        Text(
            text = "سخن کہیے، یا کوئی مخطوطہ دکھائیے",
            style = typo.nastaliq(
                size = 22.sp,
                lineHeightMultiplier = 2.1f,
                color = colors.textPrimary
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .staggeredEntrance(delayMs = 60)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // English Sub-caption
        Text(
            text = "Share a couplet, paste calligraphy, or explore meter & meaning",
            style = typo.caption,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .staggeredEntrance(delayMs = 100)
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 3 Quick-Start Prompt Chips (staggered cascade 120ms, 160ms, 200ms)
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PromptChip(
                label = "Paste calligraphy from clipboard",
                delayMs = 120,
                onClick = onPasteClipboard
            )
            PromptChip(
                label = "Analyze a ghazal's bahr (meter)",
                delayMs = 160,
                onClick = { onPromptSelected("Analyze a ghazal's bahr (meter)") }
            )
            PromptChip(
                label = "Explain Iqbal's concept of Khudi",
                delayMs = 200,
                onClick = { onPromptSelected("Explain Iqbal's concept of Khudi") }
            )
        }
    }
}

@Composable
private fun PromptChip(
    label: String,
    delayMs: Int,
    onClick: () -> Unit
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    Box(
        modifier = Modifier
            .staggeredEntrance(delayMs = delayMs)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceGrouped)
            .border(0.5.dp, colors.hairline, RoundedCornerShape(12.dp))
            .tactilePress(pressedScale = 0.96f, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            style = typo.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = colors.textPrimary
        )
    }
}

/**
 * User Message Bubble: Right-aligned, capped at 460dp, warm lapis fill,
 * with staged calligraphy mosaic.
 */
@Composable
private fun UserMessageBubble(
    message: ChatMessage,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    val bubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = 20.dp,
        bottomEnd = 6.dp
    )

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier.widthIn(max = 460.dp)
    ) {
        // Attached Calligraphy Images Mosaic
        if (message.images.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                items(message.images) { imgUri ->
                    AsyncImage(
                        model = imgUri,
                        contentDescription = "Calligraphy Attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.5.dp, colors.hairline, RoundedCornerShape(12.dp))
                            .tactilePress(pressedScale = 0.94f) {
                                onImageClick(imgUri)
                            }
                    )
                }
            }
        }

        // Text Bubble
        if (message.content.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(colors.userBubbleFill)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                val hasUrdu = message.content.any { it in '\u0600'..'\u06FF' }
                if (hasUrdu) {
                    Text(
                        text = message.content,
                        style = typo.nastaliq(
                            size = 18.sp,
                            lineHeightMultiplier = 1.95f,
                            color = colors.userBubbleText
                        ),
                        textAlign = TextAlign.Start
                    )
                } else {
                    Text(
                        text = message.content,
                        style = typo.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
                        color = colors.userBubbleText
                    )
                }
            }
        }
    }
}

/**
 * Assistant Manuscript Message: Full-width editorial column like an open manuscript page.
 * Separated by 12dp vertical rhythm: ThinkingBlock -> Urdu Couplet Inset -> Latin Commentary -> Folio Card.
 */
@Composable
private fun AssistantManuscriptMessage(
    message: ChatMessage,
    onSavePoem: (Poem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Collapsible ThinkingBlock (if reasoning tokens exist or active)
        if (!message.thinkingContent.isNullOrBlank() || message.isThinking) {
            ThinkingBlock(
                thoughtText = message.thinkingContent.orEmpty(),
                isThinking = message.isThinking,
                elapsedSeconds = message.thinkingDurationMs / 1000f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2. Structured Poem OR Couplet Extraction
        val structured = message.structuredPoem
        if (structured != null && structured.verses.isNotEmpty()) {
            // Urdu Couplet Inset with Right-edge Lapis Accent Bar
            UrduCoupletsBlock(
                verses = structured.verses.map { it.urdu }
            )

            // Latin Commentary / Roman Urdu
            if (message.content.isNotBlank()) {
                Text(
                    text = message.content,
                    style = typo.body.copy(fontSize = 15.sp, lineHeight = 23.sp),
                    color = colors.textPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Interactive "Add to Folio" Artifact Card
            ManuscriptCard(
                poem = structured,
                isSaved = message.isSavedToFolio,
                onSaveToFolio = onSavePoem,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Text Stream without structured card yet
            if (message.content.isNotBlank()) {
                val lines = message.content.lines()
                val hasUrduLines = lines.any { line -> line.any { it in '\u0600'..'\u06FF' } }

                if (hasUrduLines && lines.size > 1) {
                    val urduLines = lines.filter { it.any { c -> c in '\u0600'..'\u06FF' } }
                    val latinLines = lines.filterNot { it.any { c -> c in '\u0600'..'\u06FF' } }

                    if (urduLines.isNotEmpty()) {
                        UrduCoupletsBlock(verses = urduLines)
                    }

                    if (latinLines.any { it.isNotBlank() }) {
                        Text(
                            text = latinLines.joinToString("\n").trim(),
                            style = typo.body.copy(fontSize = 15.sp, lineHeight = 23.sp),
                            color = colors.textPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Regular content
                    val isUrduOnly = message.content.any { it in '\u0600'..'\u06FF' }
                    if (isUrduOnly) {
                        Text(
                            text = message.content,
                            style = typo.nastaliq(
                                size = 20.sp,
                                lineHeightMultiplier = 2.05f,
                                color = colors.textPrimary
                            ),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = message.content,
                            style = typo.body.copy(fontSize = 15.sp, lineHeight = 23.sp),
                            color = colors.textPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Urdu Couplet Block: Elevated subtle L1 parchment inset with generous 2.1x Nastaliq line height,
 * centered couplet alignment, and 2.5dp right-edge Lapis accent bar for RTL verses.
 */
@Composable
private fun UrduCoupletsBlock(
    verses: List<String>,
    modifier: Modifier = Modifier
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGrouped)
            .border(0.5.dp, colors.hairline, shape)
    ) {
        // 2.5dp right-edge Lapis accent bar for authentic RTL manuscript framing
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .background(colors.lapisInk)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 22.dp, top = 16.dp, bottom = 16.dp)
        ) {
            verses.forEach { verse ->
                if (verse.isNotBlank()) {
                    Text(
                        text = verse,
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
    }
}

/**
 * Calligraphy Lightbox / Zoom Dialog for inspection
 */
@Composable
private fun CalligraphyZoomDialog(
    imageUri: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .tactilePress(pressedScale = 1f, onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Zoomed Calligraphy",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(16.dp))
            )

            // Close button top right
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(20.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .tactilePress(pressedScale = 0.88f, onClick = onDismiss)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
