package com.merkost.metronome.practiceSets

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.PracticePresetCodec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PracticeStarterRepositoryTest {
    private val presetsKey = stringPreferencesKey("PRACTICE_PRESETS")
    private val setsKey = stringPreferencesKey("PRACTICE_SETS")

    @Test
    fun previewDoesNotWriteAndSaveCommitsPresetReferencesTogether() = runTest {
        val store = InMemoryPreferencesDataStore()
        val repository = repository(store)
        val draft = repository.draft(emptyList())

        assertTrue(store.data.first().asMap().isEmpty())
        assertEquals(listOf(60, 72, 84), draft.pendingPresets.map { it.bpm })
        assertEquals(Subdivision.EIGHTH, draft.pendingPresets[1].subdivision)
        assertTrue(draft.steps.all { it.target == PracticeStepTarget.Duration(2) })

        val saved = assertIs<PracticeStarterResult.Saved>(repository.save(PracticeSetDraft(draft.name, draft.steps), draft.pendingPresets))
        val snapshot = store.data.first()
        val presets = PracticePresetCodec.decode(snapshot[presetsKey])
        val sets = PracticeSetCodec.decode(snapshot[setsKey])
        assertEquals(3, presets.size)
        assertEquals(saved.practiceSet, sets.single())
        assertEquals(presets.map { it.id }, sets.single().steps.map { it.presetId })
    }

    @Test
    fun presetLimitFailureLeavesBothCollectionsUntouched() = runTest {
        val existing = List(PracticePreset.MAX_PRESETS) { preset("existing-$it", 100) }
        val encoded = PracticePresetCodec.encode(existing)
        val store = InMemoryPreferencesDataStore(mutablePreferencesOf(presetsKey to encoded))
        val repository = repository(store)
        val draft = repository.draft(existing)

        assertEquals(PracticeStarterResult.PresetLimitReached, repository.save(PracticeSetDraft(draft.name, draft.steps), draft.pendingPresets))
        assertEquals(encoded, store.data.first()[presetsKey])
        assertTrue(PracticeSetCodec.decode(store.data.first()[setsKey]).isEmpty())
    }

    @Test
    fun setLimitFailureDoesNotAddStarterPresets() = runTest {
        val sets = List(PracticeSet.MAX_SETS) { index ->
            PracticeSet("set-$index", "Set $index", 1L, 1L, null, null, index, listOf(PracticeSetStep("step-$index", "preset", PracticeStepTarget.None)))
        }
        val encoded = PracticeSetCodec.encode(sets)
        val store = InMemoryPreferencesDataStore(mutablePreferencesOf(setsKey to encoded))
        val repository = repository(store)
        val draft = repository.draft(emptyList())

        assertEquals(PracticeStarterResult.SetLimitReached, repository.save(PracticeSetDraft(draft.name, draft.steps), draft.pendingPresets))
        assertEquals(encoded, store.data.first()[setsKey])
        assertTrue(PracticePresetCodec.decode(store.data.first()[presetsKey]).isEmpty())
    }

    @Test
    fun storageFailureAddsNeitherPresetsNorSet() = runTest {
        val store = InMemoryPreferencesDataStore()
        val repository = repository(store)
        val draft = repository.draft(emptyList())
        store.failUpdates = true

        assertEquals(PracticeStarterResult.StorageFailure, repository.save(PracticeSetDraft(draft.name, draft.steps), draft.pendingPresets))
        store.failUpdates = false
        assertTrue(store.data.first().asMap().isEmpty())
    }

    @Test
    fun editedDraftReusesUserSetupAndDoesNotSaveRemovedStarterSteps() = runTest {
        val existing = preset("my-easy-tempo", 60)
        val store = InMemoryPreferencesDataStore(mutablePreferencesOf(presetsKey to PracticePresetCodec.encode(listOf(existing))))
        val repository = repository(store)
        val draft = repository.draft(listOf(existing))
        val edited = PracticeSetDraft("My warm-up", draft.steps.take(2).map { it.copy(target = PracticeStepTarget.Duration(3)) })

        assertEquals(2, draft.pendingPresets.size)
        val result = assertIs<PracticeStarterResult.Saved>(repository.save(edited, draft.pendingPresets))
        val presets = PracticePresetCodec.decode(store.data.first()[presetsKey])
        assertEquals(existing, presets.first())
        assertEquals(listOf(60, 72), presets.map { it.bpm })
        assertEquals("My warm-up", result.practiceSet.name)
        assertTrue(result.practiceSet.steps.all { it.target == PracticeStepTarget.Duration(3) })
    }

    private fun repository(store: InMemoryPreferencesDataStore): PracticeStarterRepository {
        var id = 0
        return PracticeStarterRepository(store, { "starter-${id++}" }, { 100L })
    }

    private fun preset(id: String, bpm: Int) = PracticePreset(
        id = id,
        name = "My setup $id",
        createdAtEpochMillis = 1L,
        lastUsedAtEpochMillis = null,
        isFavourite = true,
        sortPosition = 0,
        bpm = bpm,
        timeSignature = TimeSignature.FOUR_FOUR,
        subdivision = Subdivision.QUARTER,
        beats = TimeSignature.FOUR_FOUR.defaultBeats,
        countInEnabled = false,
    )
}
