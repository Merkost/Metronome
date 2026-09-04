package com.merkost.metronome.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.MAX_BPM
import com.merkost.metronome.model.MIN_BPM
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.bpmFromTapIntervals
import com.merkost.metronome.model.BeatDisplayStyle
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.model.GapTrainerConfig
import com.merkost.metronome.model.GradualTempoConfig
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.SavedTempo
import com.merkost.metronome.model.StopWatchState
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.platform.currentTimeMillis
import com.merkost.metronome.presets.ActivePresetTracker
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.PracticePresetRepository
import com.merkost.metronome.presets.applyPracticePreset
import com.merkost.metronome.practiceSets.PracticeSessionCommand
import com.merkost.metronome.practiceSets.PracticeSessionController
import com.merkost.metronome.practiceSets.PracticeSessionFinishReason
import com.merkost.metronome.practiceSets.PracticeSessionStartResult
import com.merkost.metronome.practiceSets.PracticeSessionState
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.practiceSets.PracticeSetRepository
import com.merkost.metronome.practiceSets.practiceAgainSet
import com.merkost.metronome.practiceSets.recordPracticeCompletion
import com.merkost.metronome.review.ReviewPromptCoordinator
import com.merkost.metronome.whatsnew.WhatsNewCoordinator
import com.merkost.metronome.review.ReviewPromptSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
    private val reviewPromptCoordinator: ReviewPromptCoordinator,
    private val practicePresetRepository: PracticePresetRepository,
    private val practiceSessionController: PracticeSessionController? = null,
    private val practiceSetRepository: PracticeSetRepository? = null,
    private val whatsNewCoordinator: WhatsNewCoordinator? = null,
) : ViewModel() {
    val colorFlash = appDatastore.colorFlash
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val currentStereo = appDatastore.stereo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Pair(1f, 1f))
    val clickVolume = appDatastore.clickVolume
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1f)
    val selectedSound = appDatastore.selectedSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ClickSound.WOOD)
    val hapticEnabled = appDatastore.hapticEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val keepScreenAwake = appDatastore.keepScreenAwake
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val countInEnabled = appDatastore.countInEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val beatDisplayStyle = appDatastore.beatDisplayStyle
        .stateIn(viewModelScope, SharingStarted.Eagerly, BeatDisplayStyle.DOTS)

    private val _countInRemaining = MutableStateFlow(0)
    val countInRemaining: StateFlow<Int> = _countInRemaining
    private var suppressReviewForCurrentPause = false

    private val activePresetTracker = ActivePresetTracker()
    val activePresetState = activePresetTracker.state
    private val emptyPracticeSessionState = MutableStateFlow(PracticeSessionState())
    val practiceSessionState: StateFlow<PracticeSessionState> =
        practiceSessionController?.state ?: emptyPracticeSessionState
    private val mutablePracticeSessionStartResults = MutableSharedFlow<PracticeSessionStartResult>(
        extraBufferCapacity = 1,
    )
    val practiceSessionStartResults: SharedFlow<PracticeSessionStartResult> =
        mutablePracticeSessionStartResults.asSharedFlow()
    val recentPracticeSet: StateFlow<PracticeSet?> = combine(
        practiceSetRepository?.sets ?: flowOf(emptyList()),
        practiceSessionState,
    ) { sets, sessionState ->
        practiceAgainSet(sets, sessionState.session?.sourceSetId)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val mutablePracticeCompletionEvents = MutableSharedFlow<PracticeCompletionEvent>(
        extraBufferCapacity = 1,
    )
    val practiceCompletionEvents: SharedFlow<PracticeCompletionEvent> =
        mutablePracticeCompletionEvents.asSharedFlow()
    private var pendingAfterSessionFinish: (() -> Unit)? = null

    fun onCountInTick(remaining: Int) {
        _countInRemaining.value = remaining
    }

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

    val whatsNewVersion = MutableStateFlow<String?>(null)


    fun onOnboardingNext() {
        onboardingStep.update { if (it < 2) it + 1 else it }
    }

    fun onOnboardingBack() {
        onboardingStep.update { if (it > 0) it - 1 else it }
    }

    fun onWhatsNewDismissed() {
        whatsNewVersion.value = null
        viewModelScope.launch { whatsNewCoordinator?.markSeen() }
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
        if (practiceSessionState.value.session != null) {
            viewModelScope.launch { practiceSessionController?.pause() }
        } else {
            _metronomeState.update { it.copy(playing = false) }
        }
    }

    fun pauseForPresetManagement() {
        if (!_metronomeState.value.playing) return
        suppressReviewForCurrentPause = true
        if (practiceSessionState.value.session != null) {
            viewModelScope.launch { practiceSessionController?.pause() }
        } else {
            _metronomeState.update { it.copy(playing = false) }
        }
    }

    private var sessionBaseMillis = 0L

    private suspend fun startTimerCoroutine() {
        isPlaying.collectLatest { isPlaying ->
            if (isPlaying) {
                val beginTimeMillis = currentTimeMillis()
                var lastProgressMillis = beginTimeMillis
                while (true) {
                    val now = currentTimeMillis()
                    _metronomeState.update {
                        it.copy(
                            stopWatchState = StopWatchState(
                                beginTimeMillis, sessionBaseMillis + now - beginTimeMillis
                            )
                        )
                    }
                    practiceSessionController?.onElapsed(
                        deltaMillis = (now - lastProgressMillis).coerceAtLeast(0L),
                        isPlaying = true,
                    )
                    lastProgressMillis = now
                    delay(1000)
                }
            } else {
                val elapsed = metronomeState.value.stopWatchState.elapsedTime
                appDatastore.addTotalTime(elapsed - sessionBaseMillis)
                sessionBaseMillis = elapsed
                if (suppressReviewForCurrentPause) {
                    suppressReviewForCurrentPause = false
                } else {
                    reviewPromptCoordinator.requestAfterQuietPause {
                        ReviewPromptSnapshot(
                            totalPracticeMillis = appDatastore.totalTime.first(),
                            sessionPracticeMillis = elapsed,
                            isPlaying = metronomeState.value.playing,
                            hasActiveTimer = practiceTimerGoal.value != null,
                            hasActiveTempoTrainer = gradualTempoConfig.value != null,
                            hasActiveGapTrainer = gapTrainerConfig.value != null,
                            hasActivePracticeSession = practiceSessionState.value.session != null,
                            isTimerSheetVisible = timerSheetVisible,
                            isTempoSheetVisible = tempoSheetVisible,
                            isOnboardingVisible = onboardingStep.value >= 0,
                            isPresetManagementVisible = presetManagementVisible,
                        )
                    }
                }
            }
        }
    }


    fun onPlayPauseClicked(isPlaying: Boolean) {
        if (practiceSessionState.value.session != null) {
            viewModelScope.launch {
                if (isPlaying) practiceSessionController?.pause() else practiceSessionController?.resume()
            }
        } else {
            _metronomeState.update { it.copy(playing = isPlaying.not()) }
        }
    }

    fun onSliderValueChanged(newSliderValue: Float) {
        markConfigurationEdited()
        _metronomeState.update {
            it.updateRhythm(newSliderValue.toInt())
        }
    }

    fun onSliderValueDecreased() {
        markConfigurationEdited()
        _metronomeState.update { it.updateRhythm(if (it.rhythm - 1 >= metronomeMinimum) it.rhythm - 1 else it.rhythm) }
    }

    fun onSliderValueIncreased() {
        markConfigurationEdited()
        _metronomeState.update { it.updateRhythm(if (it.rhythm + 1 <= metronomeMaximum) it.rhythm + 1 else it.rhythm) }
    }

    fun onMinusFive() {
        markConfigurationEdited()
        _metronomeState.update { it.updateRhythm(if (it.rhythm - 5 >= metronomeMinimum) it.rhythm - 5 else it.rhythm) }
    }

    fun onPlusFive() {
        markConfigurationEdited()
        _metronomeState.update { it.updateRhythm(if (it.rhythm + 5 <= metronomeMaximum) it.rhythm + 5 else it.rhythm) }
    }

    fun divideByTwo() {
        markConfigurationEdited()
        _metronomeState.update { it.updateRhythm(if (it.rhythm / 2 >= metronomeMinimum) it.rhythm / 2 else it.rhythm) }
    }

    fun multiplyByTwo() {
        markConfigurationEdited()
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
        val medianBpm = bpmFromTapIntervals(tapIntervals) ?: return
        markConfigurationEdited()
        _metronomeState.update { it.copy(rhythm = medianBpm) }
    }

    fun onBallClicked(index: Int, beat: Beat) {
        markConfigurationEdited()
        val newBeat = beat.next()
        _metronomeState.update { it.copy(beats = it.beats.toMutableList().apply { set(index, newBeat) }) }
    }

    fun onTimeSignatureChanged(ts: TimeSignature) {
        markConfigurationEdited()
        _metronomeState.update { it.copy(timeSignature = ts, beats = ts.defaultBeats) }
        index.value = -1
        viewModelScope.launch { appDatastore.saveTimeSignature(ts) }
    }

    fun onSubdivisionChanged(subdivision: Subdivision) {
        markConfigurationEdited()
        _metronomeState.update { it.copy(subdivision = subdivision) }
        viewModelScope.launch { appDatastore.saveSubdivision(subdivision) }
    }

    val totalPracticeTime = appDatastore.totalTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0L)

    val todayPracticeTime = appDatastore.todayPracticeTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0L)

    val practiceStreak = appDatastore.practiceStreak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val savedTempos = appDatastore.savedTempos
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun saveCurrentTempo() {
        val state = _metronomeState.value
        val tempo = SavedTempo(state.rhythm, state.timeSignature, state.subdivision)
        viewModelScope.launch { appDatastore.addSavedTempo(tempo) }
    }

    fun applySavedTempo(tempo: SavedTempo) {
        val tsChanged = _metronomeState.value.timeSignature != tempo.timeSignature
        _metronomeState.update {
            it.copy(
                rhythm = tempo.bpm,
                timeSignature = tempo.timeSignature,
                beats = if (tsChanged) tempo.timeSignature.defaultBeats else it.beats,
                subdivision = tempo.subdivision,
            )
        }
        if (tsChanged) {
            index.value = -1
        }
        viewModelScope.launch {
            appDatastore.saveTimeSignature(tempo.timeSignature)
            appDatastore.saveSubdivision(tempo.subdivision)
        }
    }

    fun deleteSavedTempo(tempo: SavedTempo) {
        viewModelScope.launch { appDatastore.removeSavedTempo(tempo) }
    }

    fun applyPracticePreset(preset: PracticePreset) {
        viewModelScope.launch { practiceSessionController?.markCurrentStepEdited() }
        activePresetTracker.request(preset, _metronomeState.value.playing)?.let(::commitPracticePreset)
    }

    fun onPresetStored(preset: PracticePreset) {
        activePresetTracker.applied(preset)
    }

    fun onPresetDeleted(id: String) {
        activePresetTracker.removed(id)
    }

    private fun commitPracticePreset(preset: PracticePreset) {
        stopGradualTempo()
        stopGapTrainer()
        _metronomeState.update { it.applyPracticePreset(preset) }
        index.value = -1
        viewModelScope.launch {
            appDatastore.saveTimeSignature(preset.timeSignature)
            appDatastore.saveSubdivision(preset.subdivision)
            appDatastore.saveCountInEnabled(preset.countInEnabled)
            practicePresetRepository.markUsed(preset.id)
        }
    }

    val lastTimerMinutes = appDatastore.lastTimerMinutes
        .stateIn(viewModelScope, SharingStarted.Eagerly, 15)

    val lastTrainerConfig = appDatastore.lastTrainerConfig
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _practiceTimerGoal = MutableStateFlow<Long?>(null)
    private val _practiceTimerRemaining = MutableStateFlow(0L)
    val practiceTimerGoal: StateFlow<Long?> = _practiceTimerGoal
    val practiceTimerRemaining: StateFlow<Long> = _practiceTimerRemaining
    private var practiceTimerJob: Job? = null
    private var practiceTimerDoneJob: Job? = null

    fun startPracticeTimer(minutes: Int) {
        viewModelScope.launch { appDatastore.saveLastTimerMinutes(minutes) }
        beginPracticeTimer(minutes)
    }

    private fun beginPracticeTimer(minutes: Int) {
        practiceTimerJob?.cancel()
        practiceTimerDoneJob?.cancel()
        val goalMs = minutes * 60_000L
        _practiceTimerGoal.value = goalMs
        _practiceTimerRemaining.value = goalMs
        _metronomeState.update { it.copy(playing = true) }
        practiceTimerJob = viewModelScope.launch {
            while (_practiceTimerRemaining.value > 0 && _practiceTimerGoal.value != null) {
                delay(1000L)
                if (metronomeState.value.playing) {
                    _practiceTimerRemaining.update { (it - 1000L).coerceAtLeast(0L) }
                }
            }
            if (_practiceTimerRemaining.value <= 0 && _practiceTimerGoal.value != null) {
                if (hapticEnabled.value) {
                    hapticProvider.playConfirmHaptic()
                }
                practiceTimerDoneJob = viewModelScope.launch {
                    delay(8000L)
                    if (timerSheetVisible) {
                        timerAutoDismissPending = true
                    } else {
                        dismissPracticeTimer()
                    }
                }
            }
        }
    }

    private var timerSheetVisible = false
    private var timerAutoDismissPending = false

    fun setTimerSheetVisible(visible: Boolean) {
        timerSheetVisible = visible
        if (!visible && timerAutoDismissPending) {
            timerAutoDismissPending = false
            dismissPracticeTimer()
        }
    }

    fun extendPracticeTimer(minutes: Int) {
        val goal = _practiceTimerGoal.value ?: return
        val extension = minutes * 60_000L
        practiceTimerDoneJob?.cancel()
        if (_practiceTimerRemaining.value <= 0L) {
            beginPracticeTimer(minutes)
        } else {
            _practiceTimerGoal.value = goal + extension
            _practiceTimerRemaining.update { it + extension }
        }
    }

    fun restartPracticeTimer() {
        val goal = _practiceTimerGoal.value ?: return
        beginPracticeTimer((goal / 60_000L).toInt().coerceAtLeast(1))
    }

    fun dismissPracticeTimer() {
        practiceTimerJob?.cancel()
        practiceTimerJob = null
        practiceTimerDoneJob?.cancel()
        practiceTimerDoneJob = null
        timerAutoDismissPending = false
        _practiceTimerGoal.value = null
        _practiceTimerRemaining.value = 0L
    }

    private val _gradualTempoConfig = MutableStateFlow<GradualTempoConfig?>(null)
    val gradualTempoConfig: StateFlow<GradualTempoConfig?> = _gradualTempoConfig
    private val _gradualTempoCurrentBar = MutableStateFlow(0)
    val gradualTempoCurrentBar: StateFlow<Int> = _gradualTempoCurrentBar
    private var gradualTempoDismissJob: Job? = null

    fun startGradualTempo(config: GradualTempoConfig) {
        markConfigurationEdited()
        gradualTempoDismissJob?.cancel()
        gradualTempoDismissJob = null
        _gradualTempoConfig.value = config
        _gradualTempoCurrentBar.value = 0
        _metronomeState.update { it.copy(rhythm = config.startBpm, playing = true) }
        viewModelScope.launch { appDatastore.saveLastTrainerConfig(config) }
    }

    private var tempoSheetVisible = false
    private var presetManagementVisible = false
    private var trainerAutoDismissPending = false

    fun setTempoSheetVisible(visible: Boolean) {
        tempoSheetVisible = visible
        if (!visible && trainerAutoDismissPending) {
            trainerAutoDismissPending = false
            stopGradualTempo()
        }
    }

    fun setPresetManagementVisible(visible: Boolean) {
        presetManagementVisible = visible
    }

    fun stopGradualTempo(resetToStart: Boolean = false) {
        val config = _gradualTempoConfig.value
        gradualTempoDismissJob?.cancel()
        gradualTempoDismissJob = null
        trainerAutoDismissPending = false
        _gradualTempoConfig.value = null
        _gradualTempoCurrentBar.value = 0
        if (resetToStart && config != null) {
            _metronomeState.update { it.copy(rhythm = config.startBpm) }
        }
    }

    fun incrementGradualTempo() {
        val config = _gradualTempoConfig.value ?: return
        _gradualTempoCurrentBar.update { it + 1 }
        val currentBar = _gradualTempoCurrentBar.value
        if (currentBar > 0 && currentBar % config.barsPerStep == 0) {
            val newBpm = config.nextBpmFrom(_metronomeState.value.rhythm)
            _metronomeState.update { it.copy(rhythm = newBpm) }
            if (config.isComplete(newBpm) && gradualTempoDismissJob == null) {
                if (hapticEnabled.value) {
                    hapticProvider.playConfirmHaptic()
                }
                gradualTempoDismissJob = viewModelScope.launch {
                    delay(5000)
                    if (tempoSheetVisible) {
                        trainerAutoDismissPending = true
                    } else {
                        stopGradualTempo()
                    }
                }
            }
        }
    }

    private val _gapTrainerConfig = MutableStateFlow<GapTrainerConfig?>(null)
    val gapTrainerConfig: StateFlow<GapTrainerConfig?> = _gapTrainerConfig

    private val _gapTrainerStartBar = MutableStateFlow(0)
    val gapTrainerStartBar: StateFlow<Int> = _gapTrainerStartBar

    private val _currentBar = MutableStateFlow(0)
    val currentBar: StateFlow<Int> = _currentBar

    val lastGapConfig = appDatastore.lastGapConfig
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun startGapTrainer(config: GapTrainerConfig) {
        _gapTrainerConfig.value = config
        _gapTrainerStartBar.value = _currentBar.value
        _metronomeState.update { it.copy(playing = true) }
        viewModelScope.launch { appDatastore.saveLastGapConfig(config) }
    }

    fun updateGapTrainer(config: GapTrainerConfig) {
        _gapTrainerConfig.value = config
        _gapTrainerStartBar.value = _currentBar.value
        viewModelScope.launch { appDatastore.saveLastGapConfig(config) }
    }

    fun stopGapTrainer() {
        _gapTrainerConfig.value = null
    }

    fun onBarReset() {
        _currentBar.value = 0
        _gapTrainerStartBar.value = 0
    }

    fun onBarCompleted(barNumber: Int) {
        val pendingSessionIndex = practiceSessionState.value.session?.pendingStepIndex
        activePresetTracker.consumePendingAtBarBoundary()?.let(::commitPracticePreset)
        practiceSessionController?.let { controller ->
            viewModelScope.launch {
                controller.onBarCompleted()
                pendingSessionIndex?.let { controller.onStepApplied(it) }
            }
        }
        _currentBar.value = barNumber
        if (_gradualTempoConfig.value != null) {
            incrementGradualTempo()
        }
    }

    fun onLongPressConfirm() {
        hapticProvider.playConfirmHaptic()
    }

    fun hasStructuredPracticeConflict(): Boolean =
        practiceSessionState.value.session != null ||
            practiceTimerGoal.value != null ||
            gradualTempoConfig.value != null ||
            gapTrainerConfig.value != null

    fun startPracticeSet(practiceSet: PracticeSet) {
        val controller = practiceSessionController
        if (controller == null) {
            mutablePracticeSessionStartResults.tryEmit(PracticeSessionStartResult.PersistenceFailed)
            return
        }
        if (controller.state.value.session != null) {
            pendingAfterSessionFinish = { startPracticeSet(practiceSet) }
            viewModelScope.launch { controller.finish(PracticeSessionFinishReason.Replaced) }
            return
        }
        dismissPracticeTimer()
        stopGradualTempo()
        stopGapTrainer()
        viewModelScope.launch {
            val result = controller.start(practiceSet, practicePresetRepository.presets.first())
            when (result) {
                is PracticeSessionStartResult.Started -> practiceSetRepository?.markStarted(practiceSet.id)
                else -> Unit
            }
            mutablePracticeSessionStartResults.emit(result)
        }
    }

    fun resumePracticeSession() {
        viewModelScope.launch { practiceSessionController?.resume() }
    }

    fun retryPracticeSessionPersistence() {
        viewModelScope.launch { practiceSessionController?.retryPersistence() }
    }

    fun previousPracticeStep() {
        viewModelScope.launch { practiceSessionController?.previous(_metronomeState.value.playing) }
    }

    fun nextPracticeStep() {
        viewModelScope.launch { practiceSessionController?.next(_metronomeState.value.playing) }
    }

    fun restartPracticeStep() {
        viewModelScope.launch { practiceSessionController?.restartCurrentStep(_metronomeState.value.playing) }
    }

    fun togglePracticeSessionPlayback() {
        viewModelScope.launch {
            val session = practiceSessionState.value.session ?: return@launch
            if (session.playbackIntent == com.merkost.metronome.practiceSets.PracticePlaybackIntent.Running) {
                practiceSessionController?.pause()
            } else {
                practiceSessionController?.resume()
            }
        }
    }

    fun finishPracticeSession() {
        viewModelScope.launch {
            practiceSessionController?.finish(PracticeSessionFinishReason.Completed)
        }
    }

    fun replacePracticeSessionWithTimer(minutes: Int) {
        replacePracticeSession { startPracticeTimer(minutes) }
    }

    fun replacePracticeSessionWithTempoTrainer(config: GradualTempoConfig) {
        replacePracticeSession { startGradualTempo(config) }
    }

    fun replacePracticeSessionWithGapTrainer(config: GapTrainerConfig) {
        replacePracticeSession { startGapTrainer(config) }
    }

    private fun replacePracticeSession(action: () -> Unit) {
        val controller = practiceSessionController
        if (controller?.state?.value?.session == null) {
            action()
            return
        }
        pendingAfterSessionFinish = action
        viewModelScope.launch { controller.finish(PracticeSessionFinishReason.Replaced) }
    }

    private suspend fun handlePracticeSessionCommand(
        command: PracticeSessionCommand,
        controller: PracticeSessionController,
    ) {
        when (command) {
            is PracticeSessionCommand.ApplyStep -> {
                val preset = controller.state.value.session?.steps?.getOrNull(command.index)?.preset ?: return
                if (command.atBarBoundary) {
                    activePresetTracker.pending(preset)
                } else {
                    activePresetTracker.applied(preset)
                    commitPracticePreset(preset)
                    controller.onStepApplied(command.index)
                }
            }
            PracticeSessionCommand.StartPlayback -> _metronomeState.update { it.copy(playing = true) }
            PracticeSessionCommand.PausePlayback -> _metronomeState.update { it.copy(playing = false) }
            is PracticeSessionCommand.SessionFinished -> {
                if (!recordPracticeCompletion(command, practiceSetRepository)) {
                    mutablePracticeCompletionEvents.emit(PracticeCompletionEvent.StorageFailure)
                }
                pendingAfterSessionFinish?.also { pendingAfterSessionFinish = null }?.invoke()
            }
        }
    }

    private fun markConfigurationEdited() {
        activePresetTracker.changed()
        viewModelScope.launch { practiceSessionController?.markCurrentStepEdited() }
    }

    init {
        viewModelScope.launch {
            val ts = appDatastore.timeSignature.first()
            _metronomeState.update { it.copy(timeSignature = ts, beats = ts.defaultBeats) }
        }
        viewModelScope.launch {
            val subdivision = appDatastore.subdivision.first()
            _metronomeState.update { it.copy(subdivision = subdivision) }
        }
        viewModelScope.launch {
            appDatastore.onboardingComplete.first().let { complete ->
                if (!complete) {
                    onboardingStep.value = 0
                }
            }
        }
        viewModelScope.launch {
            whatsNewCoordinator?.let { coordinator ->
                if (coordinator.shouldShow()) {
                    whatsNewVersion.value = coordinator.currentVersion()
                }
            }
        }
        viewModelScope.launch {
            appDatastore.countInEnabled.collect { enabled ->
                val active = activePresetState.value.active
                if (active != null && active.countInEnabled != enabled) {
                    markConfigurationEdited()
                }
            }
        }
        practiceSessionController?.let { controller ->
            viewModelScope.launch {
                controller.recover()
                controller.state.value.session?.currentStep?.preset?.let(::commitPracticePreset)
            }
            viewModelScope.launch {
                for (command in controller.commands) {
                    handlePracticeSessionCommand(command, controller)
                }
            }
        }
    }
}

sealed interface PracticeCompletionEvent {
    data object StorageFailure : PracticeCompletionEvent
}

fun <T> Sequence<T>.repeat() = sequence { while (true) yieldAll(this@repeat) }
