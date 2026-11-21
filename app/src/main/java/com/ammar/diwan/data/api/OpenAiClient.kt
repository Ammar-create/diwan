package com.ammar.diwan.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

sealed class StreamChunk {
    data class Text(val delta: String) : StreamChunk()
    data class Reasoning(val delta: String) : StreamChunk()
    data object Done : StreamChunk()
}

data class OpenAiMessage(
    val role: String, // "system", "user", "assistant"
    val content: String,
    val images: List<String> = emptyList() // Base64 data URIs ("data:image/jpeg;base64,...") or HTTP URLs
)

class OpenAiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS) // Generous read timeout for deep LLM reasoning streams
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Server-Sent Events (SSE) chat streaming client.
     * Emits token deltas in real-time.
     * CRITICAL RULE: NEVER calls .trim() on deltas to prevent word welding (e.g. 'andshehada').
     */
    fun chatStream(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<OpenAiMessage>,
        temperature: Float = 0.7f
    ): Flow<StreamChunk> = flow {
        val url = baseUrl.trimEnd('/') + "/chat/completions"
        val payload = buildJsonObject {
            put("model", model)
            put("temperature", temperature)
            put("stream", true)
            put("messages", buildJsonArray {
                messages.forEach { msg ->
                    add(buildJsonObject {
                        put("role", msg.role)
                        if (msg.images.isEmpty()) {
                            put("content", msg.content)
                        } else {
                            put("content", buildJsonArray {
                                if (msg.content.isNotBlank()) {
                                    add(buildJsonObject {
                                        put("type", "text")
                                        put("text", msg.content)
                                    })
                                }
                                msg.images.forEach { img ->
                                    val formattedUrl = if (img.startsWith("data:") || img.startsWith("http://") || img.startsWith("https://")) {
                                        img
                                    } else {
                                        "data:image/jpeg;base64,$img"
                                    }
                                    add(buildJsonObject {
                                        put("type", "image_url")
                                        put("image_url", buildJsonObject {
                                            put("url", formattedUrl)
                                        })
                                    })
                                }
                            })
                        }
                    })
                }
            })
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .header("User-Agent", "Diwan/1.0 (Android)")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val call = client.newCall(request)
        try {
            val response = call.execute()
            response.use { resp ->
                if (!resp.isSuccessful) {
                    val errBody = runCatching { resp.body?.string() }.getOrNull().orEmpty()
                    throw Exception("HTTP ${resp.code}: $errBody")
                }
                val source = resp.body?.source() ?: throw Exception("Empty response body")
                var emittedDone = false

                while (!source.exhausted()) {
                    currentCoroutineContext().ensureActive()
                    val line = source.readUtf8Line() ?: break
                    if (line.isBlank() || line.startsWith(":")) continue

                    if (line.startsWith("data:")) {
                        val data = line.removePrefix("data:").trim()
                        if (data == "[DONE]") {
                            emit(StreamChunk.Done)
                            emittedDone = true
                            break
                        }

                        runCatching {
                            val root = json.parseToJsonElement(data).jsonObject
                            val choices = root["choices"]?.jsonArray
                            val firstChoice = choices?.firstOrNull()?.jsonObject
                            val delta = firstChoice?.get("delta")?.jsonObject

                            // 1. DeepSeek R1 / Reasoning API support (reasoning_content)
                            val reasoningElem = delta?.get("reasoning_content")
                            if (reasoningElem is JsonPrimitive && reasoningElem.isString) {
                                val reasoning = reasoningElem.content
                                if (reasoning.isNotEmpty()) {
                                    emit(StreamChunk.Reasoning(reasoning))
                                }
                            }

                            // 2. Standard content tokens - emit verbatim without trimming
                            val contentElem = delta?.get("content")
                            if (contentElem is JsonPrimitive && contentElem.isString) {
                                val content = contentElem.content
                                if (content.isNotEmpty()) {
                                    emit(StreamChunk.Text(content))
                                }
                            }
                        }
                    }
                }

                if (!emittedDone) {
                    emit(StreamChunk.Done)
                }
            }
        } finally {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun fetchModels(baseUrl: String, apiKey: String): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = baseUrl.trimEnd('/') + "/models"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $apiKey")
                .header("User-Agent", "Diwan/1.0 (Android)")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}: ${response.body?.string()}")
                }
                val body = response.body?.string() ?: throw Exception("Empty response")
                val root = json.parseToJsonElement(body).jsonObject
                val data = root["data"]?.jsonArray ?: emptyList()
                data.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content }
                    .sorted()
            }
        }
    }

    suspend fun sendChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String,
        temperature: Float = 0.7f
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val url = baseUrl.trimEnd('/') + "/chat/completions"
            val payload = buildJsonObject {
                put("model", model)
                put("temperature", temperature)
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .header("User-Agent", "Diwan/1.0 (Android)")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}: ${response.body?.string()}")
                }
                val body = response.body?.string() ?: throw Exception("Empty response")
                val root = json.parseToJsonElement(body).jsonObject
                val choices = root["choices"]?.jsonArray
                val message = choices?.firstOrNull()?.jsonObject?.get("message")?.jsonObject
                message?.get("content")?.jsonPrimitive?.content ?: ""
            }
        }
    }

    suspend fun extractPoemFromImage(
        baseUrl: String,
        apiKey: String,
        visionModel: String,
        base64JpegOrPng: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val url = baseUrl.trimEnd('/') + "/chat/completions"
            val prompt = """
                Analyze this calligraphy or image of Urdu poetry.
                Extract every couplet with highest precision.
                Return ONLY valid JSON in this exact structure without markdown backticks:
                {
                  "titleUrdu": "عنوان",
                  "titleEnglish": "English Title",
                  "poetUrdu": "شاعر",
                  "poetEnglish": "Poet Name",
                  "verses": [
                    {
                      "urdu": "اردو مصرع",
                      "roman": "Roman Urdu transliteration",
                      "english": "English translation"
                    }
                  ],
                  "explanationUrdu": "مفہوم و تشریح",
                  "explanationEnglish": "English explanation"
                }
            """.trimIndent()

            val dataUri = if (base64JpegOrPng.startsWith("data:")) {
                base64JpegOrPng
            } else {
                "data:image/png;base64,$base64JpegOrPng"
            }

            val payload = buildJsonObject {
                put("model", visionModel)
                put("temperature", 0.2)
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", buildJsonArray {
                            add(buildJsonObject {
                                put("type", "text")
                                put("text", prompt)
                            })
                            add(buildJsonObject {
                                put("type", "image_url")
                                put("image_url", buildJsonObject {
                                    put("url", dataUri)
                                })
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .header("User-Agent", "Diwan/1.0 (Android)")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}: ${response.body?.string()}")
                }
                val body = response.body?.string() ?: throw Exception("Empty response")
                val root = json.parseToJsonElement(body).jsonObject
                val choices = root["choices"]?.jsonArray
                val message = choices?.firstOrNull()?.jsonObject?.get("message")?.jsonObject
                message?.get("content")?.jsonPrimitive?.content ?: ""
            }
        }
    }
}
