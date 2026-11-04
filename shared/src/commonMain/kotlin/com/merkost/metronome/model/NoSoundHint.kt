package com.merkost.metronome.model

enum class NoSoundHint { MEDIA_VOLUME_OFF, CLICK_VOLUME_OFF }

fun noSoundHint(playing: Boolean, mediaVolumeOff: Boolean, clickVolume: Float): NoSoundHint? = when {
    !playing -> null
    mediaVolumeOff -> NoSoundHint.MEDIA_VOLUME_OFF
    clickVolume <= 0f -> NoSoundHint.CLICK_VOLUME_OFF
    else -> null
}
