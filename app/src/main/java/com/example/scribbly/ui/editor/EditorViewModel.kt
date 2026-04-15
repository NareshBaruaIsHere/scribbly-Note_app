package com.example.scribbly.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scribbly.data.local.NoteEntity
import com.example.scribbly.data.repository.NoteRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorUiState(
    val id: Long? = null,
    val title: String = "",
    val content: String = "",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val isSaving: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

class EditorViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState

    // Track internal changes to trigger auto-save
    private val _saveTrigger = MutableStateFlow(0L)
    
    private var isInitialized = false

    init {
        setupAutoSave()
    }

    fun initNote(noteId: Long?) {
        if (isInitialized) return
        isInitialized = true
        
        if (noteId != null) {
            viewModelScope.launch {
                noteRepository.getNoteById(noteId).collect { noteWithLabels ->
                    noteWithLabels?.note?.let { note ->
                        _uiState.update { 
                            it.copy(
                                id = note.id,
                                title = note.title,
                                content = note.content,
                                isPinned = note.isPinned,
                                isArchived = note.isArchived,
                                isDeleted = note.isDeleted,
                                createdAt = note.createdAt
                            )
                        }
                    }
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
        triggerSave()
    }

    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(content = newContent) }
        triggerSave()
    }

    fun togglePin() {
        _uiState.update { it.copy(isPinned = !it.isPinned) }
        triggerSave()
    }

    fun toggleArchive() {
        _uiState.update { it.copy(isArchived = !it.isArchived) }
        triggerSave()
    }

    fun markDeleted() {
        _uiState.update { it.copy(isDeleted = true) }
        forceSave()
    }

    private fun triggerSave() {
        _saveTrigger.value = System.currentTimeMillis()
    }

    @OptIn(FlowPreview::class)
    private fun setupAutoSave() {
        _saveTrigger
            .debounce(800) // 800ms debounce
            .onEach { time ->
                if (time > 0L) {
                    saveNote()
                }
            }
            .launchIn(viewModelScope)
    }

    fun forceSave() {
        viewModelScope.launch {
            saveNote()
        }
    }

    private suspend fun saveNote() {
        val currentState = _uiState.value
        
        // Auto-generate title from first line if empty and we have content
        val finalTitle = if (currentState.title.isBlank() && currentState.content.isNotBlank()) {
            currentState.content.lines().firstOrNull { it.isNotBlank() }?.take(30) ?: ""
        } else {
            currentState.title
        }

        if (finalTitle.isBlank() && currentState.content.isBlank()) {
            // Don't save completely empty notes
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        val note = NoteEntity(
            id = currentState.id ?: 0,
            title = finalTitle,
            content = currentState.content,
            createdAt = currentState.createdAt,
            updatedAt = System.currentTimeMillis(),
            isPinned = currentState.isPinned,
            isArchived = currentState.isArchived,
            isDeleted = currentState.isDeleted
        )

        if (currentState.id == null) {
            val newId = noteRepository.insertNote(note)
            _uiState.update { it.copy(id = newId) }
        } else {
            noteRepository.updateNote(note)
        }
        
        // Update title in state if it was auto-generated
        if (finalTitle != currentState.title) {
           _uiState.update { it.copy(title = finalTitle) }
        }

        _uiState.update { it.copy(isSaving = false) }
    }
}
