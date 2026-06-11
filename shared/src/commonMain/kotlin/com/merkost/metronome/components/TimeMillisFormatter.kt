package com.merkost.metronome.components

object TimestampMillisecondsFormatter {

    private const val DEFAULT_TIME = "0:00"

    fun format(timestamp: Long): String {
        if (timestamp == 0L) return DEFAULT_TIME

        val seconds = timestamp / 1000
        val secondsFormatted = (seconds % 60).pad(2)
        val minutes = seconds / 60
        val minutesFormatted = (minutes % 60)
        val hours = minutes / 60
        return if (hours > 0) {
            "$hours:${minutesFormatted.pad(2)}:$secondsFormatted"
        } else {
            "$minutesFormatted:$secondsFormatted"
        }
    }

    fun formatHuman(timestamp: Long): String {
        val totalMinutes = timestamp / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "$hours h $minutes min"
            minutes > 0 -> "$minutes min"
            else -> "0 min"
        }
    }

    private fun Long.pad(desiredLength: Int) = this.toString().padStart(desiredLength, '0')
}
