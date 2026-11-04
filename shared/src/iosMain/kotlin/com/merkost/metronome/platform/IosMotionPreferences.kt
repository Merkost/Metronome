package com.merkost.metronome.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled
import platform.UIKit.UIAccessibilityReduceMotionStatusDidChangeNotification

class IosMotionPreferences : MotionPreferences {
    private val state = MutableStateFlow(UIAccessibilityIsReduceMotionEnabled())

    override val reduceMotion: StateFlow<Boolean> = state

    private val observer = NSNotificationCenter.defaultCenter.addObserverForName(
        name = UIAccessibilityReduceMotionStatusDidChangeNotification,
        `object` = null,
        queue = null,
    ) {
        state.value = UIAccessibilityIsReduceMotionEnabled()
    }
}
