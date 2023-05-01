package com.merkost.metronome.components

object TimestampMillisecondsFormatter {

    private const val DEFAULT_TIME = "0:00"

    fun format(timestamp: Long): String {
        if (timestamp == 0L) return DEFAULT_TIME

        val millisecondsFormatted = (timestamp % 1000).pad(3)
        val seconds = timestamp / 1000
        val secondsFormatted = (seconds % 60).pad(2)
        val minutes = seconds / 60
        val minutesFormatted = (minutes % 60)
        val hours = minutes / 60
        return if (hours > 0) {
            val hoursFormatted = (minutes / 60)
            "$hoursFormatted:${minutesFormatted.pad(2)}:$secondsFormatted"
        } else {
            "$minutesFormatted:$secondsFormatted"
        }
    }

    private fun Long.pad(desiredLength: Int) = this.toString().padStart(desiredLength, '0')
}