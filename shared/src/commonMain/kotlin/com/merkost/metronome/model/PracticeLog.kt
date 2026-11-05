package com.merkost.metronome.model

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.week_sentence
import metronome.shared.generated.resources.week_strip_days
import metronome.shared.generated.resources.week_strip_none

data class PracticeDay(
    val epochDay: Long,
    val millis: Long,
    val practiced: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean,
)

data class PracticeWeek(
    val days: List<PracticeDay>,
    val totalMillis: Long,
    val daysPracticed: Int,
)

object PracticeLog {
    const val MIN_PRACTICE_MILLIS = 60_000L
    private const val KEEP_DAYS = 28

    fun parse(raw: String?): Map<Long, Long> {
        if (raw.isNullOrEmpty()) return emptyMap()
        val result = mutableMapOf<Long, Long>()
        raw.split(',').forEach { entry ->
            val parts = entry.split(':')
            if (parts.size != 2) return@forEach
            val day = parts[0].toLongOrNull() ?: return@forEach
            val millis = parts[1].toLongOrNull() ?: return@forEach
            if (millis > 0L) result[day] = millis
        }
        return result
    }

    fun encode(log: Map<Long, Long>): String =
        log.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value}" }

    fun add(log: Map<Long, Long>, today: Long, elapsedMillis: Long): Map<Long, Long> {
        if (elapsedMillis <= 0L) return log
        val result = log.filterKeys { it > today - KEEP_DAYS }.toMutableMap()
        result[today] = (result[today] ?: 0L) + elapsedMillis
        return result
    }

    fun week(log: Map<Long, Long>, today: Long): PracticeWeek {
        val mondayIndex = ((today + 3) % 7 + 7) % 7
        val start = today - mondayIndex
        val days = (0 until 7).map { offset ->
            val day = start + offset
            val millis = log[day] ?: 0L
            PracticeDay(
                epochDay = day,
                millis = millis,
                practiced = millis >= MIN_PRACTICE_MILLIS,
                isToday = day == today,
                isFuture = day > today,
            )
        }
        return PracticeWeek(days, days.sumOf { it.millis }, days.count { it.practiced })
    }
}

fun weekSentence(week: PracticeWeek): UiText? {
    if (week.daysPracticed == 0) return null
    val time = durationText(week.totalMillis / 60_000L)
    return UiText.quantity(Res.plurals.week_sentence, week.daysPracticed, time, week.daysPracticed)
}

fun weekStripDescription(week: PracticeWeek): UiText = when (week.daysPracticed) {
    0 -> UiText.of(Res.string.week_strip_none)
    else -> UiText.quantity(Res.plurals.week_strip_days, week.daysPracticed)
}
