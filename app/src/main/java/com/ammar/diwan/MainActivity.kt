package com.ammar.diwan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ammar.diwan.data.preferences.PreferencesManager
import com.ammar.diwan.ui.navigation.DiwanNavHost
import com.ammar.diwan.ui.theme.DiwanTheme
import com.ammar.diwan.ui.theme.ParchmentBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesManager = PreferencesManager(applicationContext)

        setContent {
            DiwanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ParchmentBackground
                ) {
                    DiwanNavHost(preferencesManager = preferencesManager)
                }
            }
        }
    }
}
