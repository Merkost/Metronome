package com.merkost.metronome.practiceSets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PracticeAgainPolicyTest {

    @Test
    fun activeSessionSuppressesPracticeAgain() {
        assertNull(practiceAgainSet(listOf(completedSet()), activeSourceSetId = "active"))
    }

    @Test
    fun noActiveSessionUsesMostRecentCompletion() {
        assertEquals(
            "recent",
            practiceAgainSet(
                listOf(completedSet("older", 10L), completedSet("recent", 20L)),
                activeSourceSetId = null,
            )!!.id,
        )
    }

    @Test
    fun recentSelectionUsesTheCurrentRenamedSetRegardlessOfSortPosition() {
        val renamed = completedSet("recent", 20L).copy(name = "Current name", sortPosition = 99)

        assertEquals(
            renamed,
            practiceAgainSet(
                listOf(completedSet("older", 10L).copy(sortPosition = 0), renamed),
                activeSourceSetId = null,
            ),
        )
    }

    @Test
    fun completedFinishRecordsItsSourceSet() = runTest {
        val repository = RecordingPracticeSetRepository()

        assertTrue(
            recordPracticeCompletion(
                PracticeSessionCommand.SessionFinished("set-1", PracticeSessionFinishReason.Completed),
                repository,
            ),
        )
        assertEquals(listOf("set-1"), repository.completedIds)
    }

    @Test
    fun replacementFinishDoesNotRecordCompletion() = runTest {
        val repository = RecordingPracticeSetRepository()

        assertTrue(
            recordPracticeCompletion(
                PracticeSessionCommand.SessionFinished("set-1", PracticeSessionFinishReason.Replaced),
                repository,
            ),
        )
        assertTrue(repository.completedIds.isEmpty())
    }

    @Test
    fun completedFinishReportsUnavailablePersistence() = runTest {
        assertFalse(
            recordPracticeCompletion(
                PracticeSessionCommand.SessionFinished("set-1", PracticeSessionFinishReason.Completed),
                repository = null,
            ),
        )
    }

    private fun completedSet(
        id: String = "set-1",
        completedAt: Long = 10L,
    ) = PracticeSet(
        id = id,
        name = "Set $id",
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = completedAt,
        lastStartedAtEpochMillis = null,
        lastCompletedAtEpochMillis = completedAt,
        sortPosition = 0,
        steps = listOf(PracticeSetStep("step-1", "preset-1", PracticeStepTarget.None)),
    )

    private class RecordingPracticeSetRepository : PracticeSetRepository {
        val completedIds = mutableListOf<String>()
        override val sets: Flow<List<PracticeSet>> = MutableStateFlow(emptyList())

        override suspend fun create(draft: PracticeSetDraft) = PracticeSetMutationResult.NotFound
        override suspend fun update(
            id: String,
            expectedUpdatedAtEpochMillis: Long,
            draft: PracticeSetDraft,
        ) = PracticeSetMutationResult.NotFound
        override suspend fun delete(id: String) = PracticeSetMutationResult.NotFound
        override suspend fun reorder(orderedIds: List<String>) = PracticeSetMutationResult.NotFound
        override suspend fun markStarted(id: String) = PracticeSetMutationResult.NotFound
        override suspend fun markCompleted(id: String): PracticeSetMutationResult {
            completedIds += id
            return PracticeSetMutationResult.Success()
        }
        override suspend fun setsReferencingPreset(presetId: String): List<PracticeSet> = emptyList()
    }
}
