package com.merkost.metronome.engine

internal enum class AudioSessionEvent {
    INTERRUPTION_BEGAN,
    INTERRUPTION_ENDED,
    OLD_ROUTE_UNAVAILABLE,
    OTHER,
}

internal fun shouldStopPlaybackFor(event: AudioSessionEvent): Boolean = when (event) {
    AudioSessionEvent.INTERRUPTION_BEGAN,
    AudioSessionEvent.OLD_ROUTE_UNAVAILABLE -> true
    AudioSessionEvent.INTERRUPTION_ENDED,
    AudioSessionEvent.OTHER -> false
}
