package com.merkost.metronome.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class BeatTimelineTest {

    @Test
    fun hundredsOfBeatsRemainAnchoredToTheOriginalCadence() {
        val timeSource = TestTimeSource()
        val timeline = BeatTimeline(timeSource)
        val initial = timeline.deadline

        repeat(1_000) { timeline.advance(200.milliseconds) }

        assertEquals(200_000.milliseconds, timeline.deadline - initial)
    }

    @Test
    fun smallLatenessKeepsCadenceAndSevereLatenessReanchors() {
        val timeSource = TestTimeSource()
        val timeline = BeatTimeline(timeSource)
        val initial = timeline.deadline

        timeSource += 1_100.milliseconds
        timeline.advance(1.seconds)
        assertEquals(1.seconds, timeline.deadline - initial)

        timeSource += 1_900.milliseconds
        timeline.advance(1.seconds)
        assertEquals(Duration.ZERO, timeline.deadline.elapsedNow())
    }

    @Test
    fun oneFullIntervalLateIsStale() {
        val source = TestTimeSource()
        val timeline = BeatTimeline(source)
        val event = timeline.deadline
        source += 500.milliseconds

        assertTrue(timeline.isStale(event, 500.milliseconds))
    }

    @Test
    fun explicitReanchorDropsTheOldDeadline() {
        val source = TestTimeSource()
        val timeline = BeatTimeline(source)
        source += 3.seconds

        timeline.reanchor()

        assertEquals(Duration.ZERO, timeline.deadline.elapsedNow())
    }

    @Test
    fun changedIntervalAppliesAfterTheCurrentDeadline() {
        val source = TestTimeSource()
        val timeline = BeatTimeline(source)
        val initial = timeline.deadline

        timeline.advance(1.seconds)
        timeline.advance(500.milliseconds)

        assertEquals(1_500.milliseconds, timeline.deadline - initial)
    }

    @Test
    fun countInAndMainBeatsCanShareOneCumulativeTimeline() {
        val source = TestTimeSource()
        val timeline = BeatTimeline(source)
        val initial = timeline.deadline

        repeat(5) { timeline.advance(500.milliseconds) }

        assertEquals(2_500.milliseconds, timeline.deadline - initial)
    }

    @Test
    fun aFreshTimelineNeverInheritsThePreviousDeadline() {
        val source = TestTimeSource()
        val first = BeatTimeline(source)
        first.advance(1.seconds)
        source += 5.seconds

        val restarted = BeatTimeline(source)

        assertEquals(Duration.ZERO, restarted.deadline.elapsedNow())
    }
}
