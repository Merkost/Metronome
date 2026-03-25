package com.merkost.metronome.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.merkost.metronome.ui.theme.AppColorScheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppDatastoreImpl(private val dataStore: DataStore<Preferences>) : AppDatastore {

    companion object {
        private val TOTAL_TIME = longPreferencesKey("TOTAL_TIME")
        private val STEREO = intPreferencesKey("STEREO")
        private val COLOR_SCHEME = intPreferencesKey("COLOR_SCHEME")
        private val COLOR_FLASH = booleanPreferencesKey("COLOR_FLASH")
        private val BACKGROUND_PLAY = booleanPreferencesKey("BACKGROUND_PLAY")
    }

    override val colorScheme: Flow<AppColorScheme> = dataStore.data
        .map { preferences ->
            kotlin.runCatching { AppColorScheme.entries[preferences[COLOR_SCHEME] ?: 0] }
                .getOrDefault(AppColorScheme.BLACKNWHITE)
        }

    override val colorFlash = dataStore.data
        .map { preferences ->
            preferences[COLOR_FLASH] ?: false
        }
    override val backgroundPlay = dataStore.data
        .map { preferences ->
            preferences[BACKGROUND_PLAY] ?: false
        }
    override val stereo = dataStore.data
        .map { preferences ->
            val s = (preferences[STEREO] ?: 0).coerceIn(-5..5)
            when (s) {
                in -5 until 0 -> {
                    Pair(1, 1 - s * 2)
                }

                in 5 downTo 1 -> {
                    Pair(1 - s * 2, 1)
                }

                else -> Pair(1, 1)
            }
        }
    override val stereoSettings = dataStore.data.map { preferences ->
        (preferences[STEREO] ?: 0).coerceIn(-5..5)
    }

    override val totalTime = dataStore.data
        .map { preferences ->
            preferences[TOTAL_TIME] ?: 0L
        }

    override suspend fun saveStereo(value: Int) {
        dataStore.edit { preferences ->
            preferences[STEREO] = value
        }
    }

    override suspend fun saveColorScheme(colorScheme: AppColorScheme) {
        dataStore.edit { preferences ->
            preferences[COLOR_SCHEME] = colorScheme.ordinal
        }
    }

    override suspend fun saveColorFlash(colorFlash: Boolean) {
        dataStore.edit { preferences ->
            preferences[COLOR_FLASH] = colorFlash
        }
    }

    override suspend fun saveBackgroundPlay(backgroundPlay: Boolean) {
        dataStore.edit { preferences ->
            preferences[BACKGROUND_PLAY] = backgroundPlay
        }
    }

    override suspend fun addTotalTime(elapsedTime: Long) {
        dataStore.edit { preferences ->
            preferences[TOTAL_TIME] = totalTime.first() + elapsedTime
        }
    }

    override suspend fun resetTime() {
        dataStore.edit { preferences ->
            preferences[TOTAL_TIME] = 0L
        }
    }
}
