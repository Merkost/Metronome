package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface MetronomePlayer {
    val failures: Flow<Throwable> get() = emptyFlow()
    fun initialize(initialSound: ClickSound = ClickSound.WOOD)
    fun play(beat: Beat, stereoLeft: Float, stereoRight: Float)
    fun play(beat: Beat, stereoLeft: Float, stereoRight: Float, onSubmitted: () -> Unit) {
        play(beat, stereoLeft, stereoRight)
        onSubmitted()
    }
    fun stop()
    fun release()
    fun switchSound(sound: ClickSound)
}
