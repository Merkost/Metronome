package com.merkost.metronome.engine

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackgroundStopPolicyTest {
    @Test
    fun aLockedScreenKeepsTheClickPlaying() {
        assertFalse(shouldStopWhenHidden(backgroundPlay = false, screenInteractive = false))
    }

    @Test
    fun leavingTheAppStopsTheClickWhenBackgroundPlayIsOff() {
        assertTrue(shouldStopWhenHidden(backgroundPlay = false, screenInteractive = true))
    }

    @Test
    fun backgroundPlayNeverStops() {
        assertFalse(shouldStopWhenHidden(backgroundPlay = true, screenInteractive = true))
        assertFalse(shouldStopWhenHidden(backgroundPlay = true, screenInteractive = false))
    }
}
