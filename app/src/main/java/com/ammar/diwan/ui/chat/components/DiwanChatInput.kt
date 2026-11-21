package com.ammar.diwan.ui.chat.components

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ammar.diwan.ui.theme.DiwanDimens
import com.ammar.diwan.ui.theme.DiwanMotion
import com.ammar.diwan.ui.theme.DiwanTheme
import com.ammar.diwan.ui.theme.tactilePress

private enum class SendButtonState {
    DISABLED, READY, STREAMING
}

@Composable
fun DiwanChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isStreaming: Boolean,
    onStopStreaming: () -> Unit,
    stagedImages: List<Uri> = emptyList(),
    onAddAttachments: (List<Uri>) -> Unit = {},
    onRemoveAttachment: (Uri) -> Unit = {},
    clipboardSuggestionUri: Uri? = null,
    showClipboardSuggestion: Boolean = false,
    onAcceptClipboard: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onAddAttachments(uris)
        }
    }

    // Smart Clipboard Image Suggestion
    var localClipboardUri by remember { mutableStateOf<Uri?>(null) }

    val activeClipboardUri = clipboardSuggestionUri ?: localClipboardUri
    val isClipboardVisible = (showClipboardSuggestion && clipboardSuggestionUri != null && !stagedImages.contains(clipboardSuggestionUri)) ||
        (localClipboardUri != null && !stagedImages.contains(localClipboardUri))

    val clipboard = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    }

    fun inspectClipboard() {
        if (clipboardSuggestionUri != null) return
        try {
            val clip = clipboard?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val item = clip.getItemAt(0)
                val uri = item.uri
                if (uri != null) {
                    val type = context.contentResolver.getType(uri)
                    if (type?.startsWith("image/") == true || uri.toString().contains("image")) {
                        if (!stagedImages.contains(uri)) {
                            localClipboardUri = uri
                            return
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore security or provider exceptions
        }
    }

    LaunchedEffect(Unit) {
        inspectClipboard()
    }

    // Determine Send Button State
    val buttonState = when {
        isStreaming -> SendButtonState.STREAMING
        text.isNotBlank() || stagedImages.isNotEmpty() -> SendButtonState.READY
        else -> SendButtonState.DISABLED
    }

    val capsuleShape = RoundedCornerShape(26.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = DiwanDimens.MaxChatColumnWidth)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Smart Clipboard Suggestion Chip (springs 8dp above the capsule)
        AnimatedVisibility(
            visible = isClipboardVisible && activeClipboardUri != null,
            enter = slideInVertically { it / 2 } + fadeIn(DiwanMotion.enterFade(200)) + scaleIn(initialScale = 0.92f),
            exit = slideOutVertically { it / 2 } + fadeOut(DiwanMotion.exitFade(150)) + scaleOut(targetScale = 0.92f)
        ) {
            activeClipboardUri?.let { uri ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = colors.lapisInk.copy(alpha = 0.08f),
                            spotColor = colors.lapisInk.copy(alpha = 0.12f)
                        )
                        .clip(RoundedCornerShape(50))
                        .background(colors.surfaceElevated)
                        .border(0.75.dp, colors.hairline, RoundedCornerShape(50))
                        .tactilePress(pressedScale = 0.95f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (onAcceptClipboard != null) {
                                onAcceptClipboard()
                            } else {
                                onAddAttachments(listOf(uri))
                            }
                            localClipboardUri = null
                        }
                        .padding(start = 6.dp, end = 12.dp, top = 5.dp, bottom = 5.dp)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Paste calligraphy from clipboard",
                        style = typo.caption.copy(fontWeight = FontWeight.Medium),
                        color = colors.lapisInk
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = colors.lapisInk,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // Floating Capsule Container with 0.75dp specular hairline and soft lapis elevation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = capsuleShape,
                    ambientColor = colors.lapisInk.copy(alpha = 0.12f),
                    spotColor = colors.lapisInk.copy(alpha = 0.16f)
                )
                .clip(capsuleShape)
                .background(colors.surfaceChrome)
                .border(0.75.dp, colors.specularBorder, capsuleShape)
                .animateContentSize(animationSpec = DiwanMotion.FluidSpatial)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Attachment Staging Tray (Top inside capsule)
                AnimatedVisibility(
                    visible = stagedImages.isNotEmpty(),
                    enter = expandVertically(DiwanMotion.FluidSpatial) + fadeIn(DiwanMotion.enterFade(200)),
                    exit = shrinkVertically(DiwanMotion.FluidSpatial) + fadeOut(DiwanMotion.exitFade(140))
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(stagedImages, key = { it.toString() }) { imageUri ->
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .animateItem()
                            ) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Attached Calligraphy",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(0.5.dp, colors.hairline, RoundedCornerShape(12.dp))
                                )

                                // 20dp circular remove badge
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 3.dp, y = (-3).dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xBF1A1D20))
                                        .tactilePress(pressedScale = 0.88f) {
                                            onRemoveAttachment(imageUri)
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Input Row: Photo picker button + BasicTextField + Morphing Action button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    // Left Attachment Button (38dp circle)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .border(0.5.dp, colors.hairline, CircleShape)
                            .tactilePress(pressedScale = 0.90f) {
                                photoPickerLauncher.launch("image/*")
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = "Attach Calligraphy",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Field with Multi-line Expansion (1 to 6 lines)
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Ask about verses, poets, or meter…",
                                style = typo.body.copy(fontSize = 15.sp),
                                color = colors.textTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        BasicTextField(
                            value = text,
                            onValueChange = onTextChange,
                            textStyle = typo.body.copy(
                                fontSize = 15.sp,
                                color = colors.textPrimary
                            ),
                            cursorBrush = SolidColor(colors.lapisInk),
                            maxLines = 6,
                            minLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                            keyboardActions = KeyboardActions(onDone = {
                                if (buttonState == SendButtonState.READY) onSendMessage()
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Morphing Action Button (38dp circle)
                    MorphingActionButton(
                        state = buttonState,
                        onSend = onSendMessage,
                        onStop = onStopStreaming
                    )
                }
            }
        }
    }
}

@Composable
private fun MorphingActionButton(
    state: SendButtonState,
    onSend: () -> Unit,
    onStop: () -> Unit
) {
    val colors = DiwanTheme.colors
    val haptic = LocalHapticFeedback.current

    val infinitePulse = rememberInfiniteTransition(label = "streaming_pulse")
    val pulseScale by infinitePulse.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infinitePulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(38.dp)
    ) {
        // Outer pulsing ring while streaming
        if (state == SendButtonState.STREAMING) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = pulseAlpha
                    }
                    .clip(CircleShape)
                    .background(colors.vermilion)
            )
        }

        AnimatedContent(
            targetState = state,
            transitionSpec = {
                (scaleIn(initialScale = 0.75f, animationSpec = DiwanMotion.SnappySpring) +
                    fadeIn(DiwanMotion.enterFade(180))) togetherWith
                    (scaleOut(targetScale = 0.75f) + fadeOut(DiwanMotion.exitFade(120)))
            },
            label = "action_btn_morph"
        ) { targetState ->
            when (targetState) {
                SendButtonState.DISABLED -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceGrouped.copy(alpha = 0.40f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowUpward,
                            contentDescription = "Send Disabled",
                            tint = colors.textTertiary.copy(alpha = 0.45f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                SendButtonState.READY -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.lapisInk)
                            .tactilePress(pressedScale = 0.90f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSend()
                            }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowUpward,
                            contentDescription = "Send",
                            tint = colors.onLapisInk,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                SendButtonState.STREAMING -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.vermilion)
                            .tactilePress(pressedScale = 0.90f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onStop()
                            }
                    ) {
                        // 12dp rounded square Stop glyph
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White)
                        )
                    }
                }
            }
        }
    }
}
