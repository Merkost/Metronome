package com.merkost.metronome.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface MotionPreferences {
    val reduceMotion: StateFlow<Boolean>
}

object FullMotion : MotionPreferences {
    override val reduceMotion: StateFlow<Boolean> = MutableStateFlow(false)
}
