package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound

interface MetronomePlayer {
    fun initialize(initialSound: ClickSound = ClickSound.WOOD)
    fun play(beat: Beat, stereoLeft: Float, stereoRight: Float)
    fun stop()
    fun release()
    fun switchSound(sound: ClickSound)
}
