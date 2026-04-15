package com.example.scribbly.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("UPDATE notes SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateArchivedState(noteId: Long, isArchived: Boolean, updatedAt: Long)

    @Query("UPDATE notes SET isDeleted = :isDeleted, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateDeletedState(noteId: Long, isDeleted: Boolean, updatedAt: Long)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Transaction
    @Query("""
        SELECT * FROM notes 
        WHERE isArchived = 0 AND isDeleted = 0 
        AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
    """)
    fun searchActiveNotes(query: String): Flow<List<NoteWithLabels>>

    @Transaction
    @Query("SELECT * FROM notes WHERE isArchived = 0 AND isDeleted = 0")
    fun getAllActiveNotes(): Flow<List<NoteWithLabels>>

    @Transaction
    @Query("SELECT * FROM notes WHERE isArchived = 1 AND isDeleted = 0")
    fun getArchivedNotes(): Flow<List<NoteWithLabels>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId: Long): Flow<NoteWithLabels?>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    suspend fun getNoteEntityById(noteId: Long): NoteEntity?

    @Transaction
    @Query("SELECT * FROM notes WHERE isDeleted = 0")
    suspend fun getAllNotesForBackup(): List<NoteWithLabels>

    // Label Operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabel(label: LabelEntity): Long

    @Query("SELECT id FROM labels WHERE name = :name LIMIT 1")
    suspend fun getLabelIdByName(name: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteLabelCrossRef(crossRef: NoteLabelCrossRef)
    
    @Query("DELETE FROM note_label_cross_ref WHERE noteId = :noteId")
    suspend fun deleteLabelsForNote(noteId: Long)
}
