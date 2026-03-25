package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat

interface MetronomePlayer {
    fun initialize()
    fun play(beat: Beat, stereoLeft: Float, stereoRight: Float)
    fun stop()
    fun release()
}
