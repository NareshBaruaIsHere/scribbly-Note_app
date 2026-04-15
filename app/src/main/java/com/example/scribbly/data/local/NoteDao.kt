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

    // Label Operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabel(label: LabelEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteLabelCrossRef(crossRef: NoteLabelCrossRef)
    
    @Query("DELETE FROM note_label_cross_ref WHERE noteId = :noteId")
    suspend fun deleteLabelsForNote(noteId: Long)
}
