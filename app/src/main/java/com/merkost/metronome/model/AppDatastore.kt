package com.merkost.metronome.model

import kotlinx.coroutines.flow.Flow

interface AppDatastore {
//    fun saveStereo()
    val color: Flow<ColorScheme>
    val colorFlash: Flow<Boolean>
    val backgroundPlay: Flow<Boolean>


    suspend fun saveColor(color: ColorScheme)
    suspend fun saveColorFlash(colorFlash: Boolean)
    suspend fun saveBackgroundPlay(backgroundPlay: Boolean)

}