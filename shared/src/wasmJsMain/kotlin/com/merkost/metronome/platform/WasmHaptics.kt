package com.merkost.metronome.platform

import com.merkost.metronome.model.Beat

class HapticProviderWasm : HapticProvider {
    override fun playBeatHaptic(beat: Beat) = Unit
    override fun playConfirmHaptic() = Unit
}
