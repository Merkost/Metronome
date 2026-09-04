package com.merkost.metronome.platform

import com.merkost.metronome.model.Beat

// Browsers expose no reliable per-beat haptic, so this is a no-op rather than
// a vibrate() call that would be ignored or throttled on most platforms.
class HapticProviderWasm : HapticProvider {
    override fun playBeatHaptic(beat: Beat) = Unit
    override fun playConfirmHaptic() = Unit
}
