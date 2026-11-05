package com.merkost.metronome.engine

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.sound_error_audio_unavailable
import metronome.shared.generated.resources.sound_error_interrupted
import metronome.shared.generated.resources.sound_error_load
import metronome.shared.generated.resources.sound_error_pause_playback
import metronome.shared.generated.resources.sound_error_play
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.model.MAX_BPM
import com.merkost.metronome.model.MIN_BPM
import com.merkost.metronome.model.UiText
import com.merkost.metronome.platform.AudioFocusController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

interface SoundPreviewController {
    val activeSound: StateFlow<ClickSound?>
    val errorMessage: StateFlow<UiText?>
    fun preview(sound: ClickSound, bpm: Int = 100, volume: Float = 1f, pan: Int = 0)
    fun updateOutput(volume: Float, pan: Int) {}
    fun stop()
}

class DefaultSoundPreviewController(
    private val player: MetronomePlayer,
    private val audioFocus: AudioFocusController,
    private val isPlaybackActive: () -> Boolean,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    private val onFailure: (Throwable) -> Unit = {},
) : SoundPreviewController {
    private val mutableActiveSound = MutableStateFlow<ClickSound?>(null)
    override val activeSound: StateFlow<ClickSound?> = mutableActiveSound.asStateFlow()
    private val mutableErrorMessage = MutableStateFlow<UiText?>(null)
    override val errorMessage: StateFlow<UiText?> = mutableErrorMessage.asStateFlow()
    private var previewJob: Job? = null
    private var generation = 0L
    private var hasFocus = false
    private var initialized = false
    private var output = 1f to 1f

    init {
        audioFocus.setOnFocusLost {
            scope.launch {
                if (mutableActiveSound.value != null) {
                    mutableErrorMessage.value = UiText.of(Res.string.sound_error_interrupted)
                    stop()
                }
            }
        }
    }

    override fun preview(sound: ClickSound, bpm: Int, volume: Float, pan: Int) {
        stop()
        mutableErrorMessage.value = null
        if (isPlaybackActive()) {
            mutableErrorMessage.value = UiText.of(Res.string.sound_error_pause_playback)
            return
        }
        val focusGranted = try {
            audioFocus.requestFocus()
        } catch (error: Throwable) {
            onFailure(error)
            false
        }
        if (!focusGranted) {
            mutableErrorMessage.value = UiText.of(Res.string.sound_error_audio_unavailable)
            return
        }
        hasFocus = true
        val session = generation
        val interval = 60_000L / bpm.coerceIn(MIN_BPM, MAX_BPM)
        updateOutput(volume, pan)
        mutableActiveSound.value = sound
        previewJob = scope.launch {
            try {
                coroutineScope {
                    val audition = currentCoroutineContext().job
                    val failureMonitor = launch(start = CoroutineStart.UNDISPATCHED) {
                        player.failures.collect { error ->
                            onFailure(error)
                            mutableErrorMessage.value = UiText.of(Res.string.sound_error_load)
                            audition.cancel()
                        }
                    }
                    try {
                        initialized = true
                        player.initialize(sound)
                        repeat(4) { index ->
                            if (isPlaybackActive()) return@coroutineScope
                            val (left, right) = output
                            player.play(if (index == 0) Beat.HIGH else Beat.LOW, left, right)
                            delay(interval)
                        }
                    } finally {
                        failureMonitor.cancel()
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableErrorMessage.value = UiText.of(Res.string.sound_error_play)
                onFailure(error)
            } finally {
                if (generation == session) finish()
            }
        }
    }

    override fun updateOutput(volume: Float, pan: Int) {
        val level = volume.takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: 0f
        val balance = pan.coerceIn(-5, 5)
        val left = level * if (balance > 0) (5 - balance) / 5f else 1f
        val right = level * if (balance < 0) (5 + balance) / 5f else 1f
        output = left to right
    }

    override fun stop() {
        generation++
        previewJob?.cancel()
        finish()
    }

    private fun finish() {
        previewJob = null
        mutableActiveSound.value = null
        if (initialized) {
            initialized = false
            player.stop()
            player.release()
        }
        if (hasFocus) {
            hasFocus = false
            audioFocus.abandonFocus()
        }
    }
}
