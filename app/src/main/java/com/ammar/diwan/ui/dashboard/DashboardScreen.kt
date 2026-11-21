package com.ammar.diwan.ui.dashboard

import android.content.Intent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.ui.components.DiwanNastaliqText
import com.ammar.diwan.ui.theme.DiwanTheme
import com.ammar.diwan.ui.theme.LocalEnglishFont
import com.ammar.diwan.ui.theme.tactilePress

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val showRoman by viewModel.showRomanUrdu.collectAsState()
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusToast) {
        state.statusToast?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Editorial Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    DiwanNastaliqText(
                        text = "دیوان",
                        size = 32.sp,
                        lineHeightMultiplier = 1.95f,
                        color = colors.lapisInk
                    )
                    Text(
                        text = "Personal Folio",
                        fontFamily = LocalEnglishFont.current,
                        fontSize = 20.sp,
                        color = colors.textSecondary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Toggle Romanization Capsule
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (showRoman) colors.lapisSoftTint else colors.surfaceElevated)
                            .border(
                                BorderStroke(
                                    0.5.dp,
                                    if (showRoman) colors.lapisInk.copy(alpha = 0.4f) else colors.hairline
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .tactilePress(pressedScale = 0.95f) { viewModel.toggleRomanUrdu() }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.FontDownload,
                                contentDescription = "Roman Urdu Toggle",
                                tint = if (showRoman) colors.lapisInk else colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Roman",
                                style = typo.caption.copy(
                                    fontWeight = if (showRoman) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (showRoman) colors.lapisInk else colors.textSecondary
                            )
                        }
                    }

                    // Poet's Salon (Chat Sanctuary) Icon Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .border(0.5.dp, colors.hairline, CircleShape)
                            .tactilePress(pressedScale = 0.92f, onClick = onNavigateToChat)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Chat,
                            contentDescription = "Poet's Salon",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Settings Icon Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .border(0.5.dp, colors.hairline, CircleShape)
                            .tactilePress(pressedScale = 0.92f, onClick = onNavigateToSettings)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Poetry Carousel Deck
            Box(modifier = Modifier.weight(1f)) {
                PoemDeck(
                    poems = state.poems,
                    showRomanUrdu = showRoman,
                    onCopyPoem = { poem ->
                        viewModel.copyPoemToClipboard(context, poem)
                    },
                    onSharePoem = { poem ->
                        viewModel.openExportSheet(poem)
                    }
                )
            }
        }

        // Floating Chat Sanctuary Capsule Button (replaces standalone OCR with unified sanctuary)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = colors.lapisInk.copy(alpha = 0.15f),
                    spotColor = colors.lapisInk.copy(alpha = 0.20f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(colors.lapisInk)
                .border(BorderStroke(0.75.dp, colors.specularBorder), RoundedCornerShape(24.dp))
                .tactilePress(pressedScale = 0.95f, onClick = onNavigateToChat)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Chat,
                    contentDescription = "Poet's Salon Sanctuary",
                    tint = colors.onLapisInk,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Poet's Salon",
                    style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.onLapisInk
                )
            }
        }

        // Export Dialog
        if (state.isExportSheetOpen && state.selectedPoemForExport != null) {
            ExportCardDialog(
                poem = state.selectedPoemForExport!!,
                onDismiss = viewModel::closeExportSheet,
                onShareImage = {
                    val poem = state.selectedPoemForExport!!
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "${poem.titleUrdu} — ${poem.poetUrdu}\n\n" +
                                poem.verses.joinToString("\n") { "${it.urdu}\n${it.roman}" }
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Poem Card"))
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}
