package com.ammar.diwan.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ammar.diwan.data.api.OpenAiClient
import com.ammar.diwan.data.debug.DebugLogger
import com.ammar.diwan.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class VerificationStatus {
    data object Verifying : VerificationStatus()
    data class Success(val latencyMs: Long) : VerificationStatus()
    data class Error(val message: String) : VerificationStatus()
}

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
    val showRoman: Boolean = true,
    val verificationStatus: VerificationStatus? = null,
    val isSaved: Boolean = false,
    val showLogsDialog: Boolean = false,
    val logs: List<String> = emptyList()
)

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val openAiClient: OpenAiClient = OpenAiClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val baseUrl = preferencesManager.baseUrl.first()
            val apiKey = preferencesManager.apiKey.first()
            val chatModel = preferencesManager.chatModel.first()
            val visionModel = preferencesManager.visionModel.first()
            val systemPrompt = preferencesManager.systemPrompt.first()
            val temperature = preferencesManager.temperature.first()
            val themeMode = preferencesManager.themeMode.first()
            val englishFont = preferencesManager.englishFont.first()
            val urduFont = preferencesManager.urduFont.first()
            val showRoman = preferencesManager.showRoman.first()

            _uiState.update {
                it.copy(
                    baseUrl = baseUrl,
                    apiKey = apiKey,
                    chatModel = chatModel,
                    visionModel = visionModel,
                    systemPrompt = systemPrompt,
                    temperature = temperature,
                    themeMode = themeMode,
                    englishFont = englishFont,
                    urduFont = urduFont,
                    showRoman = showRoman
                )
            }
        }
    }

    fun updateBaseUrl(v: String) = _uiState.update { it.copy(baseUrl = v, isSaved = false, verificationStatus = null) }
    fun updateApiKey(v: String) = _uiState.update { it.copy(apiKey = v, isSaved = false, verificationStatus = null) }
    fun updateChatModel(v: String) = _uiState.update { it.copy(chatModel = v, isSaved = false) }
    fun updateVisionModel(v: String) = _uiState.update { it.copy(visionModel = v, isSaved = false) }
    fun updateSystemPrompt(v: String) = _uiState.update { it.copy(systemPrompt = v, isSaved = false) }
    fun updateTemperature(v: Float) = _uiState.update { it.copy(temperature = v, isSaved = false) }

    fun updateThemeMode(v: String) {
        _uiState.update { it.copy(themeMode = v, isSaved = false) }
        saveSettings()
    }

    fun updateUrduFont(v: String) {
        _uiState.update { it.copy(urduFont = v, isSaved = false) }
        saveSettings()
    }

    fun updateEnglishFont(v: String) {
        _uiState.update { it.copy(englishFont = v, isSaved = false) }
        saveSettings()
    }

    fun toggleShowRoman() {
        viewModelScope.launch {
            preferencesManager.toggleShowRoman()
            _uiState.update { it.copy(showRoman = !it.showRoman) }
        }
    }

    fun verifyConnection() {
        val state = _uiState.value
        if (state.apiKey.isBlank()) {
            _uiState.update { it.copy(verificationStatus = VerificationStatus.Error("API Key is missing")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(verificationStatus = VerificationStatus.Verifying) }
            val startTime = System.currentTimeMillis()
            val result = openAiClient.fetchModels(state.baseUrl, state.apiKey)
            val elapsed = System.currentTimeMillis() - startTime

            result.onSuccess {
                _uiState.update { it.copy(verificationStatus = VerificationStatus.Success(elapsed)) }
                DebugLogger.log("Settings", "Verification succeeded in ${elapsed}ms")
            }.onFailure { err ->
                val msg = err.message ?: "Authentication failed"
                _uiState.update { it.copy(verificationStatus = VerificationStatus.Error(msg)) }
                DebugLogger.log("Settings", "Verification failed: $msg")
            }
        }
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
            DebugLogger.log("Settings", "Settings saved successfully")
        }
    }

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

    fun resetOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            preferencesManager.resetSetup()
            onDone()
        }
    }
}
