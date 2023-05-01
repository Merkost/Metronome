package com.merkost.metronome.model

import kotlinx.coroutines.flow.Flow

interface AppDatastore {
    val color: Flow<ColorScheme>
    val colorFlash: Flow<Boolean>
    val backgroundPlay: Flow<Boolean>
    val stereo: Flow<Pair<Int, Int>>
    val stereoSettings: Flow<Int>
    val totalTime: Flow<Long>


    suspend fun saveStereo(value: Int)
    suspend fun saveColor(color: ColorScheme)
    suspend fun saveColorFlash(colorFlash: Boolean)
    suspend fun saveBackgroundPlay(backgroundPlay: Boolean)
    suspend fun addTotalTime(elapsedTime: Long)
    suspend fun resetTime()

}