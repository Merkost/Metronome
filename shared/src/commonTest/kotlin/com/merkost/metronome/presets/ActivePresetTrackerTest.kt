package com.merkost.metronome.presets

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActivePresetTrackerTest {
    @Test
    fun tracksAppliedPendingAndEditedStatesWithoutReplacingActiveEarly() {
        val first = preset("one")
        val second = preset("two")
        val tracker = ActivePresetTracker()

        tracker.applied(first)
        tracker.changed()
        tracker.pending(second)

        assertEquals("one", tracker.state.value.active?.id)
        assertTrue(tracker.state.value.isEdited)
        assertEquals("two", tracker.state.value.pending?.id)

        tracker.applied(second)

        assertEquals("two", tracker.state.value.active?.id)
        assertFalse(tracker.state.value.isEdited)
        assertNull(tracker.state.value.pending)
    }

    @Test
    fun deletingActiveReferencePreservesPendingAndLiveConfigurationIdentity() {
        val tracker = ActivePresetTracker()
        tracker.applied(preset("one"))
        tracker.pending(preset("two"))

        tracker.removed("one")

        assertNull(tracker.state.value.active)
        assertEquals("two", tracker.state.value.pending?.id)
    }

    @Test
    fun requestsApplyImmediatelyWhenPausedAndConsumesPlayingRequestAtBarBoundary() {
        val tracker = ActivePresetTracker()
        val first = preset("one")
        val second = preset("two")

        assertEquals(first, tracker.request(first, isPlaying = false))
        assertNull(tracker.request(second, isPlaying = true))
        assertEquals("one", tracker.state.value.active?.id)
        assertEquals("two", tracker.state.value.pending?.id)

        assertEquals(second, tracker.consumePendingAtBarBoundary())
        assertEquals("two", tracker.state.value.active?.id)
        assertNull(tracker.state.value.pending)
    }

    private fun preset(id: String) = PracticePreset(
        id = id,
        name = id,
        createdAtEpochMillis = 1L,
        lastUsedAtEpochMillis = null,
        isFavourite = false,
        sortPosition = 0,
        bpm = 96,
        timeSignature = TimeSignature.FOUR_FOUR,
        subdivision = Subdivision.QUARTER,
        beats = listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.LOW),
        countInEnabled = false,
    )
}
