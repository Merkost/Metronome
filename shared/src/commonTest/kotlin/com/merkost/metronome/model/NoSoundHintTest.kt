package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NoSoundHintTest {
    @Test
    fun mediaVolumeOffWinsOverClickVolume() {
        assertEquals(
            NoSoundHint.MEDIA_VOLUME_OFF,
            noSoundHint(playing = true, mediaVolumeOff = true, clickVolume = 0f),
        )
    }

    @Test
    fun clickVolumeZeroIsNamedWhenMediaIsAudible() {
        assertEquals(
            NoSoundHint.CLICK_VOLUME_OFF,
            noSoundHint(playing = true, mediaVolumeOff = false, clickVolume = 0f),
        )
    }

    @Test
    fun nothingWhenPausedOrAudible() {
        assertNull(noSoundHint(playing = false, mediaVolumeOff = true, clickVolume = 0f))
        assertNull(noSoundHint(playing = true, mediaVolumeOff = false, clickVolume = 0.5f))
        assertNull(noSoundHint(playing = true, mediaVolumeOff = false, clickVolume = 0.01f))
    }

    @Test
    fun mutedBeatsAreNeverACause() {
        assertNull(noSoundHint(playing = true, mediaVolumeOff = false, clickVolume = 1f))
    }
}
