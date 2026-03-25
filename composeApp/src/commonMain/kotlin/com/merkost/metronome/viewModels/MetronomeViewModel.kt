package com.merkost.metronome.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.MAX_BPM
import com.merkost.metronome.model.MIN_BPM
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.StopWatchState
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.platform.currentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


class MetronomeViewModel(
    private val appDatastore: AppDatastore,
    private val hapticProvider: HapticProvider,
) : ViewModel() {
    val colorFlash = appDatastore.colorFlash
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val currentStereo = appDatastore.stereo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Pair(1, 1))
    val selectedSound = appDatastore.selectedSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ClickSound.WOOD)
    val hapticEnabled = appDatastore.hapticEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    private val metronomeMinimum = MIN_BPM
    private val metronomeMaximum = MAX_BPM

    private val tapIntervals = mutableListOf<Long>()
    private var lastTapMillis: Long? = null
    private val timeoutThreshold = 2.seconds

    val metronomeRange = (metronomeMinimum.toFloat()..metronomeMaximum.toFloat())
    val steps = metronomeMaximum - metronomeMinimum

    private val _metronomeState = MutableStateFlow<MetronomeState>(MetronomeState())
    val metronomeState = _metronomeState.asStateFlow()

    val index = MutableStateFlow(-1)

    val onboardingStep = MutableStateFlow(-1)

    init {
        viewModelScope.launch {
            val ts = appDatastore.timeSignature.first()
            _metronomeState.update { it.copy(timeSignature = ts, beats = ts.defaultBeats) }
        }
        viewModelScope.launch {
            appDatastore.onboardingComplete.first().let { complete ->
                if (!complete) {
                    onboardingStep.value = 0
                }
            }
        }
    }

    fun onOnboardingNext() {
        onboardingStep.update { if (it < 2) it + 1 else it }
    }

    fun onOnboardingBack() {
        onboardingStep.update { if (it > 0) it - 1 else it }
    }

    fun onOnboardingDismiss() {
        onboardingStep.value = -1
        viewModelScope.launch { appDatastore.saveOnboardingComplete(true) }
    }

    val isPlaying = _metronomeState.map { it.playing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val timerCoroutine = flow<Nothing> {
        startTimerCoroutine()
    }.launchIn(viewModelScope)

    fun onStopClicked() {
        _metronomeState.update { it.copy(playing = false) }
    }

    private suspend fun startTimerCoroutine() {
        isPlaying.collectLatest { isPlaying ->
            val stopWatchState = metronomeState.value.stopWatchState

            if (isPlaying) {
                val beginTimeMillis = currentTimeMillis()
                _metronomeState.update { it.copy(stopWatchState = StopWatchState(0, 0)) }
                while (true) {
                    val now = currentTimeMillis()
                    _metronomeState.update {
                        it.copy(
                            stopWatchState = StopWatchState(
                                beginTimeMillis, now - beginTimeMillis
                            )
                        )
                    }
                    delay(1000)
                }
            } else {
                appDatastore.addTotalTime(stopWatchState.elapsedTime)
            }
        }
    }


    fun onPlayPauseClicked(isPlaying: Boolean) {
        _metronomeState.update { it.copy(playing = isPlaying.not()) }
    }

    fun onSliderValueChanged(newSliderValue: Float) {
        _metronomeState.update {
            it.updateRhythm(newSliderValue.toInt())
        }
    }

    fun onSliderValueDecreased() {
        _metronomeState.update { it.updateRhythm(if (it.rhythm - 1 >= metronomeMinimum) it.rhythm - 1 else it.rhythm) }
    }

    fun onSliderValueIncreased() {
        _metronomeState.update { it.updateRhythm(if (it.rhythm + 1 <= metronomeMaximum) it.rhythm + 1 else it.rhythm) }
    }

    fun onMinusFive() {
        _metronomeState.update { it.updateRhythm(if (it.rhythm - 5 >= metronomeMinimum) it.rhythm - 5 else it.rhythm) }
    }

    fun onPlusFive() {
        _metronomeState.update { it.updateRhythm(if (it.rhythm + 5 <= metronomeMaximum) it.rhythm + 5 else it.rhythm) }
    }

    fun divideByTwo() {
        _metronomeState.update { it.updateRhythm(if (it.rhythm / 2 >= metronomeMinimum) it.rhythm / 2 else it.rhythm) }
    }

    fun multiplyByTwo() {
        _metronomeState.update { it.updateRhythm(if (it.rhythm * 2 <= metronomeMaximum) it.rhythm * 2 else it.rhythm) }
    }

    fun onTempoTap() {
        val now = currentTimeMillis()
        lastTapMillis?.let { lastTap ->
            val difference = now - lastTap
            if (difference > timeoutThreshold.inWholeMilliseconds) {
                tapIntervals.clear()
            } else {
                tapIntervals.add(difference)
                if (tapIntervals.size >= 2) {
                    updateBpmFromMedian()
                }
            }
        }
        lastTapMillis = now
    }

    private fun updateBpmFromMedian() {
        if (tapIntervals.isNotEmpty()) {
            val sortedIntervals = tapIntervals.sorted()
            val medianInterval = if (sortedIntervals.size % 2 == 1) {
                sortedIntervals[sortedIntervals.size / 2]
            } else {
                (sortedIntervals[sortedIntervals.size / 2 - 1] + sortedIntervals[sortedIntervals.size / 2]) / 2
            }
            val medianBpm = (60000 / medianInterval).toInt().coerceIn(metronomeMinimum, metronomeMaximum)
            _metronomeState.update { it.copy(rhythm = medianBpm) }
        }
    }

    fun onBallClicked(index: Int, beat: Beat) {
        val newBeat = beat.next()
        _metronomeState.update { it.copy(beats = it.beats.toMutableList().apply { set(index, newBeat) }) }
    }

    fun onTimeSignatureChanged(ts: TimeSignature) {
        _metronomeState.update { it.copy(timeSignature = ts, beats = ts.defaultBeats) }
        index.value = -1
        viewModelScope.launch { appDatastore.saveTimeSignature(ts) }
    }

    // Practice Timer
    private val _practiceTimerGoal = MutableStateFlow<Long?>(null) // null = inactive, else milliseconds
    private val _practiceTimerRemaining = MutableStateFlow(0L)
    val practiceTimerGoal: StateFlow<Long?> = _practiceTimerGoal
    val practiceTimerRemaining: StateFlow<Long> = _practiceTimerRemaining

    fun startPracticeTimer(minutes: Int) {
        val goalMs = minutes * 60_000L
        _practiceTimerGoal.value = goalMs
        _practiceTimerRemaining.value = goalMs
        viewModelScope.launch {
            while (_practiceTimerRemaining.value > 0 && _practiceTimerGoal.value != null) {
                delay(1000L)
                if (metronomeState.value.playing) {
                    _practiceTimerRemaining.update { (it - 1000L).coerceAtLeast(0L) }
                }
            }
            if (_practiceTimerRemaining.value <= 0 && _practiceTimerGoal.value != null) {
                hapticProvider.playConfirmHaptic()
            }
        }
    }

    fun dismissPracticeTimer() {
        _practiceTimerGoal.value = null
        _practiceTimerRemaining.value = 0L
    }

    fun onLongPressConfirm() {
        hapticProvider.playConfirmHaptic()
    }
}

fun <T> Sequence<T>.repeat() = sequence { while (true) yieldAll(this@repeat) }
