package com.ammar.diwan.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.ui.onboarding.FluidButton
import com.ammar.diwan.ui.theme.DiwanTypography
import com.ammar.diwan.ui.theme.FoilGold
import com.ammar.diwan.ui.theme.InkMuted
import com.ammar.diwan.ui.theme.InkPrimary
import com.ammar.diwan.ui.theme.InkSecondary
import com.ammar.diwan.ui.theme.InstrumentSerif
import com.ammar.diwan.ui.theme.LapisNavy
import com.ammar.diwan.ui.theme.ParchmentBackground
import com.ammar.diwan.ui.theme.ParchmentBorder
import com.ammar.diwan.ui.theme.ParchmentSurface

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onRerunOnboarding: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ParchmentBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
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
                Text(
                    text = "Settings & Persona",
                    fontFamily = InstrumentSerif,
                    fontSize = 24.sp,
                    color = InkPrimary
                )
            }

            // Fields
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = viewModel::updateBaseUrl,
                label = { Text("Base URL", color = InkSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = diwanColors()
            )

            OutlinedTextField(
                value = state.apiKey,
                onValueChange = viewModel::updateApiKey,
                label = { Text("API Key", color = InkSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = diwanColors()
            )

            OutlinedTextField(
                value = state.chatModel,
                onValueChange = viewModel::updateChatModel,
                label = { Text("Chat Model", color = InkSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = diwanColors()
            )

            OutlinedTextField(
                value = state.visionModel,
                onValueChange = viewModel::updateVisionModel,
                label = { Text("Vision / OCR Model", color = InkSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = diwanColors()
            )

            OutlinedTextField(
                value = state.systemPrompt,
                onValueChange = viewModel::updateSystemPrompt,
                label = { Text("Poet Companion Prompt", color = InkSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(12.dp),
                colors = diwanColors()
            )

            // Temperature Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Creativity (Temperature)",
                        style = DiwanTypography.labelSmall,
                        color = InkSecondary
                    )
                    Text(
                        text = "%.2f".format(state.temperature),
                        style = DiwanTypography.labelSmall,
                        color = LapisNavy
                    )
                }
                Slider(
                    value = state.temperature,
                    onValueChange = viewModel::updateTemperature,
                    valueRange = 0.0f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = LapisNavy,
                        activeTrackColor = LapisNavy,
                        inactiveTrackColor = ParchmentBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FluidButton(
                text = if (state.isSaved) "Settings Saved!" else "Save Settings",
                icon = Icons.Rounded.Check,
                onClick = viewModel::saveSettings
            )

            OutlinedButton(
                onClick = { viewModel.resetOnboarding(onRerunOnboarding) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ParchmentBorder)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        tint = InkSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Re-run Onboarding Wizard", color = InkSecondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun diwanColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LapisNavy,
    unfocusedBorderColor = ParchmentBorder,
    focusedTextColor = InkPrimary,
    unfocusedTextColor = InkPrimary,
    focusedContainerColor = ParchmentSurface,
    unfocusedContainerColor = ParchmentSurface
)
