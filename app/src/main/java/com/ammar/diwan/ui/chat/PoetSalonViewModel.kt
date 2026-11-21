package com.ammar.diwan.ui.chat

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ammar.diwan.data.api.OpenAiClient
import com.ammar.diwan.data.api.OpenAiMessage
import com.ammar.diwan.data.api.StreamChunk
import com.ammar.diwan.data.models.ChatMessage
import com.ammar.diwan.data.models.Poem
import com.ammar.diwan.data.models.Verse
import com.ammar.diwan.data.preferences.PreferencesManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID

data class SalonUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val stagedImages: List<Uri> = emptyList(),
    val clipboardImageUri: Uri? = null,
    val showClipboardSuggestion: Boolean = false,
    val errorMessage: String? = null
)

class PoetSalonViewModel(
    private val preferencesManager: PreferencesManager,
    private val openAiClient: OpenAiClient = OpenAiClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalonUiState())
    val uiState: StateFlow<SalonUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    private var activeJob: Job? = null

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun clearConversation() {
        stopStreaming()
        _uiState.update { it.copy(messages = emptyList(), errorMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun stopStreaming() {
        activeJob?.cancel()
        activeJob = null
        _uiState.update { current ->
            current.copy(
                isSending = false,
                messages = current.messages.map { msg ->
                    if (msg.isStreaming || msg.isThinking) {
                        msg.copy(isStreaming = false, isThinking = false)
                    } else msg
                }
            )
        }
    }

    // --- Attachment Staging & Clipboard Handling ---

    fun stageImage(uri: Uri) {
        _uiState.update { current ->
            if (current.stagedImages.contains(uri)) current
            else current.copy(stagedImages = current.stagedImages + uri)
        }
    }

    fun stageImages(uris: List<Uri>) {
        _uiState.update { current ->
            val updated = (current.stagedImages + uris).distinct()
            current.copy(stagedImages = updated)
        }
    }

    fun unstageImage(uri: Uri) {
        _uiState.update { current ->
            current.copy(stagedImages = current.stagedImages.filterNot { it == uri })
        }
    }

    fun clearStagedImages() {
        _uiState.update { it.copy(stagedImages = emptyList()) }
    }

    fun checkClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = clipboard.primaryClip ?: return
        if (clip.itemCount == 0) return

        val item = clip.getItemAt(0)
        val uri = item.uri
        if (uri != null) {
            val isAlreadyStaged = _uiState.value.stagedImages.contains(uri)
            if (!isAlreadyStaged) {
                _uiState.update { it.copy(clipboardImageUri = uri, showClipboardSuggestion = true) }
            }
            return
        }

        val text = item.text?.toString()?.trim() ?: return
        if (text.startsWith("content://") || text.startsWith("file://")) {
            val parsed = Uri.parse(text)
            if (!_uiState.value.stagedImages.contains(parsed)) {
                _uiState.update { it.copy(clipboardImageUri = parsed, showClipboardSuggestion = true) }
            }
            return
        }

        if (text.startsWith("data:image/") && text.contains("base64,")) {
            viewModelScope.launch {
                val tempUri = withContext(Dispatchers.IO) {
                    runCatching {
                        val b64 = text.substringAfter("base64,")
                        val bytes = Base64.decode(b64, Base64.DEFAULT)
                        val file = File(context.cacheDir, "clip_${System.currentTimeMillis()}.png")
                        FileOutputStream(file).use { it.write(bytes) }
                        Uri.fromFile(file)
                    }.getOrNull()
                }
                if (tempUri != null && !_uiState.value.stagedImages.contains(tempUri)) {
                    _uiState.update { it.copy(clipboardImageUri = tempUri, showClipboardSuggestion = true) }
                }
            }
        }
    }

    fun acceptClipboardImage() {
        val uri = _uiState.value.clipboardImageUri ?: return
        _uiState.update { current ->
            current.copy(
                stagedImages = if (current.stagedImages.contains(uri)) current.stagedImages else current.stagedImages + uri,
                showClipboardSuggestion = false,
                clipboardImageUri = null
            )
        }
    }

    fun dismissClipboardSuggestion() {
        _uiState.update { it.copy(showClipboardSuggestion = false) }
    }

    fun pasteFromClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = clipboard.primaryClip
        if (clip == null || clip.itemCount == 0) {
            _uiState.update { it.copy(errorMessage = "Clipboard is empty") }
            return
        }

        val item = clip.getItemAt(0)
        val uri = item.uri
        if (uri != null) {
            stageImage(uri)
            return
        }

        val text = item.text?.toString()?.trim()
        if (text != null) {
            if (text.startsWith("content://") || text.startsWith("file://")) {
                stageImage(Uri.parse(text))
                return
            }

            if (text.startsWith("data:image/") && text.contains("base64,")) {
                viewModelScope.launch {
                    val tempUri = withContext(Dispatchers.IO) {
                        runCatching {
                            val b64 = text.substringAfter("base64,")
                            val bytes = Base64.decode(b64, Base64.DEFAULT)
                            val file = File(context.cacheDir, "clip_${System.currentTimeMillis()}.png")
                            FileOutputStream(file).use { it.write(bytes) }
                            Uri.fromFile(file)
                        }.getOrNull()
                    }
                    if (tempUri != null) {
                        stageImage(tempUri)
                    } else {
                        _uiState.update { it.copy(errorMessage = "Could not decode clipboard image") }
                    }
                }
                return
            }

            if (text.startsWith("http://") || text.startsWith("https://")) {
                viewModelScope.launch {
                    val tempUri = withContext(Dispatchers.IO) {
                        runCatching {
                            val url = URL(text)
                            val bytes = url.readBytes()
                            val file = File(context.cacheDir, "clip_${System.currentTimeMillis()}.jpg")
                            FileOutputStream(file).use { it.write(bytes) }
                            Uri.fromFile(file)
                        }.getOrNull()
                    }
                    if (tempUri != null) {
                        stageImage(tempUri)
                    } else {
                        _uiState.update { it.copy(errorMessage = "Failed to load image from URL") }
                    }
                }
                return
            }
        }

        _uiState.update { it.copy(errorMessage = "No image found in clipboard") }
    }

    // --- Message Dispatching & 30Hz Coalesced Streaming ---

    fun sendPrompt(prompt: String, context: Context? = null) {
        _uiState.update { it.copy(inputText = prompt) }
        sendMessage(context)
    }

    fun sendMessage(context: Context? = null) {
        val text = _uiState.value.inputText.trim()
        val staged = _uiState.value.stagedImages
        if (text.isBlank() && staged.isEmpty()) return
        if (_uiState.value.isSending) return

        val userMessageId = UUID.randomUUID().toString()
        val assistantMessageId = UUID.randomUUID().toString()
        val stagedCopy = staged.toList()

        val userMessage = ChatMessage(
            id = userMessageId,
            role = "user",
            content = text,
            images = stagedCopy.map { it.toString() }
        )

        val assistantPlaceholder = ChatMessage(
            id = assistantMessageId,
            role = "assistant",
            content = "",
            isStreaming = true,
            isThinking = false
        )

        _uiState.update { current ->
            current.copy(
                messages = current.messages + userMessage + assistantPlaceholder,
                inputText = "",
                stagedImages = emptyList(),
                showClipboardSuggestion = false,
                isSending = true,
                errorMessage = null
            )
        }

        activeJob = viewModelScope.launch {
            val resolvedContext = context ?: preferencesManager.context
            val baseUrl = preferencesManager.baseUrl.first()
            val apiKey = preferencesManager.apiKey.first()
            val chatModel = preferencesManager.chatModel.first()
            val baseSystemPrompt = preferencesManager.systemPrompt.first()
            val temperature = preferencesManager.temperature.first()

            val base64Images = withContext(Dispatchers.IO) {
                stagedCopy.mapNotNull { uri ->
                    readUriAsBase64(resolvedContext, uri)
                }
            }

            val enhancedSystemPrompt = buildString {
                appendLine(baseSystemPrompt)
                appendLine()
                appendLine("When sharing, reciting, transcribing, or analyzing poetry, present verses in authentic Urdu Nastaliq script, accompanied by rhythmic Roman Urdu and English translation or commentary.")
                appendLine("Whenever a poem, ghazal, or couplet is featured or extracted, append an interactive Folio descriptor at the very end of your response in this exact format:")
                appendLine("```poem-folio")
                appendLine("{")
                appendLine("  \"titleUrdu\": \"عنوان\",")
                appendLine("  \"titleEnglish\": \"Title\",")
                appendLine("  \"poetUrdu\": \"شاعر\",")
                appendLine("  \"poetEnglish\": \"Poet\",")
                appendLine("  \"meter\": \"بحر\",")
                appendLine("  \"verses\": [")
                appendLine("    {")
                appendLine("      \"urdu\": \"پہلا مصرع\",")
                appendLine("      \"roman\": \"Pehla misra\",")
                appendLine("      \"english\": \"First hemistich\"")
                appendLine("    },")
                appendLine("    {")
                appendLine("      \"urdu\": \"دوسرا مصرع\",")
                appendLine("      \"roman\": \"Doosra misra\",")
                appendLine("      \"english\": \"Second hemistich\"")
                appendLine("    }")
                appendLine("  ],")
                appendLine("  \"explanationUrdu\": \"مفہوم\",")
                appendLine("  \"explanationEnglish\": \"Explanation\"")
                appendLine("}")
                appendLine("```")
            }

            val openAiMessages = buildList {
                add(OpenAiMessage(role = "system", content = enhancedSystemPrompt))
                // Context window: last 8 messages prior to this exchange
                val history = _uiState.value.messages.dropLast(2)
                history.takeLast(8).forEach { msg ->
                    add(OpenAiMessage(role = msg.role, content = msg.content, images = msg.images))
                }
                val promptText = if (text.isNotBlank()) text else "Please transcribe and analyze this calligraphy."
                add(OpenAiMessage(role = "user", content = promptText, images = base64Images))
            }

            val parser = StreamParser()
            var isDirty = false
            var isStreamFinished = false

            fun flushToState() {
                val cleanContent = parseDisplayContent(parser.rawContent.toString())
                val currentThinking = parser.rawReasoning.toString()
                val parsedPoem = if (isStreamFinished) {
                    extractPoem(parser.rawContent.toString())
                } else null

                val duration = if (parser.isThinking && parser.thinkingStartTime > 0L) {
                    System.currentTimeMillis() - parser.thinkingStartTime
                } else {
                    parser.thinkingDurationMs
                }

                _uiState.update { current ->
                    current.copy(
                        messages = current.messages.map { msg ->
                            if (msg.id == assistantMessageId) {
                                msg.copy(
                                    content = cleanContent,
                                    thinkingContent = currentThinking.ifEmpty { null },
                                    isThinking = parser.isThinking,
                                    thinkingDurationMs = duration,
                                    structuredPoem = parsedPoem ?: msg.structuredPoem,
                                    isStreaming = !isStreamFinished
                                )
                            } else msg
                        }
                    )
                }
                isDirty = false
            }

            // 30Hz Coalescing ticker: updates Compose StateFlow at ~33ms intervals, saving 50%+ layout passes
            val tickerJob = launch {
                while (isActive && !isStreamFinished) {
                    delay(33L)
                    if (isDirty || (parser.isThinking && parser.thinkingStartTime > 0L)) {
                        flushToState()
                    }
                }
            }

            try {
                openAiClient.chatStream(
                    baseUrl = baseUrl,
                    apiKey = apiKey,
                    model = chatModel,
                    messages = openAiMessages,
                    temperature = temperature
                ).collect { chunk ->
                    when (chunk) {
                        is StreamChunk.Reasoning -> {
                            parser.onReasoningChunk(chunk.delta)
                            isDirty = true
                        }
                        is StreamChunk.Text -> {
                            parser.onTextChunk(chunk.delta)
                            isDirty = true
                        }
                        is StreamChunk.Done -> {
                            // Handled after collect
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _uiState.update { current ->
                        current.copy(errorMessage = e.message ?: "Failed to generate response")
                    }
                }
            } finally {
                isStreamFinished = true
                parser.finish()
                tickerJob.cancel()
                flushToState()
                _uiState.update { it.copy(isSending = false) }
            }
        }
    }

    // --- Save to Folio ---

    fun savePoemToFolio(messageId: String, onSaved: ((Poem) -> Unit)? = null) {
        val message = _uiState.value.messages.find { it.id == messageId } ?: return
        val poem = message.structuredPoem ?: return

        _uiState.update { current ->
            current.copy(
                messages = current.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(isSavedToFolio = true)
                    } else msg
                }
            )
        }

        onSaved?.invoke(poem)
    }

    // --- Helpers: Bitmap Base64, Display Text & Poem Parsing ---

    private suspend fun readUriAsBase64(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val stream = context.contentResolver.openInputStream(uri)
                ?: uri.path?.let { File(it).takeIf { f -> f.exists() }?.inputStream() }
            stream?.use { s ->
                val bitmap = BitmapFactory.decodeStream(s) ?: return@use null
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
            }
        }.getOrNull()
    }

    private fun parseDisplayContent(raw: String): String {
        var text = raw
        val markers = listOf("```poem-folio", "```poem", "```json")
        for (marker in markers) {
            val start = text.indexOf(marker)
            if (start != -1) {
                val end = text.indexOf("```", start + marker.length)
                text = if (end != -1) {
                    text.substring(0, start) + text.substring(end + 3)
                } else {
                    text.substring(0, start)
                }
            }
        }
        return text.trimEnd()
    }

    private fun extractPoem(raw: String): Poem? {
        val markers = listOf("poem-folio", "poem", "json")
        for (tag in markers) {
            val marker = "```$tag"
            val start = raw.indexOf(marker)
            if (start != -1) {
                val end = raw.indexOf("```", start + marker.length)
                val jsonCandidate = if (end != -1) {
                    raw.substring(start + marker.length, end)
                } else {
                    raw.substring(start + marker.length)
                }.trim()

                val poem = parsePoemJson(jsonCandidate)
                if (poem != null) return poem
            }
        }

        val firstBrace = raw.indexOf('{')
        val lastBrace = raw.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace > firstBrace) {
            val jsonCandidate = raw.substring(firstBrace, lastBrace + 1)
            if (jsonCandidate.contains("titleUrdu") || jsonCandidate.contains("verses")) {
                val poem = parsePoemJson(jsonCandidate)
                if (poem != null) return poem
            }
        }

        return parsePoemHeuristic(raw)
    }

    private fun parsePoemJson(jsonStr: String): Poem? {
        return runCatching {
            val root = json.parseToJsonElement(jsonStr).jsonObject
            val titleUrdu = root["titleUrdu"]?.jsonPrimitive?.content ?: "کلام"
            val titleEnglish = root["titleEnglish"]?.jsonPrimitive?.content ?: ""
            val poetUrdu = root["poetUrdu"]?.jsonPrimitive?.content ?: "شاعر"
            val poetEnglish = root["poetEnglish"]?.jsonPrimitive?.content ?: ""
            val meter = root["meter"]?.jsonPrimitive?.content
            val explanationUrdu = root["explanationUrdu"]?.jsonPrimitive?.content
            val explanationEnglish = root["explanationEnglish"]?.jsonPrimitive?.content

            val versesArray = root["verses"]?.jsonArray ?: return null
            val verses = versesArray.mapNotNull { elem ->
                val vObj = elem.jsonObject
                val urdu = vObj["urdu"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val roman = vObj["roman"]?.jsonPrimitive?.content ?: ""
                val english = vObj["english"]?.jsonPrimitive?.content ?: ""
                Verse(urdu = urdu, roman = roman, english = english)
            }

            if (verses.isEmpty()) return null

            Poem(
                id = UUID.randomUUID().toString(),
                titleUrdu = titleUrdu,
                titleEnglish = titleEnglish,
                poetUrdu = poetUrdu,
                poetEnglish = poetEnglish,
                verses = verses,
                explanationUrdu = explanationUrdu,
                explanationEnglish = explanationEnglish,
                tags = listOf("Diwan Scholar", "Chat Extract"),
                meter = meter
            )
        }.getOrNull()
    }

    private fun parsePoemHeuristic(raw: String): Poem? {
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        val urduLines = lines.filter { line ->
            val urduCount = line.count { it in '\u0600'..'\u06FF' }
            urduCount >= 10 && !line.startsWith("```") && !line.startsWith("{") && !line.startsWith("}")
        }

        if (urduLines.size < 2) return null

        val verses = urduLines.chunked(2).map { chunk ->
            Verse(
                urdu = chunk.joinToString("\n"),
                roman = "",
                english = ""
            )
        }

        return Poem(
            id = UUID.randomUUID().toString(),
            titleUrdu = "منتخب اشعار",
            titleEnglish = "Selected Couplets",
            poetUrdu = "شاعر",
            poetEnglish = "Unknown Poet",
            verses = verses,
            tags = listOf("Diwan Scholar", "Extracted")
        )
    }

    private class StreamParser {
        val rawReasoning = StringBuilder()
        val rawContent = StringBuilder()
        private val pendingBuffer = StringBuilder()

        var inThinkTag = false
        var hasStartedThinking = false
        var thinkingStartTime = 0L
        var thinkingDurationMs = 0L
        var isThinking = false

        fun onReasoningChunk(delta: String) {
            if (!hasStartedThinking) {
                hasStartedThinking = true
                isThinking = true
                thinkingStartTime = System.currentTimeMillis()
            }
            rawReasoning.append(delta)
        }

        fun onTextChunk(delta: String) {
            if (isThinking && !inThinkTag && rawReasoning.isNotEmpty()) {
                isThinking = false
                thinkingDurationMs = System.currentTimeMillis() - thinkingStartTime
            }

            pendingBuffer.append(delta)
            val combined = pendingBuffer.toString()
            pendingBuffer.clear()

            var cursor = 0
            while (cursor < combined.length) {
                if (!inThinkTag) {
                    val openTagIndex = combined.indexOf("<think>", cursor)
                    if (openTagIndex != -1) {
                        rawContent.append(combined.substring(cursor, openTagIndex))
                        cursor = openTagIndex + "<think>".length
                        inThinkTag = true
                        isThinking = true
                        if (thinkingStartTime == 0L) {
                            thinkingStartTime = System.currentTimeMillis()
                        }
                    } else {
                        val remaining = combined.substring(cursor)
                        val partialIndex = findPartialOpenTag(remaining)
                        if (partialIndex != -1) {
                            rawContent.append(remaining.substring(0, partialIndex))
                            pendingBuffer.append(remaining.substring(partialIndex))
                        } else {
                            rawContent.append(remaining)
                        }
                        cursor = combined.length
                    }
                } else {
                    val closeTagIndex = combined.indexOf("</think>", cursor)
                    if (closeTagIndex != -1) {
                        rawReasoning.append(combined.substring(cursor, closeTagIndex))
                        cursor = closeTagIndex + "</think>".length
                        inThinkTag = false
                        isThinking = false
                        thinkingDurationMs = System.currentTimeMillis() - thinkingStartTime
                    } else {
                        val remaining = combined.substring(cursor)
                        val partialIndex = findPartialCloseTag(remaining)
                        if (partialIndex != -1) {
                            rawReasoning.append(remaining.substring(0, partialIndex))
                            pendingBuffer.append(remaining.substring(partialIndex))
                        } else {
                            rawReasoning.append(remaining)
                        }
                        cursor = combined.length
                    }
                }
            }
        }

        private fun findPartialOpenTag(s: String): Int {
            val tag = "<think>"
            for (i in 1 until tag.length) {
                val sub = tag.substring(0, i)
                if (s.endsWith(sub)) {
                    return s.length - i
                }
            }
            return -1
        }

        private fun findPartialCloseTag(s: String): Int {
            val tag = "</think>"
            for (i in 1 until tag.length) {
                val sub = tag.substring(0, i)
                if (s.endsWith(sub)) {
                    return s.length - i
                }
            }
            return -1
        }

        fun finish() {
            if (pendingBuffer.isNotEmpty()) {
                if (inThinkTag) rawReasoning.append(pendingBuffer) else rawContent.append(pendingBuffer)
                pendingBuffer.clear()
            }
            if (isThinking) {
                isThinking = false
                if (thinkingStartTime > 0L && thinkingDurationMs == 0L) {
                    thinkingDurationMs = System.currentTimeMillis() - thinkingStartTime
                }
            }
        }
    }
}
