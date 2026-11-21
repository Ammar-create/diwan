package com.ammar.diwan.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ammar.diwan.data.api.OpenAiClient
import com.ammar.diwan.data.models.ChatMessage
import com.ammar.diwan.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SalonUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            id = "welcome",
            role = "assistant",
            content = "خوش آمدید۔ فرمائیے، آج سخن فہمی کی کیسی محفل سجائیں؟ کس شاعر کا کلام یا کون سی بحر زیرِ بحث لائیں؟"
        )
    ),
    val inputText: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

class PoetSalonViewModel(
    private val preferencesManager: PreferencesManager,
    private val openAiClient: OpenAiClient = OpenAiClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalonUiState())
    val uiState: StateFlow<SalonUiState> = _uiState.asStateFlow()

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isSending) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = "user",
            content = text
        )

        _uiState.update { current ->
            current.copy(
                messages = current.messages + userMessage,
                inputText = "",
                isSending = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val baseUrl = preferencesManager.baseUrl.first()
            val apiKey = preferencesManager.apiKey.first()
            val chatModel = preferencesManager.chatModel.first()
            val systemPrompt = preferencesManager.systemPrompt.first()
            val temperature = preferencesManager.temperature.first()

            val result = openAiClient.sendChat(
                baseUrl = baseUrl,
                apiKey = apiKey,
                model = chatModel,
                systemPrompt = systemPrompt,
                userPrompt = text,
                temperature = temperature
            )

            result.onSuccess { reply ->
                val assistantMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = "assistant",
                    content = reply
                )
                _uiState.update { current ->
                    current.copy(
                        messages = current.messages + assistantMessage,
                        isSending = false
                    )
                }
            }.onFailure { err ->
                _uiState.update { current ->
                    current.copy(
                        isSending = false,
                        errorMessage = "Error: ${err.message}"
                    )
                }
            }
        }
    }
}
