package com.ammar.diwan.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
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

class OpenAiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

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
