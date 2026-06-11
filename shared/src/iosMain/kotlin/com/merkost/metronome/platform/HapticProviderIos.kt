package com.merkost.metronome.platform

import com.merkost.metronome.model.Beat
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

class HapticProviderIos : HapticProvider {
    private val lightGenerator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    private val mediumGenerator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)

    override fun playBeatHaptic(beat: Beat) {
        dispatch_async(dispatch_get_main_queue()) {
            when (beat) {
                Beat.HIGH -> mediumGenerator.impactOccurred()
                Beat.LOW -> lightGenerator.impactOccurred()
                Beat.MUTE -> Unit
            }
        }
    }

    override fun playConfirmHaptic() {
        dispatch_async(dispatch_get_main_queue()) {
            mediumGenerator.impactOccurred()
        }
    }
}
