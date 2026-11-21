package com.ammar.diwan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ammar.diwan.data.debug.DebugLogger
import com.ammar.diwan.data.preferences.PreferencesManager
import com.ammar.diwan.ui.navigation.DiwanNavHost
import com.ammar.diwan.ui.theme.DiwanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        DebugLogger.init(applicationContext)
        val preferencesManager = PreferencesManager(applicationContext)

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState(initial = PreferencesManager.DEFAULT_THEME_MODE)
            val englishFont by preferencesManager.englishFont.collectAsState(initial = PreferencesManager.DEFAULT_ENGLISH_FONT)
            val urduFont by preferencesManager.urduFont.collectAsState(initial = PreferencesManager.DEFAULT_URDU_FONT)

            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            DiwanTheme(
                darkTheme = isDark,
                englishFontKey = englishFont,
                urduFontKey = urduFont
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiwanNavHost(preferencesManager = preferencesManager)
                }
            }
        }
    }
}
