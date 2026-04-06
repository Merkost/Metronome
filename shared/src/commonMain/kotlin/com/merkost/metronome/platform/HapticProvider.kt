package com.merkost.metronome.platform

import com.merkost.metronome.model.Beat

interface HapticProvider {
    fun playBeatHaptic(beat: Beat)
    fun playConfirmHaptic()
}
