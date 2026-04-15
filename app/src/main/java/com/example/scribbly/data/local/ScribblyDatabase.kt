package com.example.scribbly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class, LabelEntity::class, NoteLabelCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class ScribblyDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: ScribblyDatabase? = null

        fun getDatabase(context: Context): ScribblyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScribblyDatabase::class.java,
                    "scribbly_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
