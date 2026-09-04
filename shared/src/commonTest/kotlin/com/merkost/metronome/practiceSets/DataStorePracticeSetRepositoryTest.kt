package com.merkost.metronome.practiceSets

import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DataStorePracticeSetRepositoryTest {
    @Test
    fun createsUpdatesReordersMarksStartedAndFindsPresetReferences() = runTest {
        var now = 100L
        val repository = repository(ids = listOf("set-1", "set-2"), nowMillis = { now++ })
        val warmup = assertIs<PracticeSetMutationResult.Success>(
            repository.create(draft("Warmup", "preset-a")),
        ).practiceSet!!
        repository.create(draft("Repertoire", "preset-b"))

        assertIs<PracticeSetMutationResult.Success>(
            repository.update(
                id = warmup.id,
                expectedUpdatedAtEpochMillis = warmup.updatedAtEpochMillis,
                draft = draft("Daily warmup", "preset-a"),
            ),
        )
        assertIs<PracticeSetMutationResult.Success>(repository.reorder(listOf("set-2", "set-1")))
        assertIs<PracticeSetMutationResult.Success>(repository.markStarted("set-1"))

        assertEquals(listOf("set-2", "set-1"), repository.sets.first().map { it.id })
        assertEquals(
            listOf("Daily warmup"),
            repository.setsReferencingPreset("preset-a").map { it.name },
        )
        assertEquals(103L, repository.sets.first().single { it.id == "set-1" }.lastStartedAtEpochMillis)
    }

    @Test
    fun marksOnlyTheRequestedSetCompletedAndSelectsItAsMostRecent() = runTest {
        var now = 100L
        val repository = repository(ids = listOf("one", "two"), nowMillis = { now++ })
        repository.create(draft("One", "preset-a"))
        repository.create(draft("Two", "preset-b"))

        assertIs<PracticeSetMutationResult.Success>(repository.markCompleted("one"))

        val sets = repository.sets.first()
        assertEquals(102L, sets.single { it.id == "one" }.lastCompletedAtEpochMillis)
        assertEquals("one", sets.mostRecentlyCompleted()!!.id)
        assertNull(sets.single { it.id == "two" }.lastCompletedAtEpochMillis)
    }

    @Test
    fun failedCompletionWritePreservesThePriorTimestamp() = runTest {
        var now = 100L
        val store = InMemoryPreferencesDataStore()
        val repository = repository(store = store, ids = listOf("one"), nowMillis = { now++ })
        repository.create(draft("One", "preset-a"))
        repository.markCompleted("one")
        val completedAt = repository.sets.first().single().lastCompletedAtEpochMillis

        store.failUpdates = true
        assertIs<PracticeSetMutationResult.StorageFailure>(repository.markCompleted("one"))
        store.failUpdates = false

        assertEquals(completedAt, repository.sets.first().single().lastCompletedAtEpochMillis)
    }

    @Test
    fun rejectsStaleUpdatesWithoutReplacingStoredData() = runTest {
        val repository = repository(ids = listOf("set-1"))
        val created = assertIs<PracticeSetMutationResult.Success>(
            repository.create(draft("Warmup", "preset-a")),
        ).practiceSet!!

        assertEquals(
            PracticeSetMutationResult.Conflict,
            repository.update(created.id, created.updatedAtEpochMillis - 1, draft("Changed", "preset-b")),
        )
        assertEquals("Warmup", repository.sets.first().single().name)
    }

    @Test
    fun enforcesSetAndStepLimitsWithoutReplacingStoredData() = runTest {
        val repository = repository(ids = (0..PracticeSet.MAX_SETS).map { "set-$it" })
        repeat(PracticeSet.MAX_SETS) { index ->
            assertIs<PracticeSetMutationResult.Success>(
                repository.create(draft("Set $index", "preset-$index")),
            )
        }

        assertEquals(
            PracticeSetMutationResult.LimitReached,
            repository.create(draft("Overflow", "preset-x")),
        )
        assertEquals(PracticeSet.MAX_SETS, repository.sets.first().size)
        assertEquals(
            PracticeSetMutationResult.Invalid(PracticeSetValidationError.TOO_MANY_STEPS),
            repository.update(
                id = "set-0",
                expectedUpdatedAtEpochMillis = repository.sets.first().first().updatedAtEpochMillis,
                draft = PracticeSetDraft(
                    name = "Too many",
                    steps = (0..PracticeSet.MAX_STEPS).map {
                        PracticeSetStep("step-$it", "preset-$it", PracticeStepTarget.None)
                    },
                ),
            ),
        )
        assertEquals(1, repository.sets.first().first().steps.size)
    }

    @Test
    fun supportsPartialReorderAndDelete() = runTest {
        val repository = repository(ids = listOf("one", "two", "three"))
        repository.create(draft("One", "preset-a"))
        repository.create(draft("Two", "preset-b"))
        repository.create(draft("Three", "preset-c"))

        repository.reorder(listOf("three"))
        assertEquals(listOf("three", "one", "two"), repository.sets.first().map { it.id })
        assertIs<PracticeSetMutationResult.Success>(repository.delete("one"))
        assertFalse(repository.sets.first().any { it.id == "one" })
        assertEquals(PracticeSetMutationResult.NotFound, repository.delete("missing"))
    }

    @Test
    fun reportsInvalidInputAndStorageFailureWithoutChangingCollection() = runTest {
        val store = InMemoryPreferencesDataStore()
        val repository = repository(store = store, ids = listOf("set-1"))

        assertEquals(
            PracticeSetMutationResult.Invalid(PracticeSetValidationError.EMPTY_NAME),
            repository.create(draft(" ", "preset-a")),
        )
        store.failUpdates = true
        assertIs<PracticeSetMutationResult.StorageFailure>(repository.create(draft("Warmup", "preset-a")))
        store.failUpdates = false
        assertTrue(repository.sets.first().isEmpty())
    }

    private fun repository(
        store: InMemoryPreferencesDataStore = InMemoryPreferencesDataStore(),
        ids: List<String> = (0..100).map { "set-$it" },
        nowMillis: () -> Long = { 1_000L },
    ): DataStorePracticeSetRepository {
        val remainingIds = ids.toMutableList()
        return DataStorePracticeSetRepository(
            dataStore = store,
            nextId = { remainingIds.removeAt(0) },
            nowMillis = nowMillis,
        )
    }

    private fun draft(name: String, presetId: String) = PracticeSetDraft(
        name = name,
        steps = listOf(PracticeSetStep("step-$presetId", presetId, PracticeStepTarget.None)),
    )
}
