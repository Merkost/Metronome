package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MusicalVoiceTest {

    @Test
    fun tempoMarkingFollowsTheClassicalBands() {
        assertEquals("Largo", tempoMarking(40))
        assertEquals("Adagio", tempoMarking(70))
        assertEquals("Andante", tempoMarking(80))
        assertEquals("Moderato", tempoMarking(110))
        assertEquals("Allegro", tempoMarking(120))
        assertEquals("Vivace", tempoMarking(160))
        assertEquals("Presto", tempoMarking(200))
    }

    @Test
    fun tempoMarkingChangesExactlyAtTheBandEdges() {
        assertEquals("Grave", tempoMarking(39))
        assertEquals("Largo", tempoMarking(59))
        assertEquals("Larghetto", tempoMarking(60))
        assertEquals("Adagio", tempoMarking(66))
        assertEquals("Andante", tempoMarking(76))
        assertEquals("Andante", tempoMarking(107))
        assertEquals("Moderato", tempoMarking(108))
        assertEquals("Allegro", tempoMarking(120))
        assertEquals("Allegro", tempoMarking(155))
        assertEquals("Vivace", tempoMarking(156))
        assertEquals("Presto", tempoMarking(176))
    }

    @Test
    fun countInSpeaksTheRemainingBeatsAsWords() {
        assertEquals("Four", countInWord(4))
        assertEquals("Three", countInWord(3))
        assertEquals("Two", countInWord(2))
        assertEquals("One", countInWord(1))
        assertEquals("Twelve", countInWord(12))
    }

    @Test
    fun countInFallsBackToDigitsBeyondTwelveAndNeverReturnsBlank() {
        assertEquals("13", countInWord(13))
        assertEquals("0", countInWord(0))
        assertEquals("-1", countInWord(-1))
    }

    @Test
    fun practiceSummaryReadsHoursThenStreak() {
        assertEquals("142 h practiced · 12-day streak", practiceSummaryLine(142L * 3_600_000L, 12))
        assertEquals("1 h 30 min practiced · 2-day streak", practiceSummaryLine(90L * 60_000L, 2))
    }

    @Test
    fun practiceSummaryDropsTheStreakWhenItIsNotYetAStreak() {
        assertEquals("42 min practiced", practiceSummaryLine(42L * 60_000L, 0))
        assertEquals("42 min practiced", practiceSummaryLine(42L * 60_000L, 1))
    }

    @Test
    fun practiceSummaryIsAbsentForSomeoneWhoHasNotPracticed() {
        assertNull(practiceSummaryLine(0L, 0))
        assertNull(practiceSummaryLine(59_999L, 3))
    }

    @Test
    fun timerFinishedNamesTheLengthAndTheStreak() {
        assertEquals("A steady 15.", timerFinishedLine(15, 0))
        assertEquals("A steady 15.", timerFinishedLine(15, 1))
        assertEquals("A steady 15. Day 5 in a row.", timerFinishedLine(15, 5))
    }

    @Test
    fun timerFinishedNamesNoLengthForNonPositiveMinutes() {
        assertEquals("Timer finished.", timerFinishedLine(0, 0))
        assertEquals("Timer finished. Day 4 in a row.", timerFinishedLine(0, 4))
    }

    @Test
    fun headlineAndStreakLineComposeIntoTheFullLine() {
        assertEquals("A steady 20", timerFinishedHeadline(20))
        assertEquals("Timer finished", timerFinishedHeadline(0))
        assertEquals("Day 3 in a row", streakDayLine(3))
        assertNull(streakDayLine(1))
        assertNull(streakDayLine(0))
    }
}
