package com.ammar.diwan.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "diwan_settings")

class PreferencesManager(val context: Context) {

    companion object {
        val KEY_SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        val KEY_BASE_URL = stringPreferencesKey("base_url")
        val KEY_API_KEY = stringPreferencesKey("api_key")
        val KEY_CHAT_MODEL = stringPreferencesKey("chat_model")
        val KEY_VISION_MODEL = stringPreferencesKey("vision_model")
        val KEY_SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val KEY_TEMPERATURE = floatPreferencesKey("temperature")
        val KEY_SHOW_ROMAN = booleanPreferencesKey("show_roman")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "system", "dark", "light"
        val KEY_ENGLISH_FONT = stringPreferencesKey("english_font") // "instrument", "google_sans", "comic", "inter"
        val KEY_URDU_FONT = stringPreferencesKey("urdu_font") // "noto_nastaliq", "jameel_noori", "gulzar"

        const val DEFAULT_BASE_URL = "https://api.aquadevs.com/v1"
        const val DEFAULT_CHAT_MODEL = "deepseek-flash-1017"
        const val DEFAULT_VISION_MODEL = "gemini-1.5-flash"
        const val DEFAULT_THEME_MODE = "system"
        const val DEFAULT_ENGLISH_FONT = "instrument"
        const val DEFAULT_URDU_FONT = "noto_nastaliq"

        const val DEFAULT_SYSTEM_PROMPT = """You are a master scholar of classical Urdu poetry, aesthetics, and meter (bahr). You speak with reverence, deep literary knowledge, and eloquent charm. When asked, analyze verses with precision, illuminate Persian/Arabic roots, and provide both authentic Nastaliq text and rhythmic Roman Urdu transliteration."""
    }

    val isSetupCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SETUP_COMPLETED] ?: false
    }

    val baseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_BASE_URL] ?: DEFAULT_BASE_URL
    }

    val apiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_API_KEY] ?: ""
    }

    val chatModel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CHAT_MODEL] ?: DEFAULT_CHAT_MODEL
    }

    val visionModel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_VISION_MODEL] ?: DEFAULT_VISION_MODEL
    }

    val systemPrompt: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SYSTEM_PROMPT] ?: DEFAULT_SYSTEM_PROMPT
    }

    val temperature: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_TEMPERATURE] ?: 0.7f
    }

    val showRoman: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_ROMAN] ?: true
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: DEFAULT_THEME_MODE
    }

    val englishFont: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ENGLISH_FONT] ?: DEFAULT_ENGLISH_FONT
    }

    val urduFont: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_URDU_FONT] ?: DEFAULT_URDU_FONT
    }

    suspend fun saveSetup(
        baseUrl: String,
        apiKey: String,
        chatModel: String,
        visionModel: String,
        systemPrompt: String = DEFAULT_SYSTEM_PROMPT
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SETUP_COMPLETED] = true
            prefs[KEY_BASE_URL] = baseUrl
            prefs[KEY_API_KEY] = apiKey
            prefs[KEY_CHAT_MODEL] = chatModel
            prefs[KEY_VISION_MODEL] = visionModel
            prefs[KEY_SYSTEM_PROMPT] = systemPrompt
        }
    }

    suspend fun updateSettings(
        baseUrl: String,
        apiKey: String,
        chatModel: String,
        visionModel: String,
        systemPrompt: String,
        temperature: Float,
        themeMode: String,
        englishFont: String,
        urduFont: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = baseUrl
            prefs[KEY_API_KEY] = apiKey
            prefs[KEY_CHAT_MODEL] = chatModel
            prefs[KEY_VISION_MODEL] = visionModel
            prefs[KEY_SYSTEM_PROMPT] = systemPrompt
            prefs[KEY_TEMPERATURE] = temperature
            prefs[KEY_THEME_MODE] = themeMode
            prefs[KEY_ENGLISH_FONT] = englishFont
            prefs[KEY_URDU_FONT] = urduFont
        }
    }

    suspend fun toggleShowRoman() {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_SHOW_ROMAN] ?: true
            prefs[KEY_SHOW_ROMAN] = !current
        }
    }

    suspend fun resetSetup() {
        context.dataStore.edit { prefs ->
            prefs[KEY_SETUP_COMPLETED] = false
        }
    }
}
