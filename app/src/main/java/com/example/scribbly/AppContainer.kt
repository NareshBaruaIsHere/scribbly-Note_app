package com.example.scribbly

import android.content.Context
import com.example.scribbly.data.local.ScribblyDatabase
import com.example.scribbly.data.preferences.SettingsRepository
import com.example.scribbly.data.preferences.dataStore
import com.example.scribbly.data.repository.NoteRepository

interface AppContainer {
    val noteRepository: NoteRepository
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: ScribblyDatabase by lazy {
        ScribblyDatabase.getDatabase(context)
    }

    override val noteRepository: NoteRepository by lazy {
        NoteRepository(database.noteDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context.dataStore)
    }
}
