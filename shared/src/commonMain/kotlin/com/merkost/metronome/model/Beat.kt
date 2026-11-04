package com.merkost.metronome.model

enum class Beat(val rate: Float) {
    HIGH(1.4f),
    LOW(1f),
    MUTE(1f);

    fun clicksTo(target: Beat): Int = (target.ordinal - ordinal + entries.size) % entries.size

    fun next(): Beat {
        return when (this) {
            HIGH -> LOW
            LOW -> MUTE
            MUTE -> HIGH
        }
    }
}
