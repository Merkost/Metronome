package com.merkost.metronome.model

import kotlin.math.max
import kotlin.math.pow

const val INDICATOR_BASE_STIFFNESS = 340f
const val INDICATOR_CROSSFADE_ABOVE_BPM = 160
const val HALO_PULSE_ALPHA = 0.07f

private const val SETTLE_OMEGA_T = 5.83
private const val SETTLE_SHARE_OF_BEAT = 0.35
private const val ACCENT_PEAK = 1.18f
private const val NORMAL_PEAK = 1.1f

fun strikePeakFor(beat: Beat): Float = when (beat) {
    Beat.HIGH -> ACCENT_PEAK
    Beat.LOW -> NORMAL_PEAK
    Beat.MUTE -> 1f
}

fun indicatorStiffnessFor(bpm: Int): Float {
    val beatSeconds = 60.0 / bpm.coerceAtLeast(1)
    val needed = (SETTLE_OMEGA_T / (SETTLE_SHARE_OF_BEAT * beatSeconds)).pow(2).toFloat()
    return max(INDICATOR_BASE_STIFFNESS, needed)
}

fun indicatorCrossfades(bpm: Int): Boolean = bpm > INDICATOR_CROSSFADE_ABOVE_BPM

fun strikeVisible(playing: Boolean, reducedMotion: Boolean, beat: Beat): Boolean =
    playing && !reducedMotion && beat != Beat.MUTE

fun haloPulseVisible(playing: Boolean, reducedMotion: Boolean, beatIndex: Int): Boolean =
    playing && !reducedMotion && beatIndex == 0
