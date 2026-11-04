package com.merkost.metronome.platform

import android.content.Context
import android.media.AudioManager

class AndroidAudioOutputStatus(context: Context) : AudioOutputStatus {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun isMediaVolumeOff(): Boolean =
        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0 ||
            audioManager.isStreamMute(AudioManager.STREAM_MUSIC)

    override val canShowVolumeControls: Boolean = true

    override fun showVolumeControls() {
        runCatching {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_SAME,
                AudioManager.FLAG_SHOW_UI,
            )
        }
    }
}
