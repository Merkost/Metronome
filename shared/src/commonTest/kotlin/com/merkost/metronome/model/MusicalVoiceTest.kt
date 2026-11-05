package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.duration_hours
import metronome.shared.generated.resources.duration_hours_minutes
import metronome.shared.generated.resources.duration_minutes
import metronome.shared.generated.resources.voice_count_four
import metronome.shared.generated.resources.voice_count_one
import metronome.shared.generated.resources.voice_count_three
import metronome.shared.generated.resources.voice_count_twelve
import metronome.shared.generated.resources.voice_count_two
import metronome.shared.generated.resources.voice_practiced
import metronome.shared.generated.resources.voice_practiced_streak
import metronome.shared.generated.resources.voice_streak_day
import metronome.shared.generated.resources.voice_timer_finished
import metronome.shared.generated.resources.voice_timer_finished_line
import metronome.shared.generated.resources.voice_timer_finished_line_streak
import metronome.shared.generated.resources.voice_timer_steady

class MusicalVoiceTest {

    @Test
    fun countInSpeaksTheRemainingBeatsAsWords() {
        assertEquals(UiText.of(Res.string.voice_count_four), countInWord(4))
        assertEquals(UiText.of(Res.string.voice_count_three), countInWord(3))
        assertEquals(UiText.of(Res.string.voice_count_two), countInWord(2))
        assertEquals(UiText.of(Res.string.voice_count_one), countInWord(1))
        assertEquals(UiText.of(Res.string.voice_count_twelve), countInWord(12))
    }

    @Test
    fun countInFallsBackToDigitsBeyondTwelveAndNeverReturnsBlank() {
        assertEquals(UiText.plain("13"), countInWord(13))
        assertEquals(UiText.plain("0"), countInWord(0))
        assertEquals(UiText.plain("-1"), countInWord(-1))
    }

    @Test
    fun practiceSummaryReadsHoursThenStreak() {
        assertEquals(
            UiText.quantity(Res.plurals.voice_practiced_streak, 12, UiText.of(Res.string.duration_hours, 142), 12),
            practiceSummaryLine(142L * 3_600_000L, 12),
        )
        assertEquals(
            UiText.quantity(Res.plurals.voice_practiced_streak, 2, UiText.of(Res.string.duration_hours_minutes, 1, 30), 2),
            practiceSummaryLine(90L * 60_000L, 2),
        )
    }

    @Test
    fun practiceSummaryDropsTheStreakWhenItIsNotYetAStreak() {
        val expected = UiText.of(Res.string.voice_practiced, UiText.of(Res.string.duration_minutes, 42))
        assertEquals(expected, practiceSummaryLine(42L * 60_000L, 0))
        assertEquals(expected, practiceSummaryLine(42L * 60_000L, 1))
    }

    @Test
    fun practiceSummaryDropsMinutesOnceHoursReachTen() {
        assertEquals(
            UiText.of(Res.string.voice_practiced, UiText.of(Res.string.duration_hours, 10)),
            practiceSummaryLine((10L * 60L + 5L) * 60_000L, 0),
        )
    }

    @Test
    fun practiceSummaryIsAbsentForSomeoneWhoHasNotPracticed() {
        assertNull(practiceSummaryLine(0L, 0))
        assertNull(practiceSummaryLine(59_999L, 3))
    }

    @Test
    fun timerFinishedNamesTheLengthAndTheStreak() {
        val steady = UiText.of(Res.string.voice_timer_steady, 15)
        assertEquals(UiText.of(Res.string.voice_timer_finished_line, steady), timerFinishedLine(15, 0))
        assertEquals(UiText.of(Res.string.voice_timer_finished_line, steady), timerFinishedLine(15, 1))
        assertEquals(
            UiText.of(Res.string.voice_timer_finished_line_streak, steady, UiText.of(Res.string.voice_streak_day, 5)),
            timerFinishedLine(15, 5),
        )
    }

    @Test
    fun timerFinishedNamesNoLengthForNonPositiveMinutes() {
        val finished = UiText.of(Res.string.voice_timer_finished)
        assertEquals(UiText.of(Res.string.voice_timer_finished_line, finished), timerFinishedLine(0, 0))
        assertEquals(
            UiText.of(Res.string.voice_timer_finished_line_streak, finished, UiText.of(Res.string.voice_streak_day, 4)),
            timerFinishedLine(0, 4),
        )
    }

    @Test
    fun headlineAndStreakLineComposeIntoTheFullLine() {
        assertEquals(UiText.of(Res.string.voice_timer_steady, 20), timerFinishedHeadline(20))
        assertEquals(UiText.of(Res.string.voice_timer_finished), timerFinishedHeadline(0))
        assertEquals(UiText.of(Res.string.voice_streak_day, 3), streakDayLine(3))
        assertNull(streakDayLine(1))
        assertNull(streakDayLine(0))
    }
}
