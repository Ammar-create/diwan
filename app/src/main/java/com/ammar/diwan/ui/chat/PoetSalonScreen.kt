package com.ammar.diwan.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.data.models.ChatMessage
import com.ammar.diwan.ui.theme.DiwanTypography
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

@Composable
fun PoetSalonScreen(
    viewModel: PoetSalonViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ParchmentBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = InkPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "محفلِ سخن (Poet's Salon)",
                        fontFamily = InstrumentSerif,
                        fontSize = 20.sp,
                        color = InkPrimary
                    )
                    Text(
                        text = "Literary debate, bahr, and verse curation",
                        style = DiwanTypography.labelSmall,
                        color = InkSecondary
                    )
                }
            }

            // Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }

                if (state.isSending) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CircularProgressIndicator(
                                color = LapisNavy,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Error display
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage!!,
                    color = Color(0xFFB00020),
                    style = DiwanTypography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ParchmentSurface,
                border = BorderStroke(1.dp, ParchmentBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.inputText,
                        onValueChange = viewModel::updateInputText,
                        placeholder = { Text("Ask about verses, poets, or meter...", color = InkMuted) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LapisNavy,
                            unfocusedBorderColor = ParchmentBorder,
                            focusedContainerColor = ParchmentBackground,
                            unfocusedContainerColor = ParchmentBackground,
                            focusedTextColor = InkPrimary,
                            unfocusedTextColor = InkPrimary
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = viewModel::sendMessage,
                        enabled = state.inputText.isNotBlank() && !state.isSending,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (state.inputText.isNotBlank() && !state.isSending) LapisNavy else ParchmentBorder,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send",
                            tint = ParchmentBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) LapisNavy else ParchmentSurface,
            border = BorderStroke(1.dp, if (isUser) LapisNavy else FoilGoldSubtle),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                val isUrdu = message.content.any { it in '\u0600'..'\u06FF' }

                Text(
                    text = message.content,
                    fontFamily = if (isUrdu) NotoNastaliqUrdu else DiwanTypography.bodyMedium.fontFamily,
                    fontSize = if (isUrdu) 20.sp else 15.sp,
                    lineHeight = if (isUrdu) 2.1.sp else 22.sp,
                    color = if (isUser) ParchmentBackground else InkPrimary,
                    textAlign = if (isUrdu && !isUser) TextAlign.Right else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
