package com.merkost.metronome.platform

interface AudioFocusController {
    fun setOnFocusLost(onLost: () -> Unit)
    fun setOnFocusEvent(listener: (AudioFocusEvent) -> Unit) {}
    fun requestFocus(): Boolean
    fun abandonFocus()
}

class NoopAudioFocusController : AudioFocusController {
    override fun setOnFocusLost(onLost: () -> Unit) {}
    override fun requestFocus(): Boolean = true
    override fun abandonFocus() {}
}

enum class AudioFocusEvent { LOSS, LOSS_TRANSIENT, LOSS_TRANSIENT_CAN_DUCK, GAIN }
