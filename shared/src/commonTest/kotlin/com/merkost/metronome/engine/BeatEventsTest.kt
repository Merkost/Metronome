package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.Subdivision
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class BeatEventsTest {

    private val sub = 0.35f

    private fun events(
        beat: Beat = Beat.HIGH,
        subdivision: Subdivision = Subdivision.QUARTER,
        muted: Boolean = false,
        interval: Duration = 500.milliseconds,
        left: Float = 1f,
        right: Float = 1f,
        volume: Float = 1f,
    ) = beatEvents(beat, interval, subdivision.clicksPerBeat, muted, left, right, volume, sub)

    @Test
    fun clickCountPerBeatMatchesSubdivision() {
        assertEquals(1, events(subdivision = Subdivision.QUARTER).size)
        assertEquals(2, events(subdivision = Subdivision.EIGHTH).size)
        assertEquals(3, events(subdivision = Subdivision.TRIPLET).size)
        assertEquals(4, events(subdivision = Subdivision.SIXTEENTH).size)
    }

    @Test
    fun mainClickCarriesTheAccentAtOffsetZero() {
        val main = events(beat = Beat.HIGH, subdivision = Subdivision.SIXTEENTH).first()
        assertEquals(Duration.ZERO, main.offset)
        assertEquals(Beat.HIGH, main.beat)
    }

    @Test
    fun subClicksAreLowAndEvenlySpaced() {
        val list = events(subdivision = Subdivision.SIXTEENTH, interval = 500.milliseconds)
        val subs = list.drop(1)
        assertEquals(listOf(125.milliseconds, 250.milliseconds, 375.milliseconds), subs.map { it.offset })
        assertTrue(subs.all { it.beat == Beat.LOW })
    }

    @Test
    fun gapMutedSilencesEverything() {
        for (subdivision in Subdivision.entries) {
            assertEquals(emptyList(), events(subdivision = subdivision, muted = true))
        }
    }

    @Test
    fun perBeatMuteDropsDownbeatButKeepsSubClicks() {
        val eighth = events(beat = Beat.MUTE, subdivision = Subdivision.EIGHTH)
        assertEquals(1, eighth.size)
        assertEquals(250.milliseconds, eighth.single().offset)
        assertEquals(Beat.LOW, eighth.single().beat)
    }

    @Test
    fun perBeatMuteWithoutSubdivisionIsSilent() {
        assertEquals(emptyList(), events(beat = Beat.MUTE, subdivision = Subdivision.QUARTER))
    }

    @Test
    fun mainUsesFullVolumeAndSubUsesSubVolume_perChannel() {
        val list = events(
            subdivision = Subdivision.EIGHTH,
            left = 0.8f,
            right = 0.4f,
            volume = 0.5f,
        )
        val main = list[0]
        assertEquals(0.8f * 0.5f, main.leftVolume, 1e-6f)
        assertEquals(0.4f * 0.5f, main.rightVolume, 1e-6f)
        val subClick = list[1]
        assertEquals(0.8f * sub * 0.5f, subClick.leftVolume, 1e-6f)
        assertEquals(0.4f * sub * 0.5f, subClick.rightVolume, 1e-6f)
    }
}
