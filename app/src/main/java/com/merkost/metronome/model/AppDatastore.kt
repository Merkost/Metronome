package com.merkost.metronome.model

import com.merkost.metronome.ui.theme.AppColorScheme
import kotlinx.coroutines.flow.Flow

interface AppDatastore {
    val color: Flow<AppColorScheme>
    val colorFlash: Flow<Boolean>
    val backgroundPlay: Flow<Boolean>
    val stereo: Flow<Pair<Int, Int>>
    val stereoSettings: Flow<Int>
    val totalTime: Flow<Long>
    val colorScheme: Flow<AppColorScheme>

    suspend fun saveStereo(value: Int)
    suspend fun saveColor(color: AppColorScheme)
    suspend fun saveColorFlash(colorFlash: Boolean)
    suspend fun saveBackgroundPlay(backgroundPlay: Boolean)
    suspend fun addTotalTime(elapsedTime: Long)
    suspend fun resetTime()

}