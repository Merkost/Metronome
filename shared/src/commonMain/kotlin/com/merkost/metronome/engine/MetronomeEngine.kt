package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.BeatPulse
import com.merkost.metronome.model.phraseBeat
import com.merkost.metronome.platform.AudioFocusController
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.viewModels.repeat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

private const val SUB_CLICK_VOLUME = 0.35f
private val LONG_BEAT_GAP = 1500.milliseconds

class MetronomeEngine(
    private val player: MetronomePlayer,
    private val viewModel: MetronomeViewModel,
    private val hapticProvider: HapticProvider,
    private val audioFocus: AudioFocusController,
    private val soundPreview: SoundPreviewController? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
) {
    private var job: Job? = null

    private var beatCount = 0
    private var barNumber = 0

    fun start() {
        val initialSound = viewModel.selectedSound.value
        player.initialize(initialSound)
        audioFocus.setOnFocusLost { viewModel.onStopClicked() }
        job = coroutineScope.launch {
            launch {
                viewModel.selectedSound.collectLatest { sound ->
                    player.switchSound(sound)
                }
            }
            viewModel.isPlaying.collectLatest { playing ->
                if (playing) {
                    withContext(Dispatchers.Main.immediate) { soundPreview?.stop() }
                    if (!requestPlaybackFocus(audioFocus, viewModel::onStopClicked)) {
                        return@collectLatest
                    }
                    var isRestart = false
                    var activeTimeline: BeatTimeline? = null
                    var pulseOrdinal = 0L
                    viewModel.metronomeState
                        .map { it.beats.size }
                        .distinctUntilChanged()
                        .collectLatest { beatsCount ->
                            val pulseGeneration = viewModel.beginBeatSession()
                            viewModel.index.update { -1 }
                            beatCount = 0
                            barNumber = 0
                            viewModel.onBarReset()
                            viewModel.onCountInTick(0)

                            if (!isRestart) {
                                val lead = player.warmUpLead
                                player.keepAlive()
                                if (lead > Duration.ZERO) delay(lead)
                            }

                            val timeline = activeTimeline?.also { it.resumeAfterRestart() }
                                ?: BeatTimeline(timeSource)
                            activeTimeline = timeline
                            val playbackContext = currentCoroutineContext()
                            fun submittedPulse(interval: Duration, ordinal: Long) {
                                if (playbackContext.isActive && viewModel.metronomeState.value.playing) {
                                    viewModel.onBeatPulse(pulseGeneration, BeatPulse(timeSource.markNow(), interval, ordinal))
                                }
                            }

                            if (!isRestart && viewModel.countInEnabled.value) {
                                for (remaining in beatsCount downTo 1) {
                                    delayUntil(timeline.deadline)
                                    viewModel.onCountInTick(remaining)
                                    val stereo = viewModel.currentStereo.value
                                    val volume = viewModel.clickVolume.value
                                    val interval = viewModel.metronomeState.value.beatDuration
                                    val ordinal = pulseOrdinal++
                                    player.play(Beat.HIGH, stereo.first * volume, stereo.second * volume) {
                                        submittedPulse(interval, ordinal)
                                    }
                                    if (viewModel.hapticEnabled.value) {
                                        hapticProvider.playBeatHaptic(Beat.HIGH)
                                    }
                                    timeline.advance(interval)
                                }
                                viewModel.onCountInTick(0)
                            }
                            isRestart = true

                            createBeatsSequence(beatsCount).collect { index ->
                                val beatStart = timeline.deadline
                                delayUntil(beatStart)
                                val state = viewModel.metronomeState.value
                                val beat = phraseBeat(state.beats[index], index, barNumber, viewModel.phraseLength.value)
                                val stereo = viewModel.currentStereo.value
                                val volume = viewModel.clickVolume.value
                                if (index == 0) viewModel.onBarStarted(barNumber)
                                val muted = viewModel.isGapBarMuted(barNumber)
                                val interval = state.beatDuration
                                timeline.markBeat(interval)
                                viewModel.index.update { index }

                                val events = beatEvents(
                                    beat = beat,
                                    interval = interval,
                                    clicksPerBeat = state.subdivision.clicksPerBeat,
                                    muted = muted,
                                    stereoLeft = stereo.first,
                                    stereoRight = stereo.second,
                                    volume = volume,
                                    subClickVolume = SUB_CLICK_VOLUME,
                                )
                                val ordinal = pulseOrdinal++
                                if (events.firstOrNull()?.offset != Duration.ZERO) {
                                    viewModel.onBeatPulse(pulseGeneration, BeatPulse(beatStart, interval, ordinal))
                                }
                                if (events.none { it.offset == Duration.ZERO }) player.keepAlive()
                                for (event in events) {
                                    val eventDeadline = beatStart + event.offset
                                    delayUntil(eventDeadline)
                                    val stale = timeline.isStale(eventDeadline, interval)
                                    if (stale && event.offset == Duration.ZERO) {
                                        viewModel.onBeatPulse(pulseGeneration, BeatPulse(beatStart, interval, ordinal))
                                    }
                                    if (!stale) {
                                        if (event.offset == Duration.ZERO) {
                                            player.play(event.beat, event.leftVolume, event.rightVolume) {
                                                submittedPulse(interval, ordinal)
                                            }
                                            if (!muted && beat != Beat.MUTE && viewModel.hapticEnabled.value) {
                                                hapticProvider.playBeatHaptic(beat)
                                            }
                                        } else {
                                            player.play(event.beat, event.leftVolume, event.rightVolume)
                                        }
                                    }
                                }

                                timeline.advance(interval)

                                beatCount++
                                if (beatCount >= beatsCount) {
                                    beatCount = 0
                                    barNumber++
                                    viewModel.onBarCompleted(barNumber)
                                }

                                if (interval > LONG_BEAT_GAP && state.subdivision.clicksPerBeat == 1) {
                                    delayUntil(beatStart + interval / 2)
                                    player.keepAlive()
                                }
                            }
                        }
                } else {
                    audioFocus.abandonFocus()
                    player.stop()
                    viewModel.beginBeatSession()
                    viewModel.index.update { -1 }
                    viewModel.onCountInTick(0)
                }
            }
        }
    }

    private suspend fun delayUntil(target: ComparableTimeMark) {
        val remaining = -target.elapsedNow()
        if (remaining > Duration.ZERO) delay(remaining)
    }

    fun stop() {
        job?.cancel()
        viewModel.beginBeatSession()
        player.stop()
    }

    fun release() {
        stop()
        player.release()
    }

    private fun createBeatsSequence(beatsCount: Int): Flow<Int> =
        (0 until beatsCount).asSequence().repeat().asFlow()
}
