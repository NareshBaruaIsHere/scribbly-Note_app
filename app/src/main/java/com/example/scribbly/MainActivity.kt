package com.example.scribbly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scribbly.data.preferences.ThemeMode
import com.example.scribbly.ui.AppViewModelProvider
import com.example.scribbly.ui.ScribblyNavHost
import com.example.scribbly.ui.settings.SettingsViewModel
import com.example.scribbly.ui.theme.ScribblyTheme

class MainActivity : ComponentActivity() {
    
    private val settingsViewModel: SettingsViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            ScribblyTheme(darkTheme = darkTheme) {
                ScribblyNavHost()
            }
        }
    }
}