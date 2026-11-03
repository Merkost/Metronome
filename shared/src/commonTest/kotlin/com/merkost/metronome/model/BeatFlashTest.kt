package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BeatFlashTest {
    @Test
    fun everyBeatFlashesUpToOneHundredEightyBpm() {
        (0 until 4).forEach { index -> assertTrue(flashEnabledFor(bpm = 180, beatIndex = index, beatsPerBar = 4)) }
    }

    @Test
    fun aboveOneHundredEightyBpmOnlyTheDownbeatFlashes() {
        assertTrue(flashEnabledFor(bpm = 181, beatIndex = 0, beatsPerBar = 4))
        assertFalse(flashEnabledFor(bpm = 181, beatIndex = 1, beatsPerBar = 4))
        assertTrue(flashEnabledFor(bpm = 300, beatIndex = 0, beatsPerBar = 2))
    }

    @Test
    fun neverMoreThanThreeFlashesPerSecond() {
        assertFalse(flashEnabledFor(bpm = 200, beatIndex = 0, beatsPerBar = 1))
        assertFalse(flashEnabledFor(bpm = 300, beatIndex = 0, beatsPerBar = 1))
        assertTrue(flashEnabledFor(bpm = 300, beatIndex = 0, beatsPerBar = 2))
        assertTrue(flashEnabledFor(bpm = 360, beatIndex = 0, beatsPerBar = 2))
        assertFalse(flashEnabledFor(bpm = 361, beatIndex = 0, beatsPerBar = 2))
    }
}
