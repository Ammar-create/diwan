package com.ammar.diwan.ui.ocr

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ammar.diwan.data.api.OpenAiClient
import com.ammar.diwan.data.models.Poem
import com.ammar.diwan.data.models.Verse
import com.ammar.diwan.data.preferences.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
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

data class ScanUiState(
    val imageUri: Uri? = null,
    val isScanning: Boolean = false,
    val extractedPoem: Poem? = null,
    val rawJson: String? = null,
    val statusToast: String? = null,
    val errorMessage: String? = null
)

class CalligraphyScanViewModel(
    private val preferencesManager: PreferencesManager,
    private val openAiClient: OpenAiClient = OpenAiClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    fun setImageUri(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri, errorMessage = null, extractedPoem = null, statusToast = null) }
    }

    fun pasteFromClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip == null || clip.itemCount == 0) {
            _uiState.update { it.copy(errorMessage = "Clipboard is empty") }
            return
        }

        val item = clip.getItemAt(0)
        val uri = item.uri

        if (uri != null) {
            setImageUri(uri)
            _uiState.update { it.copy(statusToast = "Image pasted from clipboard") }
            return
        }

        val text = item.text?.toString()?.trim()
        if (text != null) {
            if (text.startsWith("content://") || text.startsWith("file://")) {
                setImageUri(Uri.parse(text))
                _uiState.update { it.copy(statusToast = "Image pasted from clipboard") }
                return
            }

            if (text.startsWith("data:image/") && text.contains("base64,")) {
                // Decode base64 to temp file
                viewModelScope.launch {
                    val tempUri = withContext(Dispatchers.IO) {
                        runCatching {
                            val b64 = text.substringAfter("base64,")
                            val bytes = Base64.decode(b64, Base64.DEFAULT)
                            val file = File(context.cacheDir, "clipboard_${System.currentTimeMillis()}.png")
                            FileOutputStream(file).use { it.write(bytes) }
                            Uri.fromFile(file)
                        }.getOrNull()
                    }
                    if (tempUri != null) {
                        setImageUri(tempUri)
                        _uiState.update { it.copy(statusToast = "Image pasted from clipboard") }
                    } else {
                        _uiState.update { it.copy(errorMessage = "Could not decode clipboard image data") }
                    }
                }
                return
            }

            if (text.startsWith("http://") || text.startsWith("https://")) {
                viewModelScope.launch {
                    val downloadedUri = withContext(Dispatchers.IO) {
                        runCatching {
                            val url = URL(text)
                            val bytes = url.readBytes()
                            val file = File(context.cacheDir, "clipboard_${System.currentTimeMillis()}.jpg")
                            FileOutputStream(file).use { it.write(bytes) }
                            Uri.fromFile(file)
                        }.getOrNull()
                    }
                    if (downloadedUri != null) {
                        setImageUri(downloadedUri)
                        _uiState.update { it.copy(statusToast = "Image loaded from clipboard URL") }
                    } else {
                        _uiState.update { it.copy(errorMessage = "Failed to load image from URL") }
                    }
                }
                return
            }
        }

        _uiState.update { it.copy(errorMessage = "No image found in clipboard") }
    }

    fun scanImage(context: Context) {
        val uri = _uiState.value.imageUri ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, errorMessage = null) }

            val base64 = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        val out = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                    }
                }.getOrNull()
            }

            if (base64 == null) {
                _uiState.update { it.copy(isScanning = false, errorMessage = "Failed to process image file") }
                return@launch
            }

            val baseUrl = preferencesManager.baseUrl.first()
            val apiKey = preferencesManager.apiKey.first()
            val visionModel = preferencesManager.visionModel.first()

            val result = openAiClient.extractPoemFromImage(
                baseUrl = baseUrl,
                apiKey = apiKey,
                visionModel = visionModel,
                base64JpegOrPng = base64
            )

            result.onSuccess { rawResponse ->
                val parsed = parsePoemJson(rawResponse)
                if (parsed != null) {
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            extractedPoem = parsed,
                            rawJson = rawResponse
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            errorMessage = "Model output could not be parsed into verses:\n$rawResponse"
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        errorMessage = "Scan failed: ${error.message}"
                    )
                }
            }
        }
    }

    private fun parsePoemJson(text: String): Poem? {
        return runCatching {
            val clean = text.replace("```json", "").replace("```", "").trim()
            val root = json.parseToJsonElement(clean).jsonObject
            val titleUrdu = root["titleUrdu"]?.jsonPrimitive?.content ?: "عنوان"
            val titleEnglish = root["titleEnglish"]?.jsonPrimitive?.content ?: "Untitled"
            val poetUrdu = root["poetUrdu"]?.jsonPrimitive?.content ?: "شاعر"
            val poetEnglish = root["poetEnglish"]?.jsonPrimitive?.content ?: "Unknown Poet"
            val versesArray = root["verses"]?.jsonArray ?: return null

            val verses = versesArray.map { item ->
                val obj = item.jsonObject
                Verse(
                    urdu = obj["urdu"]?.jsonPrimitive?.content ?: "",
                    roman = obj["roman"]?.jsonPrimitive?.content ?: "",
                    english = obj["english"]?.jsonPrimitive?.content ?: ""
                )
            }

            val explanationUrdu = root["explanationUrdu"]?.jsonPrimitive?.content
            val explanationEnglish = root["explanationEnglish"]?.jsonPrimitive?.content

            Poem(
                id = UUID.randomUUID().toString(),
                titleUrdu = titleUrdu,
                titleEnglish = titleEnglish,
                poetUrdu = poetUrdu,
                poetEnglish = poetEnglish,
                verses = verses,
                explanationUrdu = explanationUrdu,
                explanationEnglish = explanationEnglish,
                tags = listOf("Scanned", "Calligraphy")
            )
        }.getOrNull()
    }

    fun clearToast() {
        _uiState.update { it.copy(statusToast = null) }
    }
}
