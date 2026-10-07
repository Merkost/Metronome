package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.model.MAX_BPM
import com.merkost.metronome.model.MIN_BPM
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
    val errorMessage: StateFlow<String?>
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
    private val mutableErrorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = mutableErrorMessage.asStateFlow()
    private var previewJob: Job? = null
    private var generation = 0L
    private var hasFocus = false
    private var initialized = false
    private var output = 1f to 1f

    init {
        audioFocus.setOnFocusLost {
            scope.launch {
                if (mutableActiveSound.value != null) {
                    mutableErrorMessage.value = "Preview was interrupted. Try again when you're ready."
                    stop()
                }
            }
        }
    }

    override fun preview(sound: ClickSound, bpm: Int, volume: Float, pan: Int) {
        stop()
        mutableErrorMessage.value = null
        if (isPlaybackActive()) {
            mutableErrorMessage.value = "Pause playback to preview a sound."
            return
        }
        val focusGranted = try {
            audioFocus.requestFocus()
        } catch (error: Throwable) {
            onFailure(error)
            false
        }
        if (!focusGranted) {
            mutableErrorMessage.value = "Audio is unavailable right now. Try the preview again."
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
                            mutableErrorMessage.value = "Couldn't load this sound. Try another sound or preview again."
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
                mutableErrorMessage.value = "Couldn't play the preview. Try another sound or preview again."
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
