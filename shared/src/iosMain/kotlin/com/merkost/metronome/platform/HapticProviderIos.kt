package com.merkost.metronome.platform

import com.merkost.metronome.model.Beat
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

class HapticProviderIos : HapticProvider {
    private val lightGenerator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    private val mediumGenerator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)

    override fun playBeatHaptic(beat: Beat) {
        when (beat) {
            Beat.HIGH -> mediumGenerator.impactOccurred()
            Beat.LOW -> lightGenerator.impactOccurred()
        }
    }

    override fun playConfirmHaptic() {
        mediumGenerator.impactOccurred()
    }
}
