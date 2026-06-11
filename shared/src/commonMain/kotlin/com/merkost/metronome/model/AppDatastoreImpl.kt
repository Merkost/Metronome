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
        private val TRAINER_START_BPM = intPreferencesKey("TRAINER_START_BPM")
        private val TRAINER_END_BPM = intPreferencesKey("TRAINER_END_BPM")
        private val TRAINER_INCREMENT = intPreferencesKey("TRAINER_INCREMENT")
        private val TRAINER_BARS_PER_STEP = intPreferencesKey("TRAINER_BARS_PER_STEP")
        private val LAST_TIMER_MINUTES = intPreferencesKey("LAST_TIMER_MINUTES")
        private val SUBDIVISION = stringPreferencesKey("SUBDIVISION")
        private val GAP_PLAY_BARS = intPreferencesKey("GAP_PLAY_BARS")
        private val GAP_MUTE_BARS = intPreferencesKey("GAP_MUTE_BARS")
        private val KEEP_SCREEN_AWAKE = booleanPreferencesKey("KEEP_SCREEN_AWAKE")
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
            preferences[TOTAL_TIME] = (preferences[TOTAL_TIME] ?: 0L) + elapsedTime
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

    override val lastTrainerConfig: Flow<GradualTempoConfig?> = dataStore.data
        .map { preferences ->
            val start = preferences[TRAINER_START_BPM] ?: return@map null
            val end = preferences[TRAINER_END_BPM] ?: return@map null
            GradualTempoConfig(
                startBpm = start.coerceIn(MIN_BPM, MAX_BPM),
                endBpm = end.coerceIn(MIN_BPM, MAX_BPM),
                increment = (preferences[TRAINER_INCREMENT] ?: 2).coerceAtLeast(1),
                barsPerStep = (preferences[TRAINER_BARS_PER_STEP] ?: 4).coerceAtLeast(1),
            )
        }

    override suspend fun saveLastTrainerConfig(config: GradualTempoConfig) {
        dataStore.edit { preferences ->
            preferences[TRAINER_START_BPM] = config.startBpm
            preferences[TRAINER_END_BPM] = config.endBpm
            preferences[TRAINER_INCREMENT] = config.increment
            preferences[TRAINER_BARS_PER_STEP] = config.barsPerStep
        }
    }

    override val lastTimerMinutes: Flow<Int> = dataStore.data
        .map { preferences ->
            (preferences[LAST_TIMER_MINUTES] ?: 15).coerceIn(1, 120)
        }

    override suspend fun saveLastTimerMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[LAST_TIMER_MINUTES] = minutes
        }
    }

    override val subdivision: Flow<Subdivision> = dataStore.data
        .map { preferences ->
            kotlin.runCatching {
                Subdivision.valueOf(preferences[SUBDIVISION] ?: Subdivision.QUARTER.name)
            }.getOrDefault(Subdivision.QUARTER)
        }

    override suspend fun saveSubdivision(subdivision: Subdivision) {
        dataStore.edit { preferences ->
            preferences[SUBDIVISION] = subdivision.name
        }
    }

    override val lastGapConfig: Flow<GapTrainerConfig?> = dataStore.data
        .map { preferences ->
            val playBars = preferences[GAP_PLAY_BARS] ?: return@map null
            val muteBars = preferences[GAP_MUTE_BARS] ?: return@map null
            GapTrainerConfig(
                playBars = playBars.coerceAtLeast(1),
                muteBars = muteBars.coerceAtLeast(1),
            )
        }

    override suspend fun saveLastGapConfig(config: GapTrainerConfig) {
        dataStore.edit { preferences ->
            preferences[GAP_PLAY_BARS] = config.playBars
            preferences[GAP_MUTE_BARS] = config.muteBars
        }
    }

    override val keepScreenAwake: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEEP_SCREEN_AWAKE] ?: true
        }

    override suspend fun saveKeepScreenAwake(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEEP_SCREEN_AWAKE] = enabled
        }
    }
}
