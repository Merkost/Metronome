package com.merkost.metronome.practiceSets

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import com.merkost.metronome.presets.PracticePreset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PracticeSessionPersistenceTest {
    @Test
    fun roundTripsResolvedPresetSnapshotsAndRestoresPaused() {
        val running = session(
            playbackIntent = PracticePlaybackIntent.Running,
            pendingStepIndex = 1,
        )

        val decoded = PracticeSessionCodec.decode(PracticeSessionCodec.encode(running))

        assertEquals(
            running.copy(
                playbackIntent = PracticePlaybackIntent.Paused,
                pendingStepIndex = null,
            ),
            decoded,
        )
    }

    @Test
    fun preservesEveryTargetAndEscapedName() {
        val original = session(
            setName = "Daily % set\nA\tB",
            steps = listOf(
                resolvedStep(0, PracticeStepTarget.None),
                resolvedStep(1, PracticeStepTarget.Duration(12)),
                resolvedStep(2, PracticeStepTarget.Bars(24)),
            ),
        )

        assertEquals(original, PracticeSessionCodec.decode(PracticeSessionCodec.encode(original)))
    }

    @Test
    fun rejectsInvalidIndexesAndPresetSnapshots() {
        val encoded = PracticeSessionCodec.encode(session())

        assertNull(PracticeSessionCodec.decode(encoded.replace("\t0\t", "\t9\t")))
        assertNull(PracticeSessionCodec.decode(encoded.replace("v1%09preset-0", "broken")))
        assertNull(PracticeSessionCodec.decode(null))
    }

    @Test
    fun ignoresUnknownTrailingFields() {
        val original = session()

        assertEquals(
            original,
            PracticeSessionCodec.decode(PracticeSessionCodec.encode(original) + "\tignored"),
        )
    }

    @Test
    fun repositorySavesObservesAndClearsOneSession() = runTest {
        val repository = DataStorePracticeSessionRepository(InMemoryPreferencesDataStore())

        assertTrue(repository.save(session()))
        assertNotNull(repository.session.first())
        assertTrue(repository.clear())
        assertNull(repository.session.first())
    }

    @Test
    fun repositoryReportsFailureAndPreservesPreviousSession() = runTest {
        val store = InMemoryPreferencesDataStore()
        val repository = DataStorePracticeSessionRepository(store)
        repository.save(session(setName = "Original"))

        store.failUpdates = true
        assertEquals(false, repository.save(session(setName = "Replacement")))
        store.failUpdates = false
        assertEquals("Original", repository.session.first()?.setName)
    }

    private fun session(
        setName: String = "Daily",
        steps: List<ResolvedPracticeStep> = listOf(
            resolvedStep(0, PracticeStepTarget.Duration(5)),
            resolvedStep(1, PracticeStepTarget.Bars(8)),
        ),
        playbackIntent: PracticePlaybackIntent = PracticePlaybackIntent.Paused,
        pendingStepIndex: Int? = null,
    ) = ActivePracticeSession(
        id = "session-1",
        sourceSetId = "set-1",
        setName = setName,
        steps = steps,
        currentStepIndex = 0,
        pendingStepIndex = pendingStepIndex,
        elapsedMillis = 42_000L,
        completedBars = 3,
        playbackIntent = playbackIntent,
        targetReached = false,
        currentStepEdited = true,
        startedAtEpochMillis = 10L,
        lastCheckpointAtEpochMillis = 20L,
    )

    private fun resolvedStep(index: Int, target: PracticeStepTarget) = ResolvedPracticeStep(
        stepId = "step-$index",
        preset = PracticePreset(
            id = "preset-$index",
            name = "Preset $index",
            createdAtEpochMillis = index.toLong(),
            lastUsedAtEpochMillis = null,
            isFavourite = false,
            sortPosition = index,
            bpm = 96 + index,
            timeSignature = TimeSignature.FOUR_FOUR,
            subdivision = Subdivision.QUARTER,
            beats = listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.MUTE),
            countInEnabled = index % 2 == 0,
        ),
        target = target,
    )
}
