package com.merkost.metronome.practiceSets

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.presets.PracticePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PracticeSessionControllerTest {
    @Test
    fun targetReachedNeverStopsOrAdvances() = runTest {
        val controller = recoveredController(session(target = PracticeStepTarget.Bars(2)))

        controller.onBarCompleted()
        controller.onBarCompleted()

        assertTrue(controller.state.value.session!!.targetReached)
        assertEquals(0, controller.state.value.session!!.currentStepIndex)
        assertTrue(controller.commands.tryReceive().isFailure)
    }

    @Test
    fun nextQueuesWhilePlayingAndResetsOnlyAfterAcknowledgement() = runTest {
        val controller = recoveredController(session(stepCount = 2))

        controller.next(isPlaying = true)

        assertEquals(1, controller.state.value.session!!.pendingStepIndex)
        assertEquals(
            PracticeSessionCommand.ApplyStep(1, atBarBoundary = true),
            controller.commands.receive(),
        )
        controller.onStepApplied(1)
        assertEquals(1, controller.state.value.session!!.currentStepIndex)
        assertEquals(0L, controller.state.value.session!!.elapsedMillis)
        assertEquals(0, controller.state.value.session!!.completedBars)
    }

    @Test
    fun pausedNavigationAppliesImmediatelyAndIgnoresRepeatedPendingInput() = runTest {
        val controller = recoveredController(session(stepCount = 3))

        controller.next(isPlaying = false)
        controller.next(isPlaying = false)

        assertEquals(1, controller.state.value.session!!.pendingStepIndex)
        assertEquals(PracticeSessionCommand.ApplyStep(1, false), controller.commands.receive())
        assertTrue(controller.commands.tryReceive().isFailure)
        controller.onStepApplied(1)
        controller.previous(isPlaying = false)
        assertEquals(PracticeSessionCommand.ApplyStep(0, false), controller.commands.receive())
    }

    @Test
    fun durationCountsOnlyWhilePlayingAndBarsOnlyAffectBarTargets() = runTest {
        val duration = recoveredController(session(target = PracticeStepTarget.Duration(1)))
        duration.onElapsed(30_000L, isPlaying = false)
        duration.onElapsed(30_000L, isPlaying = true)
        assertEquals(30_000L, duration.state.value.session!!.elapsedMillis)
        assertFalse(duration.state.value.session!!.targetReached)
        duration.onElapsed(30_000L, isPlaying = true)
        assertTrue(duration.state.value.session!!.targetReached)

        val bars = recoveredController(session(target = PracticeStepTarget.Bars(1)))
        bars.onElapsed(100_000L, isPlaying = true)
        assertFalse(bars.state.value.session!!.targetReached)
        bars.onBarCompleted()
        assertTrue(bars.state.value.session!!.targetReached)
    }

    @Test
    fun startsFromResolvedSnapshotsAndRejectsMissingPreset() = runTest {
        val repository = FakeSessionRepository()
        val controller = PracticeSessionController(repository, nextId = { "session-1" }, nowMillis = { 100L })
        val practiceSet = practiceSet(stepCount = 2)

        assertEquals(
            PracticeSessionStartResult.MissingPreset("preset-1"),
            controller.start(practiceSet, listOf(preset(0))),
        )
        assertNull(controller.state.value.session)

        assertIs<PracticeSessionStartResult.Started>(
            controller.start(practiceSet, listOf(preset(0), preset(1))),
        )
        assertEquals(PracticeSessionCommand.ApplyStep(0, false), controller.commands.receive())
        assertEquals(PracticeSessionCommand.StartPlayback, controller.commands.receive())
        assertEquals(PracticePlaybackIntent.Running, controller.state.value.session!!.playbackIntent)
    }

    @Test
    fun pauseResumeRestartAndManualEditsAreExplicit() = runTest {
        val controller = recoveredController(session())

        controller.resume()
        assertEquals(PracticeSessionCommand.StartPlayback, controller.commands.receive())
        assertEquals(PracticePlaybackIntent.Running, controller.state.value.session!!.playbackIntent)
        controller.markCurrentStepEdited()
        assertTrue(controller.state.value.session!!.currentStepEdited)
        controller.restartCurrentStep(isPlaying = true)
        assertEquals(PracticeSessionCommand.ApplyStep(0, true), controller.commands.receive())
        controller.onStepApplied(0)
        assertFalse(controller.state.value.session!!.currentStepEdited)
        controller.pause()
        assertEquals(PracticeSessionCommand.PausePlayback, controller.commands.receive())
        assertEquals(PracticePlaybackIntent.Paused, controller.state.value.session!!.playbackIntent)
    }

    @Test
    fun recoveryIsPausedAndFinishClearsDurableState() = runTest {
        val repository = FakeSessionRepository(session(playbackIntent = PracticePlaybackIntent.Running))
        val controller = PracticeSessionController(repository, nextId = { "unused" }, nowMillis = { 100L })

        controller.recover()

        assertTrue(controller.state.value.isRecovered)
        assertEquals(PracticePlaybackIntent.Paused, controller.state.value.session!!.playbackIntent)
        controller.finish(PracticeSessionFinishReason.Completed)
        assertNull(controller.state.value.session)
        assertNull(repository.value.value)
        assertEquals(PracticeSessionCommand.PausePlayback, controller.commands.receive())
        assertEquals(
            PracticeSessionCommand.SessionFinished("set-1", PracticeSessionFinishReason.Completed),
            controller.commands.receive(),
        )
    }

    @Test
    fun persistenceFailureSurfacesWithoutDiscardingInMemoryProgress() = runTest {
        val repository = FakeSessionRepository(session()).apply { failSaves = true }
        val controller = PracticeSessionController(repository, nextId = { "unused" }, nowMillis = { 100L })
        controller.recover()

        controller.markCurrentStepEdited()

        assertTrue(controller.state.value.session!!.currentStepEdited)
        assertTrue(controller.state.value.persistenceWarning)
    }

    @Test
    fun persistenceRetrySavesCurrentProgressAndClearsWarning() = runTest {
        val repository = FakeSessionRepository(session()).apply { failSaves = true }
        val controller = PracticeSessionController(repository, nextId = { "unused" }, nowMillis = { 100L })
        controller.recover()
        controller.markCurrentStepEdited()

        repository.failSaves = false

        assertTrue(controller.retryPersistence())
        assertTrue(repository.value.value!!.currentStepEdited)
        assertFalse(controller.state.value.persistenceWarning)
    }

    @Test
    fun failedFinishRetryClearsTheSessionAndCompletesThePendingReplacement() = runTest {
        val repository = FakeSessionRepository(session()).apply { failClears = true }
        val controller = PracticeSessionController(repository, nextId = { "unused" }, nowMillis = { 100L })
        controller.recover()

        controller.finish(PracticeSessionFinishReason.Replaced)

        assertEquals(PracticeSessionCommand.PausePlayback, controller.commands.receive())
        assertTrue(controller.state.value.persistenceWarning)
        assertNotNull(controller.state.value.session)
        repository.failClears = false

        assertTrue(controller.retryPersistence())
        assertNull(controller.state.value.session)
        assertFalse(controller.state.value.persistenceWarning)
        assertEquals(
            PracticeSessionCommand.SessionFinished("set-1", PracticeSessionFinishReason.Replaced),
            controller.commands.receive(),
        )
    }

    @Test
    fun failedDiscardRetryClearsRecoveryWithoutEmittingSessionFinished() = runTest {
        val repository = FakeSessionRepository(session()).apply { failClears = true }
        val controller = PracticeSessionController(repository, nextId = { "unused" }, nowMillis = { 100L })
        controller.recover()

        controller.discardRecovery()

        assertTrue(controller.state.value.persistenceWarning)
        assertNotNull(controller.state.value.session)
        repository.failClears = false

        assertTrue(controller.retryPersistence())
        assertNull(controller.state.value.session)
        assertFalse(controller.state.value.persistenceWarning)
        assertTrue(controller.commands.tryReceive().isFailure)
    }

    private suspend fun recoveredController(session: ActivePracticeSession): PracticeSessionController {
        val controller = PracticeSessionController(
            repository = FakeSessionRepository(session),
            nextId = { "session-new" },
            nowMillis = { 100L },
        )
        controller.recover()
        return controller
    }

    private fun session(
        stepCount: Int = 1,
        target: PracticeStepTarget = PracticeStepTarget.None,
        playbackIntent: PracticePlaybackIntent = PracticePlaybackIntent.Paused,
    ) = ActivePracticeSession(
        id = "session-1",
        sourceSetId = "set-1",
        setName = "Daily",
        steps = (0 until stepCount).map { index ->
            ResolvedPracticeStep("step-$index", preset(index), if (index == 0) target else PracticeStepTarget.None)
        },
        currentStepIndex = 0,
        pendingStepIndex = null,
        elapsedMillis = 0L,
        completedBars = 0,
        playbackIntent = playbackIntent,
        targetReached = false,
        currentStepEdited = false,
        startedAtEpochMillis = 10L,
        lastCheckpointAtEpochMillis = 10L,
    )

    private fun practiceSet(stepCount: Int) = PracticeSet(
        id = "set-1",
        name = "Daily",
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
        lastStartedAtEpochMillis = null,
        lastCompletedAtEpochMillis = null,
        sortPosition = 0,
        steps = (0 until stepCount).map { index ->
            PracticeSetStep("step-$index", "preset-$index", PracticeStepTarget.None)
        },
    )

    private fun preset(index: Int) = PracticePreset(
        id = "preset-$index",
        name = "Preset $index",
        createdAtEpochMillis = index.toLong(),
        lastUsedAtEpochMillis = null,
        isFavourite = false,
        sortPosition = index,
        bpm = 90 + index,
        timeSignature = TimeSignature.FOUR_FOUR,
        subdivision = Subdivision.QUARTER,
        beats = listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.MUTE),
        countInEnabled = false,
    )

    private class FakeSessionRepository(initial: ActivePracticeSession? = null) : PracticeSessionRepository {
        val value = MutableStateFlow(initial)
        var failSaves = false
        var failClears = false
        override val session: Flow<ActivePracticeSession?> = value

        override suspend fun save(session: ActivePracticeSession): Boolean {
            if (failSaves) return false
            value.value = session
            return true
        }

        override suspend fun clear(): Boolean {
            if (failClears) return false
            value.value = null
            return true
        }
    }
}
