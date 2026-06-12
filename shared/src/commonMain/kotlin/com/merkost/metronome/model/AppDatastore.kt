package com.merkost.metronome.model

import com.merkost.metronome.ui.theme.AppColorScheme
import kotlinx.coroutines.flow.Flow

interface AppDatastore {
    val colorFlash: Flow<Boolean>
    val backgroundPlay: Flow<Boolean>
    val stereo: Flow<Pair<Float, Float>>
    val stereoSettings: Flow<Int>
    val totalTime: Flow<Long>
    val colorScheme: Flow<AppColorScheme>

    suspend fun saveStereo(value: Int)
    suspend fun saveColorScheme(colorScheme: AppColorScheme)
    suspend fun saveColorFlash(colorFlash: Boolean)
    suspend fun saveBackgroundPlay(backgroundPlay: Boolean)
    suspend fun addTotalTime(elapsedTime: Long)
    suspend fun resetTime()

    val selectedSound: Flow<ClickSound>
    suspend fun saveSelectedSound(sound: ClickSound)

    val hapticEnabled: Flow<Boolean>
    suspend fun saveHapticEnabled(enabled: Boolean)

    val onboardingComplete: Flow<Boolean>
    suspend fun saveOnboardingComplete(complete: Boolean)

    val timeSignature: Flow<TimeSignature>
    suspend fun saveTimeSignature(timeSignature: TimeSignature)

    val lastTrainerConfig: Flow<GradualTempoConfig?>
    suspend fun saveLastTrainerConfig(config: GradualTempoConfig)

    val lastTimerMinutes: Flow<Int>
    suspend fun saveLastTimerMinutes(minutes: Int)

    val subdivision: Flow<Subdivision>
    suspend fun saveSubdivision(subdivision: Subdivision)

    val lastGapConfig: Flow<GapTrainerConfig?>
    suspend fun saveLastGapConfig(config: GapTrainerConfig)

    val keepScreenAwake: Flow<Boolean>
    suspend fun saveKeepScreenAwake(enabled: Boolean)

    val countInEnabled: Flow<Boolean>
    suspend fun saveCountInEnabled(enabled: Boolean)

    val savedTempos: Flow<List<SavedTempo>>
    suspend fun addSavedTempo(tempo: SavedTempo)
    suspend fun removeSavedTempo(tempo: SavedTempo)

    val todayPracticeTime: Flow<Long>
    val practiceStreak: Flow<Int>
}