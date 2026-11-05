package com.merkost.metronome.model

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.duration_hours
import metronome.shared.generated.resources.duration_hours_minutes
import metronome.shared.generated.resources.duration_minutes
import metronome.shared.generated.resources.voice_count_eight
import metronome.shared.generated.resources.voice_count_eleven
import metronome.shared.generated.resources.voice_count_five
import metronome.shared.generated.resources.voice_count_four
import metronome.shared.generated.resources.voice_count_nine
import metronome.shared.generated.resources.voice_count_one
import metronome.shared.generated.resources.voice_count_seven
import metronome.shared.generated.resources.voice_count_six
import metronome.shared.generated.resources.voice_count_ten
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

private val countWords = listOf(
    Res.string.voice_count_one,
    Res.string.voice_count_two,
    Res.string.voice_count_three,
    Res.string.voice_count_four,
    Res.string.voice_count_five,
    Res.string.voice_count_six,
    Res.string.voice_count_seven,
    Res.string.voice_count_eight,
    Res.string.voice_count_nine,
    Res.string.voice_count_ten,
    Res.string.voice_count_eleven,
    Res.string.voice_count_twelve,
)

fun countInWord(remaining: Int): UiText =
    countWords.getOrNull(remaining - 1)?.let { UiText.of(it) } ?: UiText.plain(remaining.toString())

fun durationText(totalMinutes: Long, hoursWithMinutesBelow: Int = Int.MAX_VALUE): UiText {
    val hours = (totalMinutes / 60).toInt()
    val minutes = (totalMinutes % 60).toInt()
    return when {
        hours > 0 && minutes > 0 && hours < hoursWithMinutesBelow ->
            UiText.of(Res.string.duration_hours_minutes, hours, minutes)
        hours > 0 -> UiText.of(Res.string.duration_hours, hours)
        else -> UiText.of(Res.string.duration_minutes, minutes)
    }
}

fun practiceSummaryLine(totalMillis: Long, streakDays: Int): UiText? {
    val totalMinutes = totalMillis / 60_000L
    if (totalMinutes < 1) return null
    val time = durationText(totalMinutes, hoursWithMinutesBelow = 10)
    return if (streakDays >= 2) {
        UiText.quantity(Res.plurals.voice_practiced_streak, streakDays, time, streakDays)
    } else {
        UiText.of(Res.string.voice_practiced, time)
    }
}

fun timerFinishedHeadline(minutes: Int): UiText =
    if (minutes <= 0) UiText.of(Res.string.voice_timer_finished) else UiText.of(Res.string.voice_timer_steady, minutes)

fun streakDayLine(streakDays: Int): UiText? =
    if (streakDays >= 2) UiText.of(Res.string.voice_streak_day, streakDays) else null

fun timerFinishedLine(minutes: Int, streakDays: Int): UiText {
    val streak = streakDayLine(streakDays)
    val headline = timerFinishedHeadline(minutes)
    return if (streak == null) {
        UiText.of(Res.string.voice_timer_finished_line, headline)
    } else {
        UiText.of(Res.string.voice_timer_finished_line_streak, headline, streak)
    }
}
