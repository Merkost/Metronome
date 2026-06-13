package com.merkost.metronome.model

data class SavedTempo(
    val bpm: Int,
    val timeSignature: TimeSignature,
    val subdivision: Subdivision,
) {
    val label: String
        get() = buildString {
            append("$bpm · ${timeSignature.label}")
            if (subdivision != Subdivision.QUARTER) {
                append(" · ${subdivision.label}")
            }
        }

    fun encode(): String = "$bpm:${timeSignature.name}:${subdivision.name}"

    companion object {
        const val MAX_SAVED = 8

        fun decode(raw: String): SavedTempo? {
            val parts = raw.split(":")
            if (parts.size != 3) return null
            return runCatching {
                SavedTempo(
                    bpm = parts[0].toInt().coerceIn(MIN_BPM, MAX_BPM),
                    timeSignature = TimeSignature.valueOf(parts[1]),
                    subdivision = Subdivision.valueOf(parts[2]),
                )
            }.getOrNull()
        }
    }
}
