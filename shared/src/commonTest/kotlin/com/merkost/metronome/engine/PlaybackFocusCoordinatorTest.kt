package com.merkost.metronome.engine

import com.merkost.metronome.platform.AudioFocusEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaybackFocusCoordinatorTest {
    private class Harness(var playing: Boolean = true) {
        var now = 0L
        val log = mutableListOf<String>()
        val coordinator = PlaybackFocusCoordinator(
            isPlaying = { playing },
            stop = { playing = false; log += "stop" },
            resume = { playing = true; log += "resume" },
            setDuckGain = { log += "gain=$it" },
            nowMillis = { now },
        )
    }

    @Test
    fun permanentLossStopsAndNeverResumes() {
        val h = Harness()
        h.coordinator.onEvent(AudioFocusEvent.LOSS)
        h.now = 1_000
        h.coordinator.onEvent(AudioFocusEvent.GAIN)
        assertEquals(listOf("stop"), h.log)
        assertFalse(h.coordinator.waitingToResume)
    }

    @Test
    fun transientLossPausesAndGainWithinThirtySecondsResumes() {
        val h = Harness()
        h.coordinator.onEvent(AudioFocusEvent.LOSS_TRANSIENT)
        assertTrue(h.coordinator.waitingToResume)
        h.now = 30_000
        h.coordinator.onEvent(AudioFocusEvent.GAIN)
        assertEquals(listOf("stop", "resume"), h.log)
        assertFalse(h.coordinator.waitingToResume)
    }

    @Test
    fun gainAfterThirtySecondsDoesNotResume() {
        val h = Harness()
        h.coordinator.onEvent(AudioFocusEvent.LOSS_TRANSIENT)
        h.now = 30_001
        h.coordinator.onEvent(AudioFocusEvent.GAIN)
        assertEquals(listOf("stop"), h.log)
        assertFalse(h.coordinator.waitingToResume)
    }

    @Test
    fun canDuckDipsToFortyPercentAndGainRestores() {
        val h = Harness()
        h.coordinator.onEvent(AudioFocusEvent.LOSS_TRANSIENT_CAN_DUCK)
        h.coordinator.onEvent(AudioFocusEvent.GAIN)
        assertEquals(listOf("gain=0.4", "gain=1.0"), h.log)
        assertTrue(h.playing)
    }

    @Test
    fun duckingDoesNotPauseOrScheduleAResume() {
        val h = Harness()
        h.coordinator.onEvent(AudioFocusEvent.LOSS_TRANSIENT_CAN_DUCK)
        assertFalse(h.coordinator.waitingToResume)
        assertTrue(h.playing)
    }

    @Test
    fun theUserStartingPlaybackCancelsTheAutomaticResume() {
        val h = Harness()
        h.coordinator.onEvent(AudioFocusEvent.LOSS_TRANSIENT)
        h.playing = true
        h.coordinator.onPlayingChanged(true)
        assertFalse(h.coordinator.waitingToResume)
        h.coordinator.onEvent(AudioFocusEvent.GAIN)
        assertEquals(listOf("stop"), h.log)
    }

    @Test
    fun transientLossWhileAlreadyPausedDoesNothing() {
        val h = Harness(playing = false)
        h.coordinator.onEvent(AudioFocusEvent.LOSS_TRANSIENT)
        h.coordinator.onEvent(AudioFocusEvent.GAIN)
        assertTrue(h.log.isEmpty())
        assertFalse(h.coordinator.waitingToResume)
    }

    @Test
    fun permanentLossAfterATransientPauseCancelsTheResume() {
        val h = Harness()
        h.coordinator.onEvent(AudioFocusEvent.LOSS_TRANSIENT)
        h.coordinator.onEvent(AudioFocusEvent.LOSS)
        h.now = 5_000
        h.coordinator.onEvent(AudioFocusEvent.GAIN)
        assertEquals(listOf("stop"), h.log)
    }

    @Test
    fun lossWhileDuckedRestoresTheGainForTheNextStart() {
        val h = Harness()
        h.coordinator.onEvent(AudioFocusEvent.LOSS_TRANSIENT_CAN_DUCK)
        h.coordinator.onEvent(AudioFocusEvent.LOSS)
        assertEquals(listOf("gain=0.4", "stop", "gain=1.0"), h.log)
    }

    @Test
    fun gainWithoutAnyLossDoesNothing() {
        val h = Harness()
        h.coordinator.onEvent(AudioFocusEvent.GAIN)
        assertTrue(h.log.isEmpty())
    }

    @Test
    fun transientPauseWhileDuckedRestoresGainBeforeWaiting() {
        val h = Harness()
        h.coordinator.onEvent(AudioFocusEvent.LOSS_TRANSIENT_CAN_DUCK)
        h.coordinator.onEvent(AudioFocusEvent.LOSS_TRANSIENT)
        h.now = 2_000
        h.coordinator.onEvent(AudioFocusEvent.GAIN)
        assertEquals(listOf("gain=0.4", "gain=1.0", "stop", "resume"), h.log)
    }
}
