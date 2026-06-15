package com.merkost.metronome.platform

interface AudioFocusController {
    fun setOnFocusLost(onLost: () -> Unit)
    fun requestFocus(): Boolean
    fun abandonFocus()
}

class NoopAudioFocusController : AudioFocusController {
    override fun setOnFocusLost(onLost: () -> Unit) {}
    override fun requestFocus(): Boolean = true
    override fun abandonFocus() {}
}
