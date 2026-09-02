package com.merkost.metronome.presets

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.StopWatchState
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetronomePresetApplicationTest {
    @Test
    fun replacesCompleteRhythmConfigurationWithoutChangingPlaybackSession() {
        val stopwatch = StopWatchState(startTime = 10L, elapsedTime = 20L)
        val state = MetronomeState(
            playing = true,
            rhythm = 80,
            stopWatchState = stopwatch,
        )
        val preset = PracticePreset(
            id = "preset",
            name = "Odd meter",
            createdAtEpochMillis = 1L,
            lastUsedAtEpochMillis = null,
            isFavourite = false,
            sortPosition = 0,
            bpm = 135,
            timeSignature = TimeSignature.SEVEN_EIGHT,
            subdivision = Subdivision.SIXTEENTH,
            beats = listOf(Beat.HIGH, Beat.LOW, Beat.MUTE, Beat.HIGH, Beat.LOW, Beat.MUTE, Beat.HIGH),
            countInEnabled = true,
        )

        val applied = state.applyPracticePreset(preset)

        assertEquals(135, applied.rhythm)
        assertEquals(TimeSignature.SEVEN_EIGHT, applied.timeSignature)
        assertEquals(Subdivision.SIXTEENTH, applied.subdivision)
        assertEquals(preset.beats, applied.beats)
        assertTrue(applied.playing)
        assertEquals(stopwatch, applied.stopWatchState)
    }
}
