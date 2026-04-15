package com.example.scribbly.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scribbly.ScribblyApp
import com.example.scribbly.data.local.NoteWithLabels
import com.example.scribbly.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArchiveViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _selectedNotes = MutableStateFlow<Set<Long>>(emptySet())
    
    val archiveState = combine(
        noteRepository.getArchivedNotes(),
        _selectedNotes
    ) { notes, selected ->
        ArchiveUiState(
            notes = notes,
            selectedNotes = selected,
            isMultiSelectMode = selected.isNotEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArchiveUiState())

    fun toggleSelection(id: Long) {
        _selectedNotes.update { current ->
            if (current.contains(id)) current - id else current + id
        }
    }

    fun clearSelection() {
        _selectedNotes.value = emptySet()
    }

    fun unarchiveSelected() {
        viewModelScope.launch {
            val selected = _selectedNotes.value
            val toUnarchive = archiveState.value.notes.filter { it.note.id in selected }
            toUnarchive.forEach {
                noteRepository.archiveNote(it.note.id, isArchived = false)
            }
            clearSelection()
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val selected = _selectedNotes.value
            val toDelete = archiveState.value.notes.filter { it.note.id in selected }
            toDelete.forEach {
                noteRepository.softDeleteNote(it.note.id)
            }
            clearSelection()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = (this[AndroidViewModelFactory.APPLICATION_KEY] as ScribblyApp)
                ArchiveViewModel(app.container.noteRepository)
            }
        }
    }
}

data class ArchiveUiState(
    val notes: List<NoteWithLabels> = emptyList(),
    val selectedNotes: Set<Long> = emptySet(),
    val isMultiSelectMode: Boolean = false
)
