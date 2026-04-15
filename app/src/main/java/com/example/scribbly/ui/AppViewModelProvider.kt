package com.example.scribbly.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scribbly.ScribblyApp
import com.example.scribbly.ui.editor.EditorViewModel
import com.example.scribbly.ui.home.HomeViewModel
import com.example.scribbly.ui.settings.SettingsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = (this[AndroidViewModelFactory.APPLICATION_KEY] as ScribblyApp)
            HomeViewModel(
                app.container.noteRepository,
                app.container.settingsRepository
            )
        }
        initializer {
            val app = (this[AndroidViewModelFactory.APPLICATION_KEY] as ScribblyApp)
            EditorViewModel(
                app.container.noteRepository
            )
        }
        initializer {
            val app = (this[AndroidViewModelFactory.APPLICATION_KEY] as ScribblyApp)
            SettingsViewModel(
                app.container.settingsRepository,
                app.container.noteRepository
            )
        }
    }
}
