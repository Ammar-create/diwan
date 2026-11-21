package com.ammar.diwan.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Verse(
    val urdu: String,
    val roman: String = "",
    val english: String = ""
)

@Serializable
data class Poem(
    val id: String,
    val titleUrdu: String,
    val titleEnglish: String = "",
    val poetUrdu: String,
    val poetEnglish: String = "",
    val verses: List<Verse>,
    val explanationUrdu: String? = null,
    val explanationEnglish: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val meter: String? = null // Bahr / Meter e.g. "Ghazal · Hazaj" or "بحرِ ہزج"
)

@Serializable
data class ChatMessage(
    val id: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val images: List<String> = emptyList(), // URI strings or base64 data URIs
    val thinkingContent: String? = null,    // Reasoning tokens extracted from <think>...</think> or reasoning_content
    val isThinking: Boolean = false,        // True while thinking/reasoning is in progress
    val thinkingDurationMs: Long = 0L,      // Elapsed duration of thinking in ms
    val structuredPoem: Poem? = null,       // Parsed structured poem for "Add to Folio" card artifact
    val isSavedToFolio: Boolean = false,    // True after user taps "Save to My Folio"
    val isStreaming: Boolean = false,       // True while actively streaming tokens
    val timestamp: Long = System.currentTimeMillis()
)
