package com.merkost.metronome.presets

import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DataStorePracticePresetRepositoryTest {
    @Test
    fun migratesLegacyOnceWithoutDroppingSourceData() = runTest {
        val legacyKey = stringPreferencesKey("SAVED_TEMPOS")
        val store = InMemoryPreferencesDataStore(
            preferencesOf(legacyKey to "120:FOUR_FOUR:QUARTER\n90:THREE_FOUR:TRIPLET")
        )
        val repository = repository(store, ids = listOf("legacy-1", "legacy-2"))

        assertEquals(MigrationResult.Migrated(2), repository.migrateLegacy(countInEnabled = true))
        assertEquals(MigrationResult.AlreadyComplete, repository.migrateLegacy(countInEnabled = false))

        val presets = repository.presets.first()
        assertEquals(listOf("120 BPM · 4/4", "90 BPM · 3/4"), presets.map { it.name })
        assertTrue(presets.all { it.countInEnabled })
        assertEquals("120:FOUR_FOUR:QUARTER\n90:THREE_FOUR:TRIPLET", store.data.first()[legacyKey])
    }

    @Test
    fun migrationCreatesCollisionSafeNamesAndStableDefaultBeats() = runTest {
        val legacyKey = stringPreferencesKey("SAVED_TEMPOS")
        val store = InMemoryPreferencesDataStore(
            preferencesOf(legacyKey to "120:FOUR_FOUR:QUARTER\n120:FOUR_FOUR:EIGHTH")
        )
        val repository = repository(store, ids = listOf("legacy-1", "legacy-2"))

        repository.migrateLegacy(countInEnabled = false)

        val presets = repository.presets.first()
        assertEquals(listOf("120 BPM · 4/4", "120 BPM · 4/4 (2)"), presets.map { it.name })
        assertEquals(TimeSignature.FOUR_FOUR.defaultBeats, presets.first().beats)
    }

    @Test
    fun migrationNeverExceedsCollectionLimit() = runTest {
        val presetsKey = stringPreferencesKey("PRACTICE_PRESETS")
        val legacyKey = stringPreferencesKey("SAVED_TEMPOS")
        val existing = (0 until PracticePreset.MAX_PRESETS - 1).map { index ->
            PracticePreset(
                id = "existing-$index",
                name = "Existing $index",
                createdAtEpochMillis = index.toLong(),
                lastUsedAtEpochMillis = null,
                isFavourite = false,
                sortPosition = index,
                bpm = 96,
                timeSignature = TimeSignature.FOUR_FOUR,
                subdivision = Subdivision.QUARTER,
                beats = TimeSignature.FOUR_FOUR.defaultBeats,
                countInEnabled = false,
            )
        }
        val store = InMemoryPreferencesDataStore(
            preferencesOf(
                presetsKey to PracticePresetCodec.encode(existing),
                legacyKey to "120:FOUR_FOUR:QUARTER\n90:THREE_FOUR:TRIPLET",
            )
        )
        val repository = repository(store, ids = listOf("legacy-1", "legacy-2"))

        assertEquals(MigrationResult.Migrated(1), repository.migrateLegacy(false))
        assertEquals(PracticePreset.MAX_PRESETS, repository.presets.first().size)
    }

    @Test
    fun createNormalizesInputAndEnforcesCollectionLimit() = runTest {
        val ids = (0..PracticePreset.MAX_PRESETS).map { "preset-$it" }
        val repository = repository(InMemoryPreferencesDataStore(), ids)

        repeat(PracticePreset.MAX_PRESETS) { index ->
            assertIs<PresetMutationResult.Success>(repository.create(draft("  Preset $index  ")))
        }

        assertEquals("Preset 0", repository.presets.first().first().name)
        assertEquals(1_000L, repository.presets.first().first().lastUsedAtEpochMillis)
        assertEquals(PresetMutationResult.LimitReached, repository.create(draft("Overflow")))
    }

    @Test
    fun supportsUpdateDuplicateDeleteFavouriteRecencyAndPartialReorder() = runTest {
        var now = 100L
        val repository = repository(
            store = InMemoryPreferencesDataStore(),
            ids = listOf("one", "two", "copy"),
            nowMillis = { now++ },
        )
        repository.create(draft("One"))
        repository.create(draft("Two"))

        assertIs<PresetMutationResult.Success>(repository.update("one", draft("Updated")))
        assertIs<PresetMutationResult.Success>(repository.duplicate("one", "Updated Copy"))
        assertIs<PresetMutationResult.Success>(repository.toggleFavourite("two"))
        assertIs<PresetMutationResult.Success>(repository.markUsed("one"))
        assertIs<PresetMutationResult.Success>(repository.reorder(listOf("copy", "two")))

        val reordered = repository.presets.first()
        assertEquals(listOf("copy", "two", "one"), reordered.map { it.id })
        assertTrue(reordered.single { it.id == "two" }.isFavourite)
        assertEquals(103L, reordered.single { it.id == "one" }.lastUsedAtEpochMillis)
        assertNotEquals(
            reordered.single { it.id == "one" }.createdAtEpochMillis,
            reordered.single { it.id == "copy" }.createdAtEpochMillis,
        )

        assertIs<PresetMutationResult.Success>(repository.delete("two"))
        assertFalse(repository.presets.first().any { it.id == "two" })
        assertEquals(PresetMutationResult.NotFound, repository.delete("missing"))
    }

    @Test
    fun rejectsInvalidDraftAndReportsStorageFailureWithoutChangingCollection() = runTest {
        val store = InMemoryPreferencesDataStore()
        val repository = repository(store, ids = listOf("one"))
        val invalid = draft(" ")

        assertEquals(
            PresetMutationResult.Invalid(PresetValidationError.EMPTY_NAME),
            repository.create(invalid),
        )

        store.failUpdates = true
        assertIs<PresetMutationResult.StorageFailure>(repository.create(draft("One")))
        store.failUpdates = false
        assertTrue(repository.presets.first().isEmpty())
    }

    private fun repository(
        store: InMemoryPreferencesDataStore,
        ids: List<String> = (0..100).map { "id-$it" },
        nowMillis: () -> Long = { 1_000L },
    ): DataStorePracticePresetRepository {
        val remainingIds = ids.toMutableList()
        return DataStorePracticePresetRepository(
            dataStore = store,
            nextId = { remainingIds.removeAt(0) },
            nowMillis = nowMillis,
        )
    }

    private fun draft(name: String) = PracticePresetDraft(
        name = name,
        bpm = 96,
        timeSignature = TimeSignature.FOUR_FOUR,
        subdivision = Subdivision.QUARTER,
        beats = listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.MUTE),
        countInEnabled = false,
    )
}
