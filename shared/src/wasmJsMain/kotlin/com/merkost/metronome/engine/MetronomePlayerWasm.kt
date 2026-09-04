package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound

/*
 * Web Audio playback for the browser build.
 *
 * The engine already owns the schedule (BeatTimeline), so this class only has
 * to make a sound now without blocking. Every call goes through the JS shim in
 * webAudio.js: buffers are decoded once up front, and play() creates a source
 * node and starts it immediately, which is non-blocking and sample accurate.
 * Nothing here allocates on the network or decodes on the beat path.
 */
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
        webAudioPlay(current.name, beat.rate, stereoLeft, stereoRight)
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

private fun webAudioPlay(sound: String, rate: Float, left: Float, right: Float): Unit =
    js("MetronomeWebAudio.play(sound, rate, left, right)")

private fun webAudioRelease(): Unit = js("MetronomeWebAudio.release()")
