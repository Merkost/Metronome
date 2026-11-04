package com.merkost.metronome.model

private val countWords = listOf(
    "One", "Two", "Three", "Four", "Five", "Six",
    "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve",
)

fun tempoMarking(bpm: Int): String = when {
    bpm < 40 -> "Grave"
    bpm < 60 -> "Largo"
    bpm < 66 -> "Larghetto"
    bpm < 76 -> "Adagio"
    bpm < 108 -> "Andante"
    bpm < 120 -> "Moderato"
    bpm < 156 -> "Allegro"
    bpm < 176 -> "Vivace"
    else -> "Presto"
}

fun countInWord(remaining: Int): String = countWords.getOrNull(remaining - 1) ?: remaining.toString()

fun practiceSummaryLine(totalMillis: Long, streakDays: Int): String? {
    val totalMinutes = totalMillis / 60_000L
    if (totalMinutes < 1) return null
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val time = when {
        hours > 0 && minutes > 0 && hours < 10 -> "$hours h $minutes min"
        hours > 0 -> "$hours h"
        else -> "$minutes min"
    }
    val streak = if (streakDays >= 2) " · $streakDays-day streak" else ""
    return "$time practised$streak"
}

fun timerFinishedHeadline(minutes: Int): String =
    if (minutes <= 0) "Timer finished" else "A steady $minutes"

fun streakDayLine(streakDays: Int): String? =
    if (streakDays >= 2) "Day $streakDays in a row" else null

fun timerFinishedLine(minutes: Int, streakDays: Int): String {
    val streak = streakDayLine(streakDays)
    val headline = timerFinishedHeadline(minutes)
    return if (streak == null) "$headline." else "$headline. $streak."
}
