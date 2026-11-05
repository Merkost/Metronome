package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PracticeLogTest {

    private val wednesday = 20_000L + ((2 - (20_000L + 3) % 7) + 7) % 7

    @Test
    fun wednesdayFixtureIsAWednesday() {
        assertEquals(2, ((wednesday + 3) % 7).toInt())
    }

    @Test
    fun encodeThenParseRoundTrips() {
        val log = mapOf(100L to 60_000L, 101L to 90_000L)
        assertEquals(log, PracticeLog.parse(PracticeLog.encode(log)))
    }

    @Test
    fun parseIgnoresMalformedAndNonPositiveEntries() {
        val parsed = PracticeLog.parse("100:60000,abc,101:,102:-5,103:0,:7,104:30000:9,105:45000")
        assertEquals(mapOf(100L to 60_000L, 105L to 45_000L), parsed)
        assertEquals(emptyMap(), PracticeLog.parse(null))
        assertEquals(emptyMap(), PracticeLog.parse(""))
    }

    @Test
    fun addMergesTimeIntoTheSameDay() {
        val updated = PracticeLog.add(mapOf(wednesday to 60_000L), wednesday, 30_000L)
        assertEquals(90_000L, updated[wednesday])
    }

    @Test
    fun addForgetsDaysOlderThanFourWeeks() {
        val old = wednesday - 28
        val kept = wednesday - 27
        val updated = PracticeLog.add(mapOf(old to 5_000L, kept to 6_000L), wednesday, 1_000L)
        assertNull(updated[old])
        assertEquals(6_000L, updated[kept])
        assertEquals(1_000L, updated[wednesday])
    }

    @Test
    fun addIgnoresNonPositiveElapsedTime() {
        val log = mapOf(wednesday to 10_000L)
        assertEquals(log, PracticeLog.add(log, wednesday, 0L))
        assertEquals(log, PracticeLog.add(log, wednesday, -4L))
    }

    @Test
    fun weekRunsMondayToSundayAndMarksToday() {
        val week = PracticeLog.week(emptyMap(), wednesday)
        assertEquals(7, week.days.size)
        assertEquals(wednesday - 2, week.days.first().epochDay)
        assertEquals(wednesday + 4, week.days.last().epochDay)
        assertEquals(listOf(false, false, true, false, false, false, false), week.days.map { it.isToday })
        assertEquals(listOf(false, false, false, true, true, true, true), week.days.map { it.isFuture })
    }

    @Test
    fun weekCountsOnlyDaysThatReachTheMinimumAndSumsAllTime() {
        val log = mapOf(
            wednesday - 2 to 20L * 60_000L,
            wednesday - 1 to 30_000L,
            wednesday to 40L * 60_000L,
            wednesday - 9 to 99L * 60_000L,
        )
        val week = PracticeLog.week(log, wednesday)
        assertEquals(60L * 60_000L + 30_000L, week.totalMillis)
        assertEquals(2, week.daysPracticed)
        assertEquals(listOf(true, false, true, false, false, false, false), week.days.map { it.practiced })
    }

    @Test
    fun weekSentenceNamesTimeAndDays() {
        val log = mapOf(
            wednesday - 2 to 100L * 60_000L,
            wednesday - 1 to 100L * 60_000L,
            wednesday to 0L,
        )
        assertEquals(
            "This week you practiced 3 h 20 min across 2 days.",
            weekSentence(PracticeLog.week(log, wednesday)),
        )
    }

    @Test
    fun weekSentenceHandlesOneDayAndNoPractice() {
        val one = PracticeLog.week(mapOf(wednesday to 25L * 60_000L), wednesday)
        assertEquals("This week you practiced 25 min on 1 day.", weekSentence(one))
        assertNull(weekSentence(PracticeLog.week(emptyMap(), wednesday)))
    }

    @Test
    fun sundayAndMondayBelongToTheirOwnWeeks() {
        val monday = wednesday - 2
        val sunday = monday + 6
        assertEquals(monday, PracticeLog.week(emptyMap(), monday).days.first().epochDay)
        assertEquals(monday, PracticeLog.week(emptyMap(), sunday).days.first().epochDay)
        assertTrue(PracticeLog.week(emptyMap(), sunday).days.none { it.isFuture })
    }

    @Test
    fun stripDescriptionReadsOutHowManyDaysWerePracticed() {
        assertEquals("No practice yet this week", weekStripDescription(PracticeLog.week(emptyMap(), wednesday)))
        val one = PracticeLog.week(mapOf(wednesday to 120_000L), wednesday)
        assertEquals("Practiced 1 day this week", weekStripDescription(one))
        val two = PracticeLog.week(mapOf(wednesday to 120_000L, wednesday - 1 to 120_000L), wednesday)
        assertEquals("Practiced 2 days this week", weekStripDescription(two))
    }
}
