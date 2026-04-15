package com.example.scribbly.data.repository

import android.content.Context
import android.net.Uri
import com.example.scribbly.data.local.LabelEntity
import com.example.scribbly.data.local.NoteDao
import com.example.scribbly.data.local.NoteEntity
import com.example.scribbly.data.local.NoteLabelCrossRef
import com.example.scribbly.data.local.NoteWithLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class NoteRepository(private val noteDao: NoteDao) {

    fun getAllActiveNotes(): Flow<List<NoteWithLabels>> = noteDao.getAllActiveNotes()
    fun getArchivedNotes(): Flow<List<NoteWithLabels>> = noteDao.getArchivedNotes()
    fun searchActiveNotes(query: String): Flow<List<NoteWithLabels>> = noteDao.searchActiveNotes(query)

    fun getNoteById(id: Long): Flow<NoteWithLabels?> = noteDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    
    suspend fun archiveNote(noteId: Long, isArchived: Boolean) {
        noteDao.updateArchivedState(
            noteId = noteId,
            isArchived = isArchived,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    suspend fun deleteNoteById(noteId: Long) = noteDao.deleteNoteById(noteId)

    suspend fun softDeleteNote(noteId: Long, isDeleted: Boolean = true) {
        noteDao.updateDeletedState(
            noteId = noteId,
            isDeleted = isDeleted,
            updatedAt = System.currentTimeMillis()
        )
    }

    // Export notes to JSON via SAF.
    suspend fun exportNotesToJson(context: Context, uri: Uri, notes: List<NoteWithLabels>) {
        withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("schemaVersion", BACKUP_SCHEMA_VERSION)
                put("notes", JSONArray().apply {
                    notes.forEach { noteWithLabels ->
                        put(
                            JSONObject().apply {
                                put("id", noteWithLabels.note.id)
                                put("title", noteWithLabels.note.title)
                                put("content", noteWithLabels.note.content)
                                put("createdAt", noteWithLabels.note.createdAt)
                                put("updatedAt", noteWithLabels.note.updatedAt)
                                put("isPinned", noteWithLabels.note.isPinned)
                                put("isArchived", noteWithLabels.note.isArchived)
                                put("isDeleted", noteWithLabels.note.isDeleted)
                                put("color", noteWithLabels.note.color)
                                put("checksum", noteWithLabels.note.checksum)
                                put("labels", JSONArray().apply {
                                    noteWithLabels.labels.forEach { label ->
                                        put(label.name)
                                    }
                                })
                            }
                        )
                    }
                })
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(payload.toString().toByteArray(Charsets.UTF_8))
            }
        }
    }

    suspend fun exportAllNotesToJson(context: Context, uri: Uri) {
        val notes = noteDao.getAllNotesForBackup()
        exportNotesToJson(context, uri, notes)
    }

    // Import notes from JSON via SAF.
    suspend fun importNotesFromJson(context: Context, uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    reader.readText()
                }

                if (jsonString.isNullOrEmpty()) return@withContext

                val root = JSONObject(jsonString)
                val notesArray = root.optJSONArray("notes") ?: JSONArray(jsonString)
                for (i in 0 until notesArray.length()) {
                    val obj = notesArray.getJSONObject(i)
                    val note = NoteEntity(
                        id = 0,
                        title = obj.optString("title", ""),
                        content = obj.optString("content", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        isPinned = obj.optBoolean("isPinned", false),
                        isArchived = obj.optBoolean("isArchived", false),
                        isDeleted = obj.optBoolean("isDeleted", false),
                        color = if (obj.has("color") && !obj.isNull("color")) obj.getInt("color") else null,
                        checksum = if (obj.has("checksum") && !obj.isNull("checksum")) obj.getString("checksum") else null
                    )
                    val insertedNoteId = noteDao.insertNote(note)

                    val labels = obj.optJSONArray("labels") ?: continue
                    for (labelIndex in 0 until labels.length()) {
                        val name = labels.optString(labelIndex).trim()
                        if (name.isEmpty()) continue

                        val labelId = ensureLabelId(name)
                        noteDao.insertNoteLabelCrossRef(
                            NoteLabelCrossRef(noteId = insertedNoteId, labelId = labelId)
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun ensureLabelId(name: String): Long {
        val insertedId = noteDao.insertLabel(LabelEntity(name = name))
        if (insertedId > 0L) return insertedId
        return requireNotNull(noteDao.getLabelIdByName(name))
    }

    private companion object {
        const val BACKUP_SCHEMA_VERSION = 1
    }
}
