package com.example.scribbly.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scribbly.data.local.NoteWithLabels
import com.example.scribbly.data.preferences.SettingsRepository
import com.example.scribbly.data.preferences.SortMode
import com.example.scribbly.data.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val notes: List<NoteWithLabels> = emptyList(),
    val searchQuery: String = "",
    val selectedNotes: Set<Long> = emptySet(),
    val isMultiSelectMode: Boolean = false
)

class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository,
    private val sortModeFlow: Flow<SortMode> = settingsRepository.sortModeFlow
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedNotes = MutableStateFlow<Set<Long>>(emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _notesFlow = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            noteRepository.getAllActiveNotes()
        } else {
            noteRepository.searchActiveNotes(query)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        _notesFlow,
        _searchQuery,
        _selectedNotes,
        sortModeFlow
    ) { notes, query, selected, sortMode ->
        val sortedNotes = when (sortMode) {
            SortMode.UPDATED_AT -> notes.sortedByDescending { it.note.updatedAt }
            SortMode.CREATED_AT -> notes.sortedByDescending { it.note.createdAt }
            SortMode.TITLE_A_Z -> notes.sortedBy { it.note.title.lowercase() }
        }.sortedByDescending { it.note.isPinned } // Always pinned first

        HomeUiState(
            notes = sortedNotes,
            searchQuery = query,
            selectedNotes = selected,
            isMultiSelectMode = selected.isNotEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleNoteSelection(noteId: Long) {
        _selectedNotes.update { current ->
            if (current.contains(noteId)) current - noteId else current + noteId
        }
    }

    fun clearSelection() {
        _selectedNotes.value = emptySet()
    }
    
    fun deleteSelectedNotes() {
        viewModelScope.launch {
            val selected = _selectedNotes.value
            val notesToDelete = uiState.value.notes.filter { it.note.id in selected }
            notesToDelete.forEach {
                noteRepository.softDeleteNote(it.note.id)
            }
            clearSelection()
        }
    }

    fun archiveSelectedNotes() {
        viewModelScope.launch {
            val selected = _selectedNotes.value
            val notesToArchive = uiState.value.notes.filter { it.note.id in selected }
            notesToArchive.forEach {
                noteRepository.archiveNote(it.note.id, isArchived = true)
            }
            clearSelection()
        }
    }

    fun pinSelectedNotes() {
        viewModelScope.launch {
            val selected = _selectedNotes.value
            val notesToPin = uiState.value.notes.filter { it.note.id in selected }
            // If all selected are pinned, unpin them. Else pin all.
            val allPinned = notesToPin.all { it.note.isPinned }
            notesToPin.forEach {
                noteRepository.updateNote(it.note.copy(isPinned = !allPinned, updatedAt = System.currentTimeMillis()))
            }
            clearSelection()
        }
    }
}
