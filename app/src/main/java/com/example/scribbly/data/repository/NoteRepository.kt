package com.example.scribbly.data.repository

import android.content.Context
import android.net.Uri
import com.example.scribbly.data.local.NoteDao
import com.example.scribbly.data.local.NoteEntity
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
        // We do a simple fetch/update for now, ideally in a transaction or specific query
        // But since we are updating one field, let's fetch first.
    }
    
    suspend fun deleteNoteById(noteId: Long) = noteDao.deleteNoteById(noteId)

    // Export Notes to JSON via SAF
    suspend fun exportNotesToJson(context: Context, uri: Uri, notes: List<NoteWithLabels>) {
        withContext(Dispatchers.IO) {
            try {
                val jsonArray = JSONArray()
                for (noteWithLabels in notes) {
                    val noteObj = JSONObject().apply {
                        put("id", noteWithLabels.note.id)
                        put("title", noteWithLabels.note.title)
                        put("content", noteWithLabels.note.content)
                        put("createdAt", noteWithLabels.note.createdAt)
                        put("updatedAt", noteWithLabels.note.updatedAt)
                        put("isPinned", noteWithLabels.note.isPinned)
                        put("isArchived", noteWithLabels.note.isArchived)
                        put("color", noteWithLabels.note.color)
                    }
                    jsonArray.put(noteObj)
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonArray.toString().toByteArray(Charsets.UTF_8))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Import Notes from JSON via SAF
    suspend fun importNotesFromJson(context: Context, uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    reader.readText()
                }

                if (!jsonString.isNullOrEmpty()) {
                    val jsonArray = JSONArray(jsonString)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val note = NoteEntity(
                            id = 0, // Generate new ID on import to avoid conflicts
                            title = obj.getString("title"),
                            content = obj.getString("content"),
                            createdAt = obj.getLong("createdAt"),
                            updatedAt = obj.getLong("updatedAt"),
                            isPinned = obj.optBoolean("isPinned", false),
                            isArchived = obj.optBoolean("isArchived", false),
                            isDeleted = false,
                            color = if (obj.has("color") && !obj.isNull("color")) obj.getInt("color") else null
                        )
                        noteDao.insertNote(note)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
