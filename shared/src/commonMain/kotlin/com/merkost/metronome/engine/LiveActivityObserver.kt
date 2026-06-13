package com.merkost.metronome.engine

import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.platform.LiveActivityController
import com.merkost.metronome.platform.LiveActivitySnapshot
import com.merkost.metronome.platform.TimerKind
import com.merkost.metronome.platform.currentTimeMillis
import com.merkost.metronome.viewModels.MetronomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class LiveActivityObserver(
    private val viewModel: MetronomeViewModel,
    private val controller: LiveActivityController,
    private val appDatastore: AppDatastore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastSent: LiveActivitySnapshot? = null
    private var active = false
    private var pushJob: kotlinx.coroutines.Job? = null

    fun start() {
        scope.launch {
            combine(
                viewModel.metronomeState,
                viewModel.practiceTimerGoal,
                viewModel.practiceTimerRemaining,
                appDatastore.liveActivityEnabled,
            ) { state, goal, remaining, enabled ->
                if (!enabled) null else snapshot(state, goal, remaining)
            }
                .distinctUntilChanged()
                .collect { snapshot -> dispatch(snapshot) }
        }
    }

    private fun snapshot(
        state: com.merkost.metronome.model.MetronomeState,
        goal: Long?,
        remaining: Long,
    ): LiveActivitySnapshot {
        val now = currentTimeMillis()
        val kind = when {
            goal != null -> TimerKind.COUNTDOWN
            state.stopWatchState.elapsedTime > 0 || state.playing -> TimerKind.STOPWATCH
            else -> TimerKind.NONE
        }
        return LiveActivitySnapshot(
            isPlaying = state.playing,
            bpm = state.rhythm,
            tempoName = state.tempoName,
            timeSignatureLabel = state.timeSignature.label,
            timerKind = kind,
            timerStartEpochMillis = if (state.playing && kind == TimerKind.STOPWATCH) {
                roundToSecond(now - state.stopWatchState.elapsedTime)
            } else null,
            timerEndEpochMillis = if (state.playing && kind == TimerKind.COUNTDOWN) {
                roundToSecond(now + remaining)
            } else null,
            timerFrozenMillis = if (!state.playing) {
                when (kind) {
                    TimerKind.COUNTDOWN -> remaining
                    TimerKind.STOPWATCH -> state.stopWatchState.elapsedTime
                    TimerKind.NONE -> null
                }
            } else null,
        )
    }

    private fun roundToSecond(epochMillis: Long): Long =
        (epochMillis + 500) / 1000 * 1000

    private fun dispatch(snapshot: LiveActivitySnapshot?) {
        pushJob?.cancel()
        if (snapshot == null) {
            if (active) {
                controller.end()
                active = false
                lastSent = null
            }
            return
        }
        val playTransition = snapshot.isPlaying != (lastSent?.isPlaying ?: false)
        pushJob = scope.launch {
            if (!playTransition) delay(1500)
            push(snapshot)
        }
    }

    private fun push(snapshot: LiveActivitySnapshot) {
        when {
            snapshot.isPlaying && !active -> {
                controller.start(snapshot)
                active = true
            }
            !snapshot.isPlaying && active && snapshot.timerKind == TimerKind.NONE -> {
                controller.end()
                active = false
            }
            active -> controller.update(snapshot)
            else -> return
        }
        lastSent = snapshot
    }
}
