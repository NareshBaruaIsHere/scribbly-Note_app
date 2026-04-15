package com.example.scribbly.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class SortMode {
    UPDATED_AT, CREATED_AT, TITLE_A_Z
}

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    
    val themeModeFlow: Flow<ThemeMode> = dataStore.data.map { preferences ->
        ThemeMode.valueOf(preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name)
    }

    val sortModeFlow: Flow<SortMode> = dataStore.data.map { preferences ->
        SortMode.valueOf(preferences[SORT_MODE_KEY] ?: SortMode.UPDATED_AT.name)
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    suspend fun updateSortMode(mode: SortMode) {
        dataStore.edit { preferences ->
            preferences[SORT_MODE_KEY] = mode.name
        }
    }

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val SORT_MODE_KEY = stringPreferencesKey("sort_mode")
    }
}
