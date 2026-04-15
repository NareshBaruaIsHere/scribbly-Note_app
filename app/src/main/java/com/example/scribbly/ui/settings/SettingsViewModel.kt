package com.example.scribbly.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scribbly.data.preferences.SettingsRepository
import com.example.scribbly.data.preferences.SortMode
import com.example.scribbly.data.preferences.ThemeMode
import com.example.scribbly.data.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    val sortMode: StateFlow<SortMode> = settingsRepository.sortModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SortMode.UPDATED_AT
    )

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun setSortMode(mode: SortMode) {
        viewModelScope.launch {
            settingsRepository.updateSortMode(mode)
        }
    }

    fun exportNotes(context: Context, uri: Uri) {
        viewModelScope.launch {
            noteRepository.exportAllNotesToJson(context, uri)
        }
    }

    fun importNotes(context: Context, uri: Uri) {
        viewModelScope.launch {
            noteRepository.importNotesFromJson(context, uri)
        }
    }
}
