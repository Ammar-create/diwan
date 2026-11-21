package com.ammar.diwan.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ammar.diwan.data.api.OpenAiClient
import com.ammar.diwan.data.preferences.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentAct: Int = 0, // 0: The Calligraphic Reveal, 1: Script & Atmosphere, 2: The Scholar's Desk
    val themeIndex: Int = 0, // 0: System, 1: Parchment, 2: Nocturne
    val urduFontIndex: Int = 0, // 0: Noto Nastaliq, 1: Jameel Noori, 2: Gulzar
    val baseUrl: String = PreferencesManager.DEFAULT_BASE_URL,
    val apiKey: String = "",
    val chatModel: String = PreferencesManager.DEFAULT_CHAT_MODEL,
    val visionModel: String = PreferencesManager.DEFAULT_VISION_MODEL,
    val isVerifying: Boolean = false,
    val isVerified: Boolean = false,
    val errorMessage: String? = null,
    val sweepTrigger: Int = 0
)

class OnboardingViewModel(
    private val preferencesManager: PreferencesManager,
    private val openAiClient: OpenAiClient = OpenAiClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setAct(act: Int) {
        _uiState.update { it.copy(currentAct = act.coerceIn(0, 2), errorMessage = null) }
    }

    fun nextAct() {
        val next = (_uiState.value.currentAct + 1).coerceAtMost(2)
        setAct(next)
    }

    fun prevAct() {
        val prev = (_uiState.value.currentAct - 1).coerceAtLeast(0)
        setAct(prev)
    }

    fun setThemeIndex(index: Int) {
        _uiState.update { it.copy(themeIndex = index.coerceIn(0, 2)) }
    }

    fun setUrduFontIndex(index: Int) {
        _uiState.update { it.copy(urduFontIndex = index.coerceIn(0, 2)) }
    }

    fun updateBaseUrl(url: String) {
        _uiState.update { it.copy(baseUrl = url, errorMessage = null) }
    }

    fun updateApiKey(key: String) {
        _uiState.update { it.copy(apiKey = key, errorMessage = null) }
    }

    fun updateChatModel(model: String) {
        _uiState.update { it.copy(chatModel = model, errorMessage = null) }
    }

    fun getThemeKey(): String = when (_uiState.value.themeIndex) {
        1 -> "light"
        2 -> "dark"
        else -> "system"
    }

    fun getUrduFontKey(): String = when (_uiState.value.urduFontIndex) {
        1 -> "jameel_noori"
        2 -> "gulzar"
        else -> "noto_nastaliq"
    }

    fun verifyAndComplete(onDone: () -> Unit) {
        val state = _uiState.value
        if (state.apiKey.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter an API key or configure later") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, errorMessage = null) }
            val result = openAiClient.fetchModels(state.baseUrl, state.apiKey)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isVerifying = false,
                        isVerified = true,
                        sweepTrigger = it.sweepTrigger + 1
                    )
                }
                delay(450)
                saveAndFinish(onDone)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Verification failed"
                _uiState.update {
                    it.copy(
                        isVerifying = false,
                        isVerified = false,
                        errorMessage = err
                    )
                }
            }
        }
    }

    fun skipAndComplete(onDone: () -> Unit) {
        viewModelScope.launch {
            saveAndFinish(onDone)
        }
    }

    private suspend fun saveAndFinish(onDone: () -> Unit) {
        val s = _uiState.value
        val themeKey = getThemeKey()
        val urduKey = getUrduFontKey()
        preferencesManager.updateSettings(
            baseUrl = s.baseUrl,
            apiKey = s.apiKey,
            chatModel = s.chatModel,
            visionModel = s.visionModel,
            systemPrompt = PreferencesManager.DEFAULT_SYSTEM_PROMPT,
            temperature = 0.7f,
            themeMode = themeKey,
            englishFont = PreferencesManager.DEFAULT_ENGLISH_FONT,
            urduFont = urduKey
        )
        preferencesManager.saveSetup(
            baseUrl = s.baseUrl,
            apiKey = s.apiKey,
            chatModel = s.chatModel,
            visionModel = s.visionModel,
            systemPrompt = PreferencesManager.DEFAULT_SYSTEM_PROMPT
        )
        onDone()
    }
}
