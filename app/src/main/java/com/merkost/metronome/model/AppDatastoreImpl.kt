package com.merkost.metronome.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single

class AppDatastoreImpl(val context: Context) : AppDatastore {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("appSettings")

        private val TOTAL_TIME = longPreferencesKey("TOTAL_TIME")
        private val STEREO = intPreferencesKey("STEREO")
        private val COLOR = intPreferencesKey("COLOR")
        private val COLOR_FLASH = booleanPreferencesKey("COLOR_FLASH")
        private val BACKGROUND_PLAY = booleanPreferencesKey("BACKGROUND_PLAY")

    }

    override val color = context.dataStore.data
        .map { preferences ->
            kotlin.runCatching { ColorScheme.values()[preferences[COLOR] ?: 0] }
                .getOrDefault(ColorScheme.WHITE)
        }
    override val colorFlash = context.dataStore.data
        .map { preferences ->
            preferences[COLOR_FLASH] ?: false
        }
    override val backgroundPlay = context.dataStore.data
        .map { preferences ->
            preferences[BACKGROUND_PLAY] ?: false
        }
    override val stereo = context.dataStore.data
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
    override val stereoSettings = context.dataStore.data.map { preferences ->
        (preferences[STEREO] ?: 0).coerceIn(-5..5)
    }

    override val totalTime = context.dataStore.data
        .map { preferences ->
            preferences[TOTAL_TIME] ?: 0L
        }

    override suspend fun saveStereo(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[STEREO] = value
        }
    }

    override suspend fun saveColor(color: ColorScheme) {
        context.dataStore.edit { preferences ->
            preferences[COLOR] = color.ordinal
        }
    }

    override suspend fun saveColorFlash(colorFlash: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_FLASH] = colorFlash
        }
    }

    override suspend fun saveBackgroundPlay(backgroundPlay: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BACKGROUND_PLAY] = backgroundPlay
        }
    }

    override suspend fun addTotalTime(elapsedTime: Long) {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_TIME] = totalTime.first() + elapsedTime
        }
    }

    override suspend fun resetTime() {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_TIME] = 0L
        }
    }
}