package com.ammar.diwan.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Verse(
    val urdu: String,
    val roman: String,
    val english: String
)

@Serializable
data class Poem(
    val id: String,
    val titleUrdu: String,
    val titleEnglish: String,
    val poetUrdu: String,
    val poetEnglish: String,
    val verses: List<Verse>,
    val explanationUrdu: String? = null,
    val explanationEnglish: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false
)

@Serializable
data class ChatMessage(
    val id: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
