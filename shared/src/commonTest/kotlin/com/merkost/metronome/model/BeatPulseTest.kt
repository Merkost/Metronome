package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

class BeatPulseTest {
    @Test
    fun eachPrimaryClickIsACentreCrossingWithAlternatingExtrema() {
        val source = TestTimeSource()
        val even = BeatPulse(source.markNow(), 500.milliseconds, 0L)
        val odd = BeatPulse(source.markNow(), 500.milliseconds, 1L)
        listOf(0 to 0f, 125 to 0.70710677f, 250 to 1f, 375 to 0.70710677f, 500 to 0f, 750 to -1f, 1000 to 0f).forEach { (time, expected) ->
            assertEquals(expected, even.pendulumDisplacement(time.milliseconds), 0.00001f)
            assertEquals(-expected, odd.pendulumDisplacement(time.milliseconds), 0.00001f)
        }
    }

    @Test
    fun movementIsFastestAtCentreAndSlowsAtTheTurningPoint() {
        val pulse = BeatPulse(TestTimeSource().markNow(), 500.milliseconds, 0L)
        val centreTravel = pulse.pendulumDisplacement(10.milliseconds) - pulse.pendulumDisplacement(Duration.ZERO)
        val edgeTravel = pulse.pendulumDisplacement(250.milliseconds) - pulse.pendulumDisplacement(240.milliseconds)
        assertTrue(centreTravel > edgeTravel * 20f)
        assertTrue(pulse.pendulumDisplacement(260.milliseconds) < pulse.pendulumDisplacement(250.milliseconds))
    }

    @Test
    fun lateRenderingUsesElapsedAudioTimeWithoutIntegratingDroppedFrames() {
        val source = TestTimeSource()
        val pulse = BeatPulse(source.markNow(), 500.milliseconds, 0L)
        source += 375.milliseconds
        assertEquals(0.70710677f, pulse.pendulumDisplacement(), 0.00001f)
        source += 2000.milliseconds
        assertEquals(0.70710677f, pulse.pendulumDisplacement(), 0.00001f)
        assertEquals(-0.70710677f, pulse.pendulumDisplacement(500_000_625.milliseconds), 0.00001f)
    }

    @Test
    fun tempoChangesKeepTheClickCentredAndUseItsCapturedDuration() {
        val source = TestTimeSource()
        val slow = BeatPulse(source.markNow(), 1500.milliseconds, 0L)
        assertEquals(1f, slow.pendulumDisplacement(750.milliseconds), 0.00001f)
        source += 1500.milliseconds
        val fast = BeatPulse(source.markNow(), (60_000.0 / 220).milliseconds, 1L)
        assertEquals(0f, fast.pendulumDisplacement(), 0.00001f)
        assertEquals(-1f, fast.pendulumDisplacement(fast.interval / 2), 0.00001f)
        assertEquals(0f, fast.pendulumDisplacement(fast.interval), 0.00001f)
    }

    @Test
    fun anUpcomingPulseWaitsAtCentreUntilItsTimestamp() {
        val source = TestTimeSource()
        val pulse = BeatPulse(source.markNow() + 500.milliseconds, 500.milliseconds, 0L)
        assertEquals(0f, pulse.pendulumDisplacement())
        source += 750.milliseconds
        assertEquals(1f, pulse.pendulumDisplacement(), 0.00001f)
    }

    @Test
    fun invalidCadencesCannotProduceAnUnboundedOrUndefinedPose() {
        val mark = TestTimeSource().markNow()
        listOf(Duration.ZERO, (-1).milliseconds, Duration.INFINITE).forEach { interval ->
            assertFailsWith<IllegalArgumentException> { BeatPulse(mark, interval, 0L) }
        }
        assertFailsWith<IllegalArgumentException> { BeatPulse(mark, 500.milliseconds, -1L) }
    }
}
