package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MetronomePlayerWasm(private val channel: String = "main") : MetronomePlayer {

    private val mutableFailures = MutableSharedFlow<Throwable>(extraBufferCapacity = 4)
    override val failures: Flow<Throwable> = mutableFailures.asSharedFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var failureMonitor: Job? = null
    private var current: ClickSound = ClickSound.WOOD
    private var ready = false

    override fun initialize(initialSound: ClickSound) {
        current = initialSound
        webAudioInit(channel, initialSound.name)
        ready = true
        failureMonitor?.cancel()
        failureMonitor = scope.launch {
            while (ready) {
                webAudioTakeError(channel)?.let { mutableFailures.emit(IllegalStateException(it)) }
                delay(100)
            }
        }
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        if (!ready || beat == Beat.MUTE) return
        val gain = maxOf(stereoLeft, stereoRight)
        if (gain <= 0f) return
        val pan = (stereoRight - stereoLeft) / gain
        webAudioPlay(channel, current.name, beat.rate, gain, pan)
    }

    override fun stop() {
        webAudioStop(channel)
    }

    override fun release() {
        ready = false
        failureMonitor?.cancel()
        failureMonitor = null
        webAudioRelease(channel)
    }

    override fun switchSound(sound: ClickSound) {
        current = sound
    }
}

private fun webAudioInit(channel: String, sound: String): Unit = js("MetronomeWebAudio.init(channel, sound)")

private fun webAudioPlay(channel: String, sound: String, rate: Float, gain: Float, pan: Float): Unit =
    js("MetronomeWebAudio.play(channel, sound, rate, gain, pan)")

private fun webAudioStop(channel: String): Unit = js("MetronomeWebAudio.stop(channel)")

private fun webAudioRelease(channel: String): Unit = js("MetronomeWebAudio.release(channel)")

private fun webAudioTakeError(channel: String): String? = js("MetronomeWebAudio.takeError(channel)")
