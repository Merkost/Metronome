package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.subClickOffsets
import kotlin.time.Duration

data class BeatEvent(
    val offset: Duration,
    val beat: Beat,
    val leftVolume: Float,
    val rightVolume: Float,
)

fun beatEvents(
    beat: Beat,
    interval: Duration,
    clicksPerBeat: Int,
    muted: Boolean,
    stereoLeft: Float,
    stereoRight: Float,
    volume: Float,
    subClickVolume: Float,
): List<BeatEvent> {
    if (muted) return emptyList()
    val events = ArrayList<BeatEvent>(clicksPerBeat)
    if (beat != Beat.MUTE) {
        events.add(BeatEvent(Duration.ZERO, beat, stereoLeft * volume, stereoRight * volume))
    }
    for (offset in subClickOffsets(interval, clicksPerBeat)) {
        events.add(
            BeatEvent(
                offset,
                Beat.LOW,
                stereoLeft * subClickVolume * volume,
                stereoRight * subClickVolume * volume,
            )
        )
    }
    return events
}
