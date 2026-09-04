package com.merkost.metronome.engine

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AudioSessionEventPolicyTest {
    @Test
    fun interruptionStartAndDisconnectedRouteStopPlayback() {
        assertTrue(shouldStopPlaybackFor(AudioSessionEvent.INTERRUPTION_BEGAN))
        assertTrue(shouldStopPlaybackFor(AudioSessionEvent.OLD_ROUTE_UNAVAILABLE))
    }

    @Test
    fun interruptionEndAndUnrelatedRouteChangesDoNotStopPlayback() {
        assertFalse(shouldStopPlaybackFor(AudioSessionEvent.INTERRUPTION_ENDED))
        assertFalse(shouldStopPlaybackFor(AudioSessionEvent.OTHER))
    }
}
