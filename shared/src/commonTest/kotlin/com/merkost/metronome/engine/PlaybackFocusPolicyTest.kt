package com.merkost.metronome.engine

import com.merkost.metronome.platform.AudioFocusController
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaybackFocusPolicyTest {
    @Test
    fun deniedFocusRejectsPlaybackAndReportsDenial() {
        var denied = false
        val granted = requestPlaybackFocus(FakeAudioFocusController(granted = false)) {
            denied = true
        }

        assertFalse(granted)
        assertTrue(denied)
    }

    @Test
    fun grantedFocusStartsPlaybackWithoutReportingDenial() {
        var denied = false
        val granted = requestPlaybackFocus(FakeAudioFocusController(granted = true)) {
            denied = true
        }

        assertTrue(granted)
        assertFalse(denied)
    }

    private class FakeAudioFocusController(private val granted: Boolean) : AudioFocusController {
        override fun setOnFocusLost(onLost: () -> Unit) {}
        override fun requestFocus(): Boolean = granted
        override fun abandonFocus() {}
    }
}
