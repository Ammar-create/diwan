package com.ammar.diwan.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ammar.diwan.data.api.OpenAiClient
import com.ammar.diwan.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep {
    Intro,
    ProviderEndpoint,
    ModelSelection,
    PoetPersona,
    Completed
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.Intro,
    val baseUrl: String = PreferencesManager.DEFAULT_BASE_URL,
    val apiKey: String = "",
    val chatModel: String = PreferencesManager.DEFAULT_CHAT_MODEL,
    val visionModel: String = PreferencesManager.DEFAULT_VISION_MODEL,
    val systemPrompt: String = PreferencesManager.DEFAULT_SYSTEM_PROMPT,
    val availableModels: List<String> = emptyList(),
    val isFetchingModels: Boolean = false,
    val isConnecting: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class OnboardingViewModel(
    private val preferencesManager: PreferencesManager,
    private val openAiClient: OpenAiClient = OpenAiClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateBaseUrl(url: String) {
        _uiState.update { it.copy(baseUrl = url, errorMessage = null) }
    }

    fun updateApiKey(key: String) {
        _uiState.update { it.copy(apiKey = key, errorMessage = null) }
    }

    fun updateChatModel(model: String) {
        _uiState.update { it.copy(chatModel = model) }
    }

    fun updateVisionModel(model: String) {
        _uiState.update { it.copy(visionModel = model) }
    }

    fun updateSystemPrompt(prompt: String) {
        _uiState.update { it.copy(systemPrompt = prompt) }
    }

    fun selectPreset(name: String) {
        when (name) {
            "Aqua" -> {
                _uiState.update {
                    it.copy(
                        baseUrl = "https://api.aquadevs.com/v1",
                        chatModel = "deepseek-flash-1017",
                        visionModel = "gemini-1.5-flash",
                        errorMessage = null
                    )
                }
            }
            "OpenRouter" -> {
                _uiState.update {
                    it.copy(
                        baseUrl = "https://openrouter.ai/api/v1",
                        chatModel = "deepseek/deepseek-chat",
                        visionModel = "google/gemini-flash-1.5",
                        errorMessage = null
                    )
                }
            }
            "OpenAI" -> {
                _uiState.update {
                    it.copy(
                        baseUrl = "https://api.openai.com/v1",
                        chatModel = "gpt-4o-mini",
                        visionModel = "gpt-4o-mini",
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun selectPersonaPreset(poet: String) {
        val prompt = when (poet) {
            "Iqbal" -> """You embody the spirit, philosophical depth, and spiritual passion of Allama Muhammad Iqbal. You speak in Urdu and English with grand intellectual dignity. You reflect on the resurgence of self (Khudi), fate, faith, and the yearning of the Muslim world. You quote your verses from Shikwa, Jawab-e-Shikwa, and Bal-e-Jibril when appropriate."""
            "Ghalib" -> """You embody the wit, irony, melancholic skepticism, and linguistic brilliance of Mirza Asadullah Khan Ghalib. You speak with delightful cheekiness, philosophical depth, and bittersweet longing. You love playful debate about human desires, unrequited love, and the paradoxes of existence."""
            "Jaun" -> """You embody the cynical, raw, anarchic, and unapologetically wounded voice of Jaun Elia. You talk about disillusionment, books, smoking, and the bitter comedy of human attachments with dry Lahore-Karachi intellectual cadence."""
            else -> PreferencesManager.DEFAULT_SYSTEM_PROMPT
        }
        _uiState.update { it.copy(systemPrompt = prompt) }
    }

    fun fetchModelsAndProceed() {
        val state = _uiState.value
        if (state.apiKey.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter your API key to connect") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingModels = true, errorMessage = null, statusMessage = "Testing connection...") }
            val result = openAiClient.fetchModels(state.baseUrl, state.apiKey)
            result.onSuccess { models ->
                _uiState.update {
                    it.copy(
                        isFetchingModels = false,
                        availableModels = models,
                        statusMessage = "Connected successfully (${models.size} models found)",
                        currentStep = OnboardingStep.ModelSelection
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isFetchingModels = false,
                        statusMessage = null,
                        errorMessage = "Connection failed: ${err.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun nextStep() {
        val next = when (_uiState.value.currentStep) {
            OnboardingStep.Intro -> OnboardingStep.ProviderEndpoint
            OnboardingStep.ProviderEndpoint -> OnboardingStep.ModelSelection
            OnboardingStep.ModelSelection -> OnboardingStep.PoetPersona
            OnboardingStep.PoetPersona -> OnboardingStep.Completed
            OnboardingStep.Completed -> OnboardingStep.Completed
        }
        _uiState.update { it.copy(currentStep = next, errorMessage = null) }
    }

    fun prevStep() {
        val prev = when (_uiState.value.currentStep) {
            OnboardingStep.Intro -> OnboardingStep.Intro
            OnboardingStep.ProviderEndpoint -> OnboardingStep.Intro
            OnboardingStep.ModelSelection -> OnboardingStep.ProviderEndpoint
            OnboardingStep.PoetPersona -> OnboardingStep.ModelSelection
            OnboardingStep.Completed -> OnboardingStep.PoetPersona
        }
        _uiState.update { it.copy(currentStep = prev, errorMessage = null) }
    }

    fun completeSetup(onDone: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            preferencesManager.saveSetup(
                baseUrl = state.baseUrl,
                apiKey = state.apiKey,
                chatModel = state.chatModel,
                visionModel = state.visionModel,
                systemPrompt = state.systemPrompt
            )
            onDone()
        }
    }
}
