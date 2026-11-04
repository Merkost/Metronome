package com.merkost.metronome.model

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

fun weekSentence(week: PracticeWeek): String? {
    if (week.daysPracticed == 0) return null
    val totalMinutes = week.totalMillis / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val time = when {
        hours > 0 && minutes > 0 -> "$hours h $minutes min"
        hours > 0 -> "$hours h"
        else -> "$minutes min"
    }
    val span = if (week.daysPracticed == 1) "on 1 day" else "across ${week.daysPracticed} days"
    return "This week you practiced $time $span."
}

fun weekStripDescription(week: PracticeWeek): String = when (week.daysPracticed) {
    0 -> "No practice yet this week"
    1 -> "Practiced 1 day this week"
    else -> "Practiced ${week.daysPracticed} days this week"
}
