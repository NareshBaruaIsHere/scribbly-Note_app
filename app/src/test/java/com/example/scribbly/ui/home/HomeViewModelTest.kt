package com.example.scribbly.ui.home

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.scribbly.data.local.LabelEntity
import com.example.scribbly.data.local.NoteDao
import com.example.scribbly.data.local.NoteEntity
import com.example.scribbly.data.local.NoteLabelCrossRef
import com.example.scribbly.data.local.NoteWithLabels
import com.example.scribbly.data.preferences.SettingsRepository
import com.example.scribbly.data.preferences.SortMode
import com.example.scribbly.data.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sortByTitle_keepsPinnedNotesFirst() = runTest(dispatcher) {
        val noteDao = FakeNoteDao(
            initialNotes = listOf(
                NoteEntity(id = 1, title = "Banana", content = "", createdAt = 10, updatedAt = 10),
                NoteEntity(id = 2, title = "Apple", content = "", createdAt = 20, updatedAt = 20),
                NoteEntity(id = 3, title = "Zoo", content = "", createdAt = 30, updatedAt = 30, isPinned = true)
            )
        )
        val settingsRepository = createSettingsRepository()
        val sortModeFlow = MutableStateFlow(SortMode.UPDATED_AT)
        val viewModel = HomeViewModel(NoteRepository(noteDao), settingsRepository, sortModeFlow)

        val collector = backgroundScope.launch { viewModel.uiState.collect { } }
        sortModeFlow.value = SortMode.TITLE_A_Z
        advanceUntilIdle()

        assertEquals(listOf(3L, 2L, 1L), viewModel.uiState.value.notes.map { it.note.id })
        collector.cancel()
    }

    @Test
    fun searchQuery_filtersVisibleNotes() = runTest(dispatcher) {
        val noteDao = FakeNoteDao(
            initialNotes = listOf(
                NoteEntity(id = 1, title = "Work", content = "Meeting notes", createdAt = 1, updatedAt = 1),
                NoteEntity(id = 2, title = "Home", content = "Grocery list", createdAt = 2, updatedAt = 2)
            )
        )
        val settingsRepository = createSettingsRepository()
        val sortModeFlow = MutableStateFlow(SortMode.UPDATED_AT)
        val viewModel = HomeViewModel(NoteRepository(noteDao), settingsRepository, sortModeFlow)

        val collector = backgroundScope.launch { viewModel.uiState.collect { } }
        viewModel.onSearchQueryChange("meet")
        advanceUntilIdle()

        assertEquals(listOf(1L), viewModel.uiState.value.notes.map { it.note.id })
        collector.cancel()
    }

    private fun createSettingsRepository(): SettingsRepository {
        val file = File.createTempFile("scribbly_test", ".preferences_pb")
        file.deleteOnExit()
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { file })
        return SettingsRepository(dataStore)
    }
}

private class FakeNoteDao(initialNotes: List<NoteEntity>) : NoteDao {
    private val notesFlow = MutableStateFlow(initialNotes)
    private val labelsByName = LinkedHashMap<String, Long>()
    private var nextNoteId = (initialNotes.maxOfOrNull { it.id } ?: 0L) + 1L
    private var nextLabelId = 1L

    override suspend fun insertNote(note: NoteEntity): Long {
        val id = if (note.id == 0L) nextNoteId++ else note.id
        notesFlow.value = notesFlow.value + note.copy(id = id)
        return id
    }

    override suspend fun updateNote(note: NoteEntity) {
        notesFlow.value = notesFlow.value.map { if (it.id == note.id) note else it }
    }

    override suspend fun updateArchivedState(noteId: Long, isArchived: Boolean, updatedAt: Long) {
        notesFlow.value = notesFlow.value.map {
            if (it.id == noteId) it.copy(isArchived = isArchived, updatedAt = updatedAt) else it
        }
    }

    override suspend fun updateDeletedState(noteId: Long, isDeleted: Boolean, updatedAt: Long) {
        notesFlow.value = notesFlow.value.map {
            if (it.id == noteId) it.copy(isDeleted = isDeleted, updatedAt = updatedAt) else it
        }
    }

    override suspend fun deleteNote(note: NoteEntity) {
        notesFlow.value = notesFlow.value.filterNot { it.id == note.id }
    }

    override suspend fun deleteNoteById(noteId: Long) {
        notesFlow.value = notesFlow.value.filterNot { it.id == noteId }
    }

    override fun searchActiveNotes(query: String): Flow<List<NoteWithLabels>> {
        val normalized = query.lowercase()
        return notesFlow.map { notes ->
            notes
                .asSequence()
                .filter { !it.isArchived && !it.isDeleted }
                .filter {
                    it.title.lowercase().contains(normalized) || it.content.lowercase().contains(normalized)
                }
                .map { NoteWithLabels(it, emptyList()) }
                .toList()
        }
    }

    override fun getAllActiveNotes(): Flow<List<NoteWithLabels>> {
        return notesFlow.map { notes ->
            notes
                .filter { !it.isArchived && !it.isDeleted }
                .map { NoteWithLabels(it, emptyList()) }
        }
    }

    override fun getArchivedNotes(): Flow<List<NoteWithLabels>> {
        return notesFlow.map { notes ->
            notes
                .filter { it.isArchived && !it.isDeleted }
                .map { NoteWithLabels(it, emptyList()) }
        }
    }

    override fun getNoteById(noteId: Long): Flow<NoteWithLabels?> {
        return notesFlow.map { notes ->
            notes.firstOrNull { it.id == noteId }?.let { NoteWithLabels(it, emptyList()) }
        }
    }

    override suspend fun getNoteEntityById(noteId: Long): NoteEntity? {
        return notesFlow.value.firstOrNull { it.id == noteId }
    }

    override suspend fun getAllNotesForBackup(): List<NoteWithLabels> {
        return notesFlow.value.filter { !it.isDeleted }.map { NoteWithLabels(it, emptyList()) }
    }

    override suspend fun insertLabel(label: LabelEntity): Long {
        val existing = labelsByName[label.name]
        if (existing != null) return -1L
        val id = nextLabelId++
        labelsByName[label.name] = id
        return id
    }

    override suspend fun getLabelIdByName(name: String): Long? = labelsByName[name]

    override suspend fun insertNoteLabelCrossRef(crossRef: NoteLabelCrossRef) {
        // No-op for this test.
    }

    override suspend fun deleteLabelsForNote(noteId: Long) {
        // No-op for this test.
    }
}


