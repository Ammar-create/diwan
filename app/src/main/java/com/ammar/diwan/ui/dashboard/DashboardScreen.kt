package com.ammar.diwan.ui.dashboard

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.ui.theme.DiwanTypography
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
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val showRoman by viewModel.showRomanUrdu.collectAsState()
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
            .background(ParchmentBackground)
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
                    Text(
                        text = "دیوان",
                        fontFamily = NotoNastaliqUrdu,
                        fontSize = 32.sp,
                        lineHeight = 1.6.sp,
                        color = LapisNavy
                    )
                    Text(
                        text = "Personal Folio",
                        fontFamily = InstrumentSerif,
                        fontSize = 20.sp,
                        color = InkSecondary,
                        letterSpacing = (-0.01).sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Toggle Romanization
                    Surface(
                        modifier = Modifier
                            .clickable { viewModel.toggleRomanUrdu() },
                        shape = RoundedCornerShape(10.dp),
                        color = if (showRoman) ParchmentSurfaceVariant else ParchmentSurface,
                        border = BorderStroke(1.dp, if (showRoman) LapisNavy else ParchmentBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FontDownload,
                                contentDescription = "Roman Urdu Toggle",
                                tint = if (showRoman) LapisNavy else InkSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Roman",
                                style = DiwanTypography.labelSmall,
                                fontWeight = if (showRoman) FontWeight.Bold else FontWeight.Normal,
                                color = if (showRoman) LapisNavy else InkSecondary
                            )
                        }
                    }

                    // Poet's Salon (Chat)
                    IconButton(
                        onClick = onNavigateToChat,
                        modifier = Modifier
                            .size(38.dp)
                            .background(ParchmentSurface, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Chat,
                            contentDescription = "Poet's Salon",
                            tint = InkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Settings
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .background(ParchmentSurface, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = InkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Poetry Carousel (Quiet, focused entirely on the verses)
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

        // Floating Calligraphy OCR Button (bottom-right)
        FloatingActionButton(
            onClick = onNavigateToScan,
            containerColor = LapisNavy,
            contentColor = ParchmentBackground,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.DocumentScanner,
                    contentDescription = "Scan Calligraphy",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan",
                    style = DiwanTypography.labelLarge,
                    fontWeight = FontWeight.Bold
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
