package com.merkost.metronome.model

enum class Beat(val rate: Float) {
    HIGH(1.4f),
    LOW(1f);

    fun next(): Beat {
        return when (this) {
            HIGH -> LOW
            LOW -> HIGH
        }
    }
}
