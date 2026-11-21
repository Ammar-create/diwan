package com.ammar.diwan.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.ui.components.DiwanGroupedSection
import com.ammar.diwan.ui.components.DiwanHairline
import com.ammar.diwan.ui.components.DiwanKeyValueInputRow
import com.ammar.diwan.ui.components.DiwanNastaliqText
import com.ammar.diwan.ui.components.DiwanSegmentedControl
import com.ammar.diwan.ui.theme.DiwanDimens
import com.ammar.diwan.ui.theme.DiwanMotion
import com.ammar.diwan.ui.theme.DiwanTheme
import com.ammar.diwan.ui.theme.LocalEnglishFont
import com.ammar.diwan.ui.theme.LocalUrduFont
import com.ammar.diwan.ui.theme.ProvideDiwanTheme
import com.ammar.diwan.ui.theme.getEnglishFont
import com.ammar.diwan.ui.theme.getUrduFont
import com.ammar.diwan.ui.theme.inkBreath
import com.ammar.diwan.ui.theme.tactilePress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onRerunOnboarding: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = DiwanTheme.colors
    val typo = DiwanTheme.typography

    val currentUrduFont = getUrduFont(state.urduFont)
    val currentEnglishFont = getEnglishFont(state.englishFont)

    CompositionLocalProvider(
        LocalUrduFont provides currentUrduFont,
        LocalEnglishFont provides currentEnglishFont
    ) {
        ProvideDiwanTheme(
            typography = typo.copy(
                urduFamily = currentUrduFont,
                latinDisplayFamily = currentEnglishFont
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.canvas)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = DiwanDimens.MaxSettingsColumnWidth)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top App Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceGrouped)
                                .border(0.5.dp, colors.hairline, CircleShape)
                                .tactilePress(pressedScale = 0.92f, onClick = onNavigateBack)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "Settings",
                            style = typo.editorialTitle.copy(fontSize = 22.sp),
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceGrouped)
                                .border(0.5.dp, colors.hairline, CircleShape)
                                .tactilePress(pressedScale = 0.92f, onClick = viewModel::openLogs)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BugReport,
                                contentDescription = "Console Logs",
                                tint = colors.lapisInk,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // SECTION 1: APPEARANCE & TYPOGRAPHY
                    DiwanGroupedSection(title = "APPEARANCE & TYPOGRAPHY") {
                        // Live Serene Typography Preview Strip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.trackRecessed)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AnimatedContent(
                                    targetState = state.urduFont,
                                    transitionSpec = {
                                        (fadeIn(DiwanMotion.enterFade(180)) + scaleIn(initialScale = 0.98f))
                                            .togetherWith(fadeOut(DiwanMotion.exitFade(120)))
                                    },
                                    label = "settings_preview_couplet"
                                ) {
                                    DiwanNastaliqText(
                                        text = "ستاروں سے آگے جہاں اور بھی ہیں\nابھی عشق کے امتحان اور بھی ہیں",
                                        size = 18.sp,
                                        lineHeightMultiplier = 2.05f,
                                        color = colors.textPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                AnimatedContent(
                                    targetState = state.englishFont,
                                    transitionSpec = {
                                        fadeIn(DiwanMotion.enterFade(160)).togetherWith(fadeOut(DiwanMotion.exitFade(120)))
                                    },
                                    label = "settings_preview_translation"
                                ) {
                                    Text(
                                        text = "Beyond the stars there are worlds still more · Iqbal",
                                        style = typo.caption.copy(fontFamily = currentEnglishFont, fontSize = 12.sp),
                                        color = colors.textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        DiwanHairline(insetStart = 16.dp)

                        // Theme Mode Row
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                text = "Theme Atmosphere",
                                style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val themeOptions = listOf("System", "Parchment", "Nocturne")
                            val themeIndex = when (state.themeMode) {
                                "light" -> 1
                                "dark" -> 2
                                else -> 0
                            }
                            DiwanSegmentedControl(
                                options = themeOptions,
                                selectedIndex = themeIndex,
                                onOptionSelected = { idx ->
                                    val mode = when (idx) {
                                        1 -> "light"
                                        2 -> "dark"
                                        else -> "system"
                                    }
                                    viewModel.updateThemeMode(mode)
                                }
                            )
                        }

                        DiwanHairline(insetStart = 16.dp)

                        // Urdu Calligraphy Font Row
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                text = "Urdu Calligraphy Font",
                                style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val urduOptions = listOf("Noto", "Jameel", "Gulzar")
                            val urduIndex = when (state.urduFont) {
                                "jameel_noori" -> 1
                                "gulzar" -> 2
                                else -> 0
                            }
                            DiwanSegmentedControl(
                                options = urduOptions,
                                selectedIndex = urduIndex,
                                onOptionSelected = { idx ->
                                    val font = when (idx) {
                                        1 -> "jameel_noori"
                                        2 -> "gulzar"
                                        else -> "noto_nastaliq"
                                    }
                                    viewModel.updateUrduFont(font)
                                }
                            )
                        }

                        DiwanHairline(insetStart = 16.dp)

                        // English / Latin Display Font Row
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                text = "Latin Display Font",
                                style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val englishOptions = listOf("Instrument", "Google Sans", "Inter")
                            val englishIndex = when (state.englishFont) {
                                "google_sans" -> 1
                                "inter" -> 2
                                else -> 0
                            }
                            DiwanSegmentedControl(
                                options = englishOptions,
                                selectedIndex = englishIndex,
                                onOptionSelected = { idx ->
                                    val font = when (idx) {
                                        1 -> "google_sans"
                                        2 -> "inter"
                                        else -> "instrument"
                                    }
                                    viewModel.updateEnglishFont(font)
                                }
                            )
                        }

                        DiwanHairline(insetStart = 16.dp)

                        // Roman Urdu Transliteration Toggle Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = DiwanDimens.RowMinHeight)
                                .tactilePress(pressedScale = 0.98f) { viewModel.toggleShowRoman() }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Show Roman Urdu",
                                    style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Transliteration below verses",
                                    style = typo.caption,
                                    color = colors.textTertiary
                                )
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (state.showRoman) colors.lapisInk else colors.trackRecessed)
                                    .border(
                                        0.5.dp,
                                        if (state.showRoman) colors.lapisInk else colors.hairline,
                                        CircleShape
                                    )
                            ) {
                                if (state.showRoman) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Active",
                                        tint = colors.onLapisInk,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // SECTION 2: INTELLIGENCE ENGINE
                    DiwanGroupedSection(title = "INTELLIGENCE ENGINE") {
                        DiwanKeyValueInputRow(
                            label = "Base URL",
                            value = state.baseUrl,
                            onValueChange = viewModel::updateBaseUrl,
                            placeholder = "https://api.openai.com/v1"
                        )

                        DiwanHairline(insetStart = 16.dp)

                        DiwanKeyValueInputRow(
                            label = "API Key",
                            value = state.apiKey,
                            onValueChange = viewModel::updateApiKey,
                            placeholder = "sk-••••••••",
                            isSecret = true
                        )

                        DiwanHairline(insetStart = 16.dp)

                        DiwanKeyValueInputRow(
                            label = "Chat Model",
                            value = state.chatModel,
                            onValueChange = viewModel::updateChatModel,
                            placeholder = "deepseek-flash-1017"
                        )

                        DiwanHairline(insetStart = 16.dp)

                        DiwanKeyValueInputRow(
                            label = "Vision Model",
                            value = state.visionModel,
                            onValueChange = viewModel::updateVisionModel,
                            placeholder = "gemini-1.5-flash"
                        )

                        DiwanHairline(insetStart = 16.dp)

                        // Inline Apple-style Connection Verification Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = DiwanDimens.RowMinHeight)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Connection",
                                style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                color = colors.textPrimary
                            )

                            when (val status = state.verificationStatus) {
                                null -> {
                                    Text(
                                        text = "Verify Connection",
                                        style = typo.caption.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.lapisInk,
                                        modifier = Modifier
                                            .tactilePress(pressedScale = 0.94f) {
                                                viewModel.verifyConnection()
                                            }
                                            .padding(vertical = 4.dp, horizontal = 8.dp)
                                    )
                                }
                                is VerificationStatus.Verifying -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .inkBreath(active = true)
                                                .clip(CircleShape)
                                                .background(colors.lapisInk)
                                        )
                                        Text(
                                            text = "Verifying…",
                                            style = typo.caption.copy(fontWeight = FontWeight.Medium),
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                                is VerificationStatus.Success -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.tactilePress { viewModel.verifyConnection() }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(colors.emerald)
                                        )
                                        Text(
                                            text = "Connected · ${status.latencyMs}ms",
                                            style = typo.tabularMetric.copy(
                                                color = colors.emerald,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                                is VerificationStatus.Error -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.tactilePress { viewModel.verifyConnection() }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(colors.vermilion)
                                        )
                                        Text(
                                            text = "Auth Failed · Retry",
                                            style = typo.caption.copy(
                                                color = colors.vermilion,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // SECTION 3: ABOUT DIWAN
                    DiwanGroupedSection(title = "ABOUT DIWAN") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = DiwanDimens.RowMinHeight)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Architecture Version",
                                style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "1.0.0 · Wave 3 Surface",
                                style = typo.caption,
                                color = colors.textSecondary
                            )
                        }

                        DiwanHairline(insetStart = 16.dp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = DiwanDimens.RowMinHeight)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Rerun Opening Ritual",
                                    style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Re-calibrate 3-act setup wizard",
                                    style = typo.caption,
                                    color = colors.textTertiary
                                )
                            }
                            Text(
                                text = "Rerun",
                                style = typo.caption.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.lapisInk,
                                modifier = Modifier
                                    .tactilePress(pressedScale = 0.94f) {
                                        viewModel.resetOnboarding(onRerunOnboarding)
                                    }
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }

                // Debug Logger Bottom Sheet Modal
                if (state.showLogsDialog) {
                    val sheetState = rememberModalBottomSheetState()
                    ModalBottomSheet(
                        onDismissRequest = viewModel::closeLogs,
                        sheetState = sheetState,
                        containerColor = colors.surfaceArtifact,
                        contentColor = colors.textPrimary
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Diagnostics & Logs",
                                    style = typo.editorialTitle.copy(fontSize = 20.sp),
                                    color = colors.textPrimary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "Clear",
                                        style = typo.caption.copy(fontWeight = FontWeight.Medium),
                                        color = colors.vermilion,
                                        modifier = Modifier
                                            .tactilePress(pressedScale = 0.94f, onClick = viewModel::clearLogs)
                                            .padding(vertical = 4.dp, horizontal = 6.dp)
                                    )
                                    Text(
                                        text = "Done",
                                        style = typo.caption.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.lapisInk,
                                        modifier = Modifier
                                            .tactilePress(pressedScale = 0.94f, onClick = viewModel::closeLogs)
                                            .padding(vertical = 4.dp, horizontal = 6.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            if (state.logs.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No diagnostic events captured yet",
                                        style = typo.caption,
                                        color = colors.textTertiary
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 420.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.trackRecessed)
                                        .padding(12.dp)
                                ) {
                                    items(state.logs) { logLine ->
                                        Text(
                                            text = logLine,
                                            style = typo.monoTechnical.copy(fontSize = 11.sp, lineHeight = 16.sp),
                                            color = colors.textPrimary,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}
