package com.merkost.metronome.platform

interface AudioOutputStatus {
    fun isMediaVolumeOff(): Boolean
    val canShowVolumeControls: Boolean
    fun showVolumeControls()
}

object UnknownAudioOutputStatus : AudioOutputStatus {
    override fun isMediaVolumeOff(): Boolean = false
    override val canShowVolumeControls: Boolean = false
    override fun showVolumeControls() = Unit
}
