package com.merkost.metronome.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        private val SELECTED_SOUND = stringPreferencesKey("SELECTED_SOUND")
        private val HAPTIC_ENABLED = booleanPreferencesKey("HAPTIC_ENABLED")
        private val ONBOARDING_COMPLETE = booleanPreferencesKey("ONBOARDING_COMPLETE")
        private val TIME_SIGNATURE = stringPreferencesKey("TIME_SIGNATURE")
    }

    override val colorScheme: Flow<AppColorScheme> = dataStore.data
        .map { preferences ->
            kotlin.runCatching { AppColorScheme.entries[preferences[COLOR_SCHEME] ?: AppColorScheme.BLACKNWHITE.ordinal] }
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
            val s = (preferences[STEREO] ?: 0).coerceIn(-5, 5)
            val left = if (s > 0) (5 - s) / 5f else 1f
            val right = if (s < 0) (5 + s) / 5f else 1f
            Pair(left, right)
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

    override val selectedSound: Flow<ClickSound> = dataStore.data
        .map { preferences ->
            kotlin.runCatching {
                ClickSound.valueOf(preferences[SELECTED_SOUND] ?: ClickSound.WOOD.name)
            }.getOrDefault(ClickSound.WOOD)
        }

    override suspend fun saveSelectedSound(sound: ClickSound) {
        dataStore.edit { preferences ->
            preferences[SELECTED_SOUND] = sound.name
        }
    }

    override val hapticEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[HAPTIC_ENABLED] ?: false
        }

    override suspend fun saveHapticEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAPTIC_ENABLED] = enabled
        }
    }

    override val onboardingComplete: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETE] ?: false
        }

    override suspend fun saveOnboardingComplete(complete: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETE] = complete
        }
    }

    override val timeSignature: Flow<TimeSignature> = dataStore.data
        .map { preferences ->
            kotlin.runCatching {
                TimeSignature.valueOf(preferences[TIME_SIGNATURE] ?: TimeSignature.FOUR_FOUR.name)
            }.getOrDefault(TimeSignature.FOUR_FOUR)
        }

    override suspend fun saveTimeSignature(timeSignature: TimeSignature) {
        dataStore.edit { preferences ->
            preferences[TIME_SIGNATURE] = timeSignature.name
        }
    }
}
