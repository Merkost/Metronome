package com.merkost.metronome.viewModels

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.practiceSets.DataStorePracticeSessionRepository
import com.merkost.metronome.practiceSets.DataStorePracticeSetRepository
import com.merkost.metronome.practiceSets.PracticeSessionController
import com.merkost.metronome.practiceSets.PracticeSetMutationResult
import com.merkost.metronome.practiceSets.PracticeStepTarget
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import com.merkost.metronome.presets.MigrationResult
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.PracticePresetDraft
import com.merkost.metronome.presets.PracticePresetRepository
import com.merkost.metronome.presets.PresetMutationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeSetsViewModelTest {
    @Test
    fun editorAddsRepeatedPresetsWithIndependentStepIdsAndTargets() = runTest {
        val viewModel = viewModel(stepIds = listOf("step-1", "step-2"))
        advanceUntilIdle()

        viewModel.beginCreate()
        viewModel.addPreset(preset("preset-a"))
        viewModel.addPreset(preset("preset-a"))
        viewModel.setTarget("step-2", PracticeStepTarget.Bars(8))

        val editor = viewModel.uiState.value.editor!!
        assertEquals(listOf("step-1", "step-2"), editor.steps.map { it.id })
        assertEquals(listOf("preset-a", "preset-a"), editor.steps.map { it.presetId })
        assertEquals(PracticeStepTarget.Bars(8), editor.steps.last().target)
        assertTrue(editor.hasUnsavedChanges)
    }

    @Test
    fun saveNormalizesDraftAndReturnsToLibrary() = runTest {
        val viewModel = viewModel(stepIds = listOf("step-1"))
        advanceUntilIdle()
        val event = async { viewModel.events.first() }
        viewModel.beginCreate()
        viewModel.setName("  Daily  ")
        viewModel.addPreset(preset("preset-a"))

        viewModel.save()
        advanceUntilIdle()

        assertIs<PracticeSetUiEvent.Saved>(event.await())
        assertEquals("Daily", viewModel.uiState.value.sets.single().name)
        assertNull(viewModel.uiState.value.editor)
    }

    @Test
    fun activeSourceSetCannotBeEditedOrDeleted() = runTest {
        val fixture = fixture(stepIds = listOf("step-1"))
        fixture.viewModel.beginCreate()
        fixture.viewModel.setName("Daily")
        fixture.viewModel.addPreset(preset("preset-a"))
        val saved = async { fixture.viewModel.events.first() }
        fixture.viewModel.save()
        advanceUntilIdle()
        assertIs<PracticeSetUiEvent.Saved>(saved.await())
        val set = fixture.viewModel.uiState.value.sets.single()
        fixture.controller.start(set, listOf(preset("preset-a")))
        advanceUntilIdle()
        val event = async { fixture.viewModel.events.first() }

        fixture.viewModel.beginEdit(set.id)
        fixture.viewModel.delete(set.id)
        advanceUntilIdle()

        assertNull(fixture.viewModel.uiState.value.editor)
        assertEquals(PracticeSetUiEvent.ActiveSetLocked, event.await())
        assertEquals(1, fixture.viewModel.uiState.value.sets.size)
    }

    @Test
    fun removeMoveAndCancelKeepPersistentCollectionUntouched() = runTest {
        val viewModel = viewModel(stepIds = listOf("one", "two", "three"))
        advanceUntilIdle()
        viewModel.beginCreate()
        viewModel.addPreset(preset("a"))
        viewModel.addPreset(preset("b"))
        viewModel.addPreset(preset("c"))

        viewModel.moveStep("three", 0)
        viewModel.removeStep("two")

        assertEquals(listOf("three", "one"), viewModel.uiState.value.editor!!.steps.map { it.id })
        viewModel.cancelEditing()
        assertNull(viewModel.uiState.value.editor)
        assertTrue(viewModel.uiState.value.sets.isEmpty())
    }

    private fun TestScope.viewModel(stepIds: List<String>): PracticeSetsViewModel =
        fixture(stepIds).viewModel

    private fun TestScope.fixture(stepIds: List<String>): Fixture {
        val dataStore = InMemoryPreferencesDataStore()
        val setIds = mutableListOf("set-1", "set-2")
        val repository = DataStorePracticeSetRepository(dataStore, { setIds.removeAt(0) }, { 100L })
        val sessionRepository = DataStorePracticeSessionRepository(dataStore)
        val controller = PracticeSessionController(sessionRepository, { "session-1" }, { 100L })
        val presets = FakePresetRepository(listOf(preset("preset-a"), preset("a"), preset("b"), preset("c")))
        val remainingStepIds = stepIds.toMutableList()
        return Fixture(
            viewModel = PracticeSetsViewModel(
                repository = repository,
                presetRepository = presets,
                sessionController = controller,
                nextStepId = { remainingStepIds.removeAt(0) },
                scope = immediateBackgroundScope(),
            ),
            controller = controller,
        )
    }

    private fun TestScope.immediateBackgroundScope(): CoroutineScope = CoroutineScope(
        backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler),
    )

    private fun preset(id: String) = PracticePreset(
        id = id,
        name = id,
        createdAtEpochMillis = 1L,
        lastUsedAtEpochMillis = null,
        isFavourite = false,
        sortPosition = 0,
        bpm = 96,
        timeSignature = TimeSignature.FOUR_FOUR,
        subdivision = Subdivision.QUARTER,
        beats = listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.MUTE),
        countInEnabled = false,
    )

    private data class Fixture(
        val viewModel: PracticeSetsViewModel,
        val controller: PracticeSessionController,
    )

    private class FakePresetRepository(initial: List<PracticePreset>) : PracticePresetRepository {
        private val items = MutableStateFlow(initial)
        override val presets: Flow<List<PracticePreset>> = items
        override suspend fun migrateLegacy(countInEnabled: Boolean) = MigrationResult.AlreadyComplete
        override suspend fun create(draft: PracticePresetDraft) = PresetMutationResult.NotFound
        override suspend fun update(id: String, draft: PracticePresetDraft) = PresetMutationResult.NotFound
        override suspend fun duplicate(id: String, name: String) = PresetMutationResult.NotFound
        override suspend fun delete(id: String) = PresetMutationResult.NotFound
        override suspend fun reorder(orderedIds: List<String>) = PresetMutationResult.NotFound
        override suspend fun toggleFavourite(id: String) = PresetMutationResult.NotFound
        override suspend fun markUsed(id: String) = PresetMutationResult.NotFound
    }
}
