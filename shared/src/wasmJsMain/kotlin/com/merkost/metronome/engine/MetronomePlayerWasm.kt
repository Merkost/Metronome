package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound

class MetronomePlayerWasm : MetronomePlayer {

    private var current: ClickSound = ClickSound.WOOD
    private var ready = false

    override fun initialize(initialSound: ClickSound) {
        current = initialSound
        webAudioInit()
        ready = true
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        if (!ready || beat == Beat.MUTE) return
        val gain = maxOf(stereoLeft, stereoRight)
        if (gain <= 0f) return
        val pan = (stereoRight - stereoLeft) / gain
        webAudioPlay(current.name, beat.rate, gain, pan)
    }

    override fun stop() = Unit

    override fun release() {
        ready = false
        webAudioRelease()
    }

    override fun switchSound(sound: ClickSound) {
        current = sound
    }
}

private fun webAudioInit(): Unit = js("MetronomeWebAudio.init()")

private fun webAudioPlay(sound: String, rate: Float, gain: Float, pan: Float): Unit =
    js("MetronomeWebAudio.play(sound, rate, gain, pan)")

private fun webAudioRelease(): Unit = js("MetronomeWebAudio.release()")
