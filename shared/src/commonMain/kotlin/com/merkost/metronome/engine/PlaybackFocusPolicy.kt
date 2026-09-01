package com.merkost.metronome.engine

import com.merkost.metronome.platform.AudioFocusController

internal inline fun requestPlaybackFocus(
    audioFocus: AudioFocusController,
    onDenied: () -> Unit,
): Boolean {
    val granted = audioFocus.requestFocus()
    if (!granted) onDenied()
    return granted
}
