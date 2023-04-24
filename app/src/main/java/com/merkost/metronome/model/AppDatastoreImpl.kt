package com.merkost.metronome.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class AppDatastoreImpl(val context: Context) : AppDatastore {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("appSettings")

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

}