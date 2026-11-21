package com.ammar.diwan.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ammar.diwan.data.debug.DebugLogger
import com.ammar.diwan.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "",
    val apiKey: String = "",
    val chatModel: String = "",
    val visionModel: String = "",
    val systemPrompt: String = "",
    val temperature: Float = 0.7f,
    val themeMode: String = "system",
    val englishFont: String = "instrument",
    val urduFont: String = "noto_nastaliq",
    val isSaved: Boolean = false,
    val showLogsDialog: Boolean = false,
    val logs: List<String> = emptyList()
)

class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    baseUrl = preferencesManager.baseUrl.first(),
                    apiKey = preferencesManager.apiKey.first(),
                    chatModel = preferencesManager.chatModel.first(),
                    visionModel = preferencesManager.visionModel.first(),
                    systemPrompt = preferencesManager.systemPrompt.first(),
                    temperature = preferencesManager.temperature.first(),
                    themeMode = preferencesManager.themeMode.first(),
                    englishFont = preferencesManager.englishFont.first(),
                    urduFont = preferencesManager.urduFont.first()
                )
            }
        }
    }

    fun updateBaseUrl(v: String) = _uiState.update { it.copy(baseUrl = v, isSaved = false) }
    fun updateApiKey(v: String) = _uiState.update { it.copy(apiKey = v, isSaved = false) }
    fun updateChatModel(v: String) = _uiState.update { it.copy(chatModel = v, isSaved = false) }
    fun updateVisionModel(v: String) = _uiState.update { it.copy(visionModel = v, isSaved = false) }
    fun updateSystemPrompt(v: String) = _uiState.update { it.copy(systemPrompt = v, isSaved = false) }
    fun updateTemperature(v: Float) = _uiState.update { it.copy(temperature = v, isSaved = false) }
    fun updateThemeMode(v: String) = _uiState.update { it.copy(themeMode = v, isSaved = false) }
    fun updateEnglishFont(v: String) = _uiState.update { it.copy(englishFont = v, isSaved = false) }
    fun updateUrduFont(v: String) = _uiState.update { it.copy(urduFont = v, isSaved = false) }

    fun openLogs() {
        _uiState.update { it.copy(showLogsDialog = true, logs = DebugLogger.logsFlow.value) }
    }

    fun closeLogs() {
        _uiState.update { it.copy(showLogsDialog = false) }
    }

    fun clearLogs() {
        DebugLogger.clear()
        _uiState.update { it.copy(logs = emptyList()) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            val s = _uiState.value
            preferencesManager.updateSettings(
                baseUrl = s.baseUrl,
                apiKey = s.apiKey,
                chatModel = s.chatModel,
                visionModel = s.visionModel,
                systemPrompt = s.systemPrompt,
                temperature = s.temperature,
                themeMode = s.themeMode,
                englishFont = s.englishFont,
                urduFont = s.urduFont
            )
            _uiState.update { it.copy(isSaved = true) }
            DebugLogger.log("Settings", "Settings updated successfully")
        }
    }

    fun resetOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            preferencesManager.resetSetup()
            onDone()
        }
    }
}
