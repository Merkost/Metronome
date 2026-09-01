package com.merkost.metronome.practiceSets

import com.merkost.metronome.presets.PracticePreset
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

sealed interface PracticeSessionCommand {
    data class ApplyStep(val index: Int, val atBarBoundary: Boolean) : PracticeSessionCommand
    data object StartPlayback : PracticeSessionCommand
    data object PausePlayback : PracticeSessionCommand
    data class SessionFinished(
        val sourceSetId: String,
        val reason: PracticeSessionFinishReason,
    ) : PracticeSessionCommand
}

enum class PracticeSessionFinishReason {
    Completed,
    Replaced,
}

sealed interface PracticeSessionStartResult {
    data class Started(val session: ActivePracticeSession) : PracticeSessionStartResult
    data class MissingPreset(val presetId: String) : PracticeSessionStartResult
    data object InvalidSet : PracticeSessionStartResult
    data object PersistenceFailed : PracticeSessionStartResult
}

data class PracticeSessionState(
    val session: ActivePracticeSession? = null,
    val isRecovered: Boolean = false,
    val persistenceWarning: Boolean = false,
)

class PracticeSessionController(
    private val repository: PracticeSessionRepository,
    private val nextId: () -> String,
    private val nowMillis: () -> Long,
) {
    private val mutableState = MutableStateFlow(PracticeSessionState())
    private val commandChannel = Channel<PracticeSessionCommand>(Channel.UNLIMITED)
    private var pendingPersistenceAction: PendingPersistenceAction? = null

    val state: StateFlow<PracticeSessionState> = mutableState.asStateFlow()
    val commands: ReceiveChannel<PracticeSessionCommand> = commandChannel

    suspend fun recover() {
        pendingPersistenceAction = null
        val recovered = repository.session.first()?.copy(
            pendingStepIndex = null,
            playbackIntent = PracticePlaybackIntent.Paused,
        )
        mutableState.value = PracticeSessionState(
            session = recovered,
            isRecovered = recovered != null,
        )
    }

    suspend fun start(
        practiceSet: PracticeSet,
        presets: List<PracticePreset>,
    ): PracticeSessionStartResult {
        if (practiceSet.toDraft().validationError != null) return PracticeSessionStartResult.InvalidSet
        val presetsById = presets.associateBy(PracticePreset::id)
        val resolved = practiceSet.steps.map { step ->
            val preset = presetsById[step.presetId]
                ?: return PracticeSessionStartResult.MissingPreset(step.presetId)
            ResolvedPracticeStep(step.id, preset, step.target)
        }
        val now = nowMillis().coerceAtLeast(0L)
        val session = ActivePracticeSession(
            id = uniqueSessionId(),
            sourceSetId = practiceSet.id,
            setName = practiceSet.name,
            steps = resolved,
            currentStepIndex = 0,
            pendingStepIndex = null,
            elapsedMillis = 0L,
            completedBars = 0,
            playbackIntent = PracticePlaybackIntent.Running,
            targetReached = false,
            currentStepEdited = false,
            startedAtEpochMillis = now,
            lastCheckpointAtEpochMillis = now,
        )
        if (!repository.save(session)) {
            mutableState.value = PracticeSessionState()
            return PracticeSessionStartResult.PersistenceFailed
        }
        pendingPersistenceAction = null
        mutableState.value = PracticeSessionState(session = session)
        commandChannel.send(PracticeSessionCommand.ApplyStep(0, atBarBoundary = false))
        commandChannel.send(PracticeSessionCommand.StartPlayback)
        return PracticeSessionStartResult.Started(session)
    }

    suspend fun previous(isPlaying: Boolean) {
        requestStep(state.value.session?.currentStepIndex?.minus(1), isPlaying)
    }

    suspend fun next(isPlaying: Boolean) {
        requestStep(state.value.session?.currentStepIndex?.plus(1), isPlaying)
    }

    suspend fun restartCurrentStep(isPlaying: Boolean) {
        requestStep(state.value.session?.currentStepIndex, isPlaying)
    }

    suspend fun onStepApplied(index: Int) {
        val session = state.value.session ?: return
        if (session.pendingStepIndex != index || index !in session.steps.indices) return
        checkpoint(
            session.copy(
                currentStepIndex = index,
                pendingStepIndex = null,
                elapsedMillis = 0L,
                completedBars = 0,
                targetReached = false,
                currentStepEdited = false,
            ),
            force = true,
        )
    }

    suspend fun pause() {
        val session = state.value.session ?: return
        if (session.playbackIntent == PracticePlaybackIntent.Paused) return
        checkpoint(session.copy(playbackIntent = PracticePlaybackIntent.Paused), force = true)
        commandChannel.send(PracticeSessionCommand.PausePlayback)
    }

    suspend fun resume() {
        val session = state.value.session ?: return
        if (session.playbackIntent == PracticePlaybackIntent.Running) return
        checkpoint(session.copy(playbackIntent = PracticePlaybackIntent.Running), force = true)
        mutableState.value = state.value.copy(isRecovered = false)
        commandChannel.send(PracticeSessionCommand.StartPlayback)
    }

    suspend fun markCurrentStepEdited() {
        val session = state.value.session ?: return
        if (session.currentStepEdited) return
        checkpoint(session.copy(currentStepEdited = true), force = true)
    }

    suspend fun onElapsed(deltaMillis: Long, isPlaying: Boolean) {
        val session = state.value.session ?: return
        if (!isPlaying || deltaMillis <= 0L) return
        val elapsed = (session.elapsedMillis + deltaMillis).coerceAtLeast(session.elapsedMillis)
        val reached = when (val target = session.currentStep.target) {
            PracticeStepTarget.None -> false
            is PracticeStepTarget.Duration -> elapsed >= target.minutes * 60_000L
            is PracticeStepTarget.Bars -> session.completedBars >= target.count
        }
        checkpoint(
            session.copy(elapsedMillis = elapsed, targetReached = session.targetReached || reached),
            force = reached && !session.targetReached,
        )
    }

    suspend fun onBarCompleted() {
        val session = state.value.session ?: return
        val bars = (session.completedBars + 1).coerceAtLeast(session.completedBars)
        val reached = (session.currentStep.target as? PracticeStepTarget.Bars)?.let { bars >= it.count } == true
        checkpoint(
            session.copy(completedBars = bars, targetReached = session.targetReached || reached),
            force = true,
        )
    }

    suspend fun finish(reason: PracticeSessionFinishReason) {
        val session = state.value.session ?: return
        commandChannel.send(PracticeSessionCommand.PausePlayback)
        if (repository.clear()) {
            pendingPersistenceAction = null
            mutableState.value = PracticeSessionState()
            commandChannel.send(PracticeSessionCommand.SessionFinished(session.sourceSetId, reason))
        } else {
            pendingPersistenceAction = PendingPersistenceAction.FinishClear(session.sourceSetId, reason)
            mutableState.value = state.value.copy(persistenceWarning = true)
        }
    }

    suspend fun discardRecovery() {
        if (repository.clear()) {
            pendingPersistenceAction = null
            mutableState.value = PracticeSessionState()
        } else {
            pendingPersistenceAction = PendingPersistenceAction.DiscardClear
            mutableState.value = state.value.copy(persistenceWarning = true)
        }
    }

    suspend fun retryPersistence(): Boolean {
        val current = state.value
        return when (val action = pendingPersistenceAction) {
            PendingPersistenceAction.Save -> {
                val persisted = current.session?.let { repository.save(it) } == true
                if (persisted) pendingPersistenceAction = null
                mutableState.value = current.copy(persistenceWarning = !persisted)
                persisted
            }
            is PendingPersistenceAction.FinishClear -> {
                val cleared = repository.clear()
                if (cleared) {
                    pendingPersistenceAction = null
                    mutableState.value = PracticeSessionState()
                    commandChannel.send(PracticeSessionCommand.SessionFinished(action.sourceSetId, action.reason))
                } else {
                    mutableState.value = current.copy(persistenceWarning = true)
                }
                cleared
            }
            PendingPersistenceAction.DiscardClear -> {
                val cleared = repository.clear()
                if (cleared) {
                    pendingPersistenceAction = null
                    mutableState.value = PracticeSessionState()
                } else {
                    mutableState.value = current.copy(persistenceWarning = true)
                }
                cleared
            }
            null -> true
        }
    }

    private suspend fun requestStep(index: Int?, isPlaying: Boolean) {
        val session = state.value.session ?: return
        if (session.pendingStepIndex != null || index == null || index !in session.steps.indices) return
        checkpoint(session.copy(pendingStepIndex = index), force = true)
        commandChannel.send(PracticeSessionCommand.ApplyStep(index, atBarBoundary = isPlaying))
    }

    private suspend fun checkpoint(session: ActivePracticeSession, force: Boolean) {
        val now = nowMillis().coerceAtLeast(0L)
        val shouldSave = force || now - session.lastCheckpointAtEpochMillis >= CHECKPOINT_INTERVAL_MILLIS
        val updated = if (shouldSave) session.copy(lastCheckpointAtEpochMillis = now) else session
        mutableState.value = state.value.copy(session = updated)
        if (shouldSave) {
            val persisted = repository.save(updated)
            if (!persisted) {
                if (pendingPersistenceAction == null || pendingPersistenceAction == PendingPersistenceAction.Save) {
                    pendingPersistenceAction = PendingPersistenceAction.Save
                }
                mutableState.value = state.value.copy(persistenceWarning = true)
            } else if (pendingPersistenceAction == PendingPersistenceAction.Save) {
                pendingPersistenceAction = null
                mutableState.value = state.value.copy(persistenceWarning = false)
            }
        }
    }

    private fun uniqueSessionId(): String {
        var candidate: String
        do {
            candidate = nextId()
        } while (candidate.isBlank())
        return candidate
    }

    private companion object {
        const val CHECKPOINT_INTERVAL_MILLIS = 5_000L
    }

    private sealed interface PendingPersistenceAction {
        data object Save : PendingPersistenceAction
        data class FinishClear(
            val sourceSetId: String,
            val reason: PracticeSessionFinishReason,
        ) : PendingPersistenceAction
        data object DiscardClear : PendingPersistenceAction
    }
}
