package com.merkost.metronome.viewModels

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.presets.MigrationResult
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.PracticePresetDraft
import com.merkost.metronome.presets.PracticePresetRepository
import com.merkost.metronome.presets.PresetMutationResult
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.practiceSets.PracticeSetDraft
import com.merkost.metronome.practiceSets.PracticeSetMutationResult
import com.merkost.metronome.practiceSets.PracticeSetRepository
import com.merkost.metronome.practiceSets.PracticeSetStep
import com.merkost.metronome.practiceSets.PracticeStepTarget
import kotlinx.coroutines.async
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PracticePresetsViewModelTest {
    @Test
    fun migratesLegacyAndDerivesBoundedFavouriteAndRecentGroups() = runTest {
        val repository = FakePracticePresetRepository()
        repository.items.value = (0..5).map { index ->
            preset(
                id = "preset-$index",
                favourite = true,
                lastUsed = index.toLong(),
                position = index,
            )
        }

        val viewModel = PracticePresetsViewModel(
            repository = repository,
            countInEnabled = flowOf(true),
            scope = immediateBackgroundScope(),
        )
        advanceUntilIdle()

        assertEquals(1, repository.migrationCalls)
        assertTrue(repository.lastMigrationCountIn)
        assertEquals(4, viewModel.uiState.value.favourites.size)
        assertEquals(listOf("preset-5", "preset-4", "preset-3", "preset-2"), viewModel.uiState.value.recents.map { it.id })
    }

    @Test
    fun createTrimsNameAndEmitsSavedEvent() = runTest {
        val repository = FakePracticePresetRepository()
        val viewModel = viewModel(repository, immediateBackgroundScope())
        val event = async { viewModel.events.first() }

        viewModel.create(draft("  Warmup  "))
        advanceUntilIdle()

        assertEquals("Warmup", repository.created.single().name)
        assertEquals(PresetUiEvent.Saved(preset("created").copy(name = "Warmup")), event.await())
    }

    @Test
    fun moveUsesStableIdsAndDeleteFailureDoesNotClearCollection() = runTest {
        val repository = FakePracticePresetRepository()
        repository.items.value = listOf(preset("one", position = 0), preset("two", position = 1), preset("three", position = 2))
        val viewModel = viewModel(repository, immediateBackgroundScope())
        advanceUntilIdle()

        viewModel.move("three", 0)
        advanceUntilIdle()

        assertEquals(listOf("three", "one", "two"), repository.lastReorder)

        repository.deleteResult = PresetMutationResult.StorageFailure("offline")
        val event = async { viewModel.events.first() }
        viewModel.delete("one")
        advanceUntilIdle()

        assertEquals(PresetUiEvent.StorageFailure, event.await())
        assertEquals(3, viewModel.uiState.value.presets.size)
    }

    @Test
    fun migrationFailureIsRetryable() = runTest {
        val repository = FakePracticePresetRepository().apply {
            migrationResult = MigrationResult.Failed("broken")
        }
        val viewModel = viewModel(repository, immediateBackgroundScope())
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.migrationFailed)

        repository.migrationResult = MigrationResult.Migrated(0)
        viewModel.retryMigration()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.migrationFailed)
        assertEquals(2, repository.migrationCalls)
    }

    @Test
    fun presentsFavouritesBeforeManuallyOrderedPresets() = runTest {
        val repository = FakePracticePresetRepository()
        repository.items.value = listOf(
            preset("first", position = 0),
            preset("favourite", favourite = true, position = 1),
            preset("last", position = 2),
        )
        val viewModel = viewModel(repository, immediateBackgroundScope())
        advanceUntilIdle()

        assertEquals(listOf("favourite", "first", "last"), viewModel.uiState.value.presets.map { it.id })
    }

    @Test
    fun presetDeletionIsBlockedWhenReferencedBySavedSets() = runTest {
        val presets = FakePracticePresetRepository()
        presets.items.value = listOf(preset("preset-a"))
        val sets = FakePracticeSetRepository(
            references = listOf(practiceSet("Warmup", "preset-a")),
        )
        val viewModel = PracticePresetsViewModel(
            repository = presets,
            countInEnabled = flowOf(false),
            scope = immediateBackgroundScope(),
            practiceSetRepository = sets,
        )
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.delete("preset-a")
        advanceUntilIdle()

        assertEquals(PresetUiEvent.InUse(listOf("Warmup")), event.await())
        assertTrue(presets.deleted.isEmpty())
    }

    private fun viewModel(
        repository: FakePracticePresetRepository,
        scope: kotlinx.coroutines.CoroutineScope,
    ) = PracticePresetsViewModel(
        repository = repository,
        countInEnabled = flowOf(false),
        scope = scope,
    )

    private fun TestScope.immediateBackgroundScope(): CoroutineScope = CoroutineScope(
        backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)
    )

    private fun draft(name: String) = PracticePresetDraft(
        name = name,
        bpm = 96,
        timeSignature = TimeSignature.FOUR_FOUR,
        subdivision = Subdivision.QUARTER,
        beats = TimeSignature.FOUR_FOUR.defaultBeats,
        countInEnabled = false,
    )

    private fun preset(
        id: String,
        favourite: Boolean = false,
        lastUsed: Long? = null,
        position: Int = 0,
    ) = PracticePreset(
        id = id,
        name = id,
        createdAtEpochMillis = position.toLong(),
        lastUsedAtEpochMillis = lastUsed,
        isFavourite = favourite,
        sortPosition = position,
        bpm = 96,
        timeSignature = TimeSignature.FOUR_FOUR,
        subdivision = Subdivision.QUARTER,
        beats = TimeSignature.FOUR_FOUR.defaultBeats,
        countInEnabled = false,
    )

    private inner class FakePracticePresetRepository : PracticePresetRepository {
        val items = MutableStateFlow<List<PracticePreset>>(emptyList())
        override val presets: Flow<List<PracticePreset>> = items
        var migrationCalls = 0
        var lastMigrationCountIn = false
        var migrationResult: MigrationResult = MigrationResult.Migrated(0)
        val created = mutableListOf<PracticePresetDraft>()
        var lastReorder = emptyList<String>()
        var deleteResult: PresetMutationResult = PresetMutationResult.Success()
        val deleted = mutableListOf<String>()

        override suspend fun migrateLegacy(countInEnabled: Boolean): MigrationResult {
            migrationCalls += 1
            lastMigrationCountIn = countInEnabled
            return migrationResult
        }

        override suspend fun create(draft: PracticePresetDraft): PresetMutationResult {
            created += draft
            val preset = preset("created").copy(name = draft.name)
            items.value = items.value + preset
            return PresetMutationResult.Success(preset)
        }

        override suspend fun update(id: String, draft: PracticePresetDraft): PresetMutationResult =
            PresetMutationResult.NotFound

        override suspend fun duplicate(id: String, name: String): PresetMutationResult =
            PresetMutationResult.NotFound

        override suspend fun delete(id: String): PresetMutationResult {
            deleted += id
            return deleteResult
        }

        override suspend fun reorder(orderedIds: List<String>): PresetMutationResult {
            lastReorder = orderedIds
            return PresetMutationResult.Success()
        }

        override suspend fun toggleFavourite(id: String): PresetMutationResult =
            PresetMutationResult.NotFound

        override suspend fun markUsed(id: String): PresetMutationResult =
            PresetMutationResult.NotFound
    }

    private class FakePracticeSetRepository(
        private val references: List<PracticeSet>,
    ) : PracticeSetRepository {
        override val sets: Flow<List<PracticeSet>> = flowOf(references)

        override suspend fun create(draft: PracticeSetDraft): PracticeSetMutationResult =
            PracticeSetMutationResult.NotFound

        override suspend fun update(
            id: String,
            expectedUpdatedAtEpochMillis: Long,
            draft: PracticeSetDraft,
        ): PracticeSetMutationResult = PracticeSetMutationResult.NotFound

        override suspend fun delete(id: String): PracticeSetMutationResult = PracticeSetMutationResult.NotFound
        override suspend fun reorder(orderedIds: List<String>): PracticeSetMutationResult =
            PracticeSetMutationResult.NotFound

        override suspend fun markStarted(id: String): PracticeSetMutationResult =
            PracticeSetMutationResult.NotFound

        override suspend fun markCompleted(id: String): PracticeSetMutationResult =
            PracticeSetMutationResult.NotFound

        override suspend fun setsReferencingPreset(presetId: String): List<PracticeSet> =
            references.filter { practiceSet -> practiceSet.steps.any { it.presetId == presetId } }
    }

    private fun practiceSet(name: String, presetId: String) = PracticeSet(
        id = "set-$name",
        name = name,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
        lastStartedAtEpochMillis = null,
        lastCompletedAtEpochMillis = null,
        sortPosition = 0,
        steps = listOf(PracticeSetStep("step-1", presetId, PracticeStepTarget.None)),
    )
}
