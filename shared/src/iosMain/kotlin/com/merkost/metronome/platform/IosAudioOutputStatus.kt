package com.merkost.metronome.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.outputVolume

@OptIn(ExperimentalForeignApi::class)
class IosAudioOutputStatus : AudioOutputStatus {
    override fun isMediaVolumeOff(): Boolean = AVAudioSession.sharedInstance().outputVolume <= 0f

    override val canShowVolumeControls: Boolean = false

    override fun showVolumeControls() = Unit
}
