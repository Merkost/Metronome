package com.merkost.metronome.engine

import com.merkost.metronome.platform.AudioFocusController
import com.merkost.metronome.platform.AudioFocusEvent
import kotlin.concurrent.Volatile

internal inline fun requestPlaybackFocus(
    audioFocus: AudioFocusController,
    onDenied: () -> Unit,
): Boolean {
    val granted = audioFocus.requestFocus()
    if (!granted) onDenied()
    return granted
}

internal const val DUCKED_CLICK_GAIN = 0.4f
internal const val FOCUS_RESUME_WINDOW_MS = 30_000L

internal class PlaybackFocusCoordinator(
    private val isPlaying: () -> Boolean,
    private val stop: () -> Unit,
    private val resume: () -> Unit,
    private val setDuckGain: (Float) -> Unit,
    private val nowMillis: () -> Long,
) {
    @Volatile private var pausedAtMillis: Long? = null
    @Volatile private var ducked = false

    val waitingToResume: Boolean get() = pausedAtMillis != null

    fun onEvent(event: AudioFocusEvent) {
        when (event) {
            AudioFocusEvent.LOSS -> {
                pausedAtMillis = null
                if (isPlaying()) stop()
                restoreGain()
            }
            AudioFocusEvent.LOSS_TRANSIENT -> {
                restoreGain()
                if (isPlaying()) {
                    pausedAtMillis = nowMillis()
                    stop()
                }
            }
            AudioFocusEvent.LOSS_TRANSIENT_CAN_DUCK -> {
                if (isPlaying() && !ducked) {
                    ducked = true
                    setDuckGain(DUCKED_CLICK_GAIN)
                }
            }
            AudioFocusEvent.GAIN -> {
                restoreGain()
                val pausedAt = pausedAtMillis ?: return
                pausedAtMillis = null
                if (nowMillis() - pausedAt <= FOCUS_RESUME_WINDOW_MS) resume()
            }
        }
    }

    fun onPlayingChanged(playing: Boolean) {
        if (playing) pausedAtMillis = null
    }

    private fun restoreGain() {
        if (ducked) {
            ducked = false
            setDuckGain(1f)
        }
    }
}
