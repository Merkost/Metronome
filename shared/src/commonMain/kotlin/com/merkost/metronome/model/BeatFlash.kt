package com.merkost.metronome.model

private const val MAX_FLASHES_PER_MINUTE = 180

fun flashEnabledFor(bpm: Int, beatIndex: Int, beatsPerBar: Int): Boolean {
    if (bpm <= MAX_FLASHES_PER_MINUTE) return true
    if (beatIndex != 0) return false
    return bpm <= MAX_FLASHES_PER_MINUTE * beatsPerBar.coerceAtLeast(1)
}

fun beatFlashVisible(
    enabled: Boolean,
    playing: Boolean,
    reducedMotion: Boolean,
    bpm: Int,
    beatIndex: Int,
    beatsPerBar: Int,
): Boolean = enabled && playing && !reducedMotion && flashEnabledFor(bpm, beatIndex, beatsPerBar)
