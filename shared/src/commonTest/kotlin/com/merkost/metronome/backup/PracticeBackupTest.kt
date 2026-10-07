package com.merkost.metronome.backup

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.practiceSets.PracticeSetCodec
import com.merkost.metronome.practiceSets.PracticeSetStep
import com.merkost.metronome.practiceSets.PracticeStepTarget
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.PracticePresetCodec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PracticeBackupCodecTest {
    @Test
    fun roundTripsUnicodeDelimitersReferencesAndEveryField() {
        val id = "preset-🎸:%2C,\t\n"
        val preset = preset(id, "Échauffement 🎵 %09\tA\nB: C, D")
        val set = practiceSet("set-🌙:%09,\n", id, "Routine 日常 %2C\tA\nB: C, D").copy(
            steps = listOf(
                PracticeSetStep("step-1:%09,\t\n🎶", id, PracticeStepTarget.Duration(12)),
                PracticeSetStep("step-2:%2C", id, PracticeStepTarget.Bars(32)),
                PracticeSetStep("step-3", id, PracticeStepTarget.None),
            ),
        )
        val backup = PracticeBackup(listOf(preset), listOf(set))

        assertEquals(backup, PracticeBackupCodec.decode(PracticeBackupCodec.encode(backup)))
    }

    @Test
    fun rejectsWrongVersionMalformedLengthsTruncationAndTrailingContents() {
        val valid = PracticeBackupCodec.encode(PracticeBackup(listOf(preset("p")), emptyList()))
        val invalid = listOf(
            "",
            valid.replaceFirst("Metronome backup v1", "Metronome backup v2"),
            "Metronome backup v1\n-1\n0\n",
            "Metronome backup v1\ninvalid\n0\n",
            "Metronome backup v1\n2147483648\n0\n",
            "Metronome backup v1\n10000\n0\n",
            "Metronome backup v1\n0\n",
            valid.dropLast(1),
            valid + "unexpected",
        )
        invalid.forEach { raw -> assertFailsWith<IllegalArgumentException> { PracticeBackupCodec.decode(raw) } }
    }

    @Test
    fun rejectsOversizedInputBeforeReadingSections() {
        val raw = "Metronome backup v1\n" + "x".repeat(MaximumBackupCharacters)

        assertFailsWith<IllegalArgumentException> { PracticeBackupCodec.decode(raw) }
    }

    @Test
    fun rejectsInvalidRecordsInsteadOfPartiallyRestoringGoodRecords() {
        val valid = PracticePresetCodec.encode(listOf(preset("p")))
        val invalid = listOf(
            valid.replace("\t96\t", "\t999\t"),
            valid.replace("FOUR_FOUR", "UNKNOWN_METER"),
            valid.replace("HIGH,LOW,MUTE,LOW", "HIGH"),
            valid.replaceFirst("v1", "v9"),
            "garbage",
        )
        invalid.forEach { bad ->
            assertFailsWith<IllegalArgumentException> { PracticeBackupCodec.decode(frame("$valid\n$bad", "")) }
        }
        val set = practiceSet("s", "p").copy(steps = listOf(PracticeSetStep("step", "p", PracticeStepTarget.Duration(0))))
        assertFailsWith<IllegalArgumentException> {
            PracticeBackupCodec.decode(PracticeBackupCodec.encode(PracticeBackup(listOf(preset("p")), listOf(set))))
        }
    }

    @Test
    fun rejectsDuplicatePresetSetAndStepIdentifiers() {
        val p = preset("p")
        val s = practiceSet("s", "p")
        val backups = listOf(
            PracticeBackup(listOf(p, p.copy(name = "Other")), emptyList()),
            PracticeBackup(listOf(p), listOf(s, s.copy(name = "Other"))),
            PracticeBackup(listOf(p), listOf(s.copy(steps = s.steps + s.steps))),
        )
        backups.forEach { backup ->
            assertFailsWith<IllegalArgumentException> { PracticeBackupCodec.decode(PracticeBackupCodec.encode(backup)) }
        }
    }

    @Test
    fun rejectsSetThatReferencesAPresetMissingFromBackup() {
        val backup = PracticeBackup(listOf(preset("p")), listOf(practiceSet("s", "missing")))

        assertFailsWith<IllegalArgumentException> { PracticeBackupCodec.decode(PracticeBackupCodec.encode(backup)) }
    }

    @Test
    fun rejectsCollectionsAboveSupportedCapacity() {
        val tooManyPresets = PracticeBackup((0..PracticePreset.MAX_PRESETS).map { preset("p-$it") }, emptyList())
        val tooManySets = PracticeBackup(listOf(preset("p")), (0..PracticeSet.MAX_SETS).map { practiceSet("s-$it", "p") })
        listOf(tooManyPresets, tooManySets).forEach { backup ->
            assertFailsWith<IllegalArgumentException> { PracticeBackupCodec.decode(PracticeBackupCodec.encode(backup)) }
        }
    }

    private fun frame(presets: String, sets: String) = "Metronome backup v1\n${presets.length}\n$presets${sets.length}\n$sets"
}

class PracticeBackupRepositoryTest {
    private val presetsKey = stringPreferencesKey("PRACTICE_PRESETS")
    private val setsKey = stringPreferencesKey("PRACTICE_SETS")
    private val migrationKey = booleanPreferencesKey("PRACTICE_PRESETS_MIGRATED")
    private val backgroundKey = booleanPreferencesKey("BACKGROUND_PLAY")

    @Test
    fun exportCapturesAllStoredSetupsWithoutChangingPreferences() = runTest {
        val backup = PracticeBackup(listOf(preset("p")), listOf(practiceSet("s", "p")))
        val store = store(backup)
        val before = store.data.first()
        val repository = PracticeBackupRepository(store)

        assertEquals(backup, PracticeBackupCodec.decode(repository.export()))
        assertEquals(before, store.data.first())
    }

    @Test
    fun collidingIdsAreRemappedAndEverySetReferenceFollowsItsImportedPreset() = runTest {
        val current = PracticeBackup(
            listOf(preset("p", "Existing").copy(sortPosition = 7), preset("p-import-1", "Also existing")),
            listOf(practiceSet("s", "p", "Existing set")),
        )
        val incoming = PracticeBackup(
            listOf(preset("p", "Imported"), preset("second", "Imported second")),
            listOf(practiceSet("s", "p", "Imported set").copy(steps = listOf(
                PracticeSetStep("step-1", "p", PracticeStepTarget.Bars(4)),
                PracticeSetStep("step-2", "second", PracticeStepTarget.Duration(3)),
            ))),
        )
        val store = store(current)
        val repository = PracticeBackupRepository(store)

        assertEquals(BackupImportResult(2, 1), repository.import(PracticeBackupCodec.encode(incoming)))
        val restored = PracticeBackupCodec.decode(repository.export())

        assertEquals(current.presets, restored.presets.take(2))
        assertEquals(current.sets, restored.sets.take(1))
        assertEquals(listOf("p-import-2", "second"), restored.presets.drop(2).map { it.id })
        assertEquals(listOf(2, 3), restored.presets.drop(2).map { it.sortPosition })
        assertEquals("s-import-1", restored.sets.last().id)
        assertEquals(1, restored.sets.last().sortPosition)
        assertEquals(listOf("p-import-2", "second"), restored.sets.last().steps.map { it.presetId })
        assertFalse(store.data.first()[migrationKey] ?: true)
        assertTrue(store.data.first()[backgroundKey] == true)
    }

    @Test
    fun presetCapacityFailurePreservesBothCollectionsAndMigrationState() = runTest {
        val current = PracticeBackup((0 until PracticePreset.MAX_PRESETS).map { preset("p-$it") }, listOf(practiceSet("s", "p-0")))
        val store = store(current)
        val before = store.data.first()
        val incoming = PracticeBackup(listOf(preset("new")), listOf(practiceSet("new-set", "new")))

        assertFailsWith<IllegalArgumentException> { PracticeBackupRepository(store).import(PracticeBackupCodec.encode(incoming)) }

        assertEquals(before, store.data.first())
    }

    @Test
    fun setCapacityFailureDoesNotAddOtherwiseValidPresets() = runTest {
        val current = PracticeBackup(listOf(preset("p")), (0 until PracticeSet.MAX_SETS).map { practiceSet("s-$it", "p") })
        val store = store(current)
        val before = store.data.first()
        val incoming = PracticeBackup(listOf(preset("new")), listOf(practiceSet("new-set", "new")))

        assertFailsWith<IllegalArgumentException> { PracticeBackupRepository(store).import(PracticeBackupCodec.encode(incoming)) }

        assertEquals(before, store.data.first())
    }

    @Test
    fun storageFailurePreservesBothCollectionsAndAllPreferences() = runTest {
        val current = PracticeBackup(listOf(preset("p")), listOf(practiceSet("s", "p")))
        val store = store(current)
        val before = store.data.first()
        val incoming = PracticeBackup(listOf(preset("new")), listOf(practiceSet("new-set", "new")))
        store.failUpdates = true

        assertFailsWith<IllegalStateException> { PracticeBackupRepository(store).import(PracticeBackupCodec.encode(incoming)) }

        assertEquals(before, store.data.first())
    }

    @Test
    fun invalidBackupFailsValidationBeforeAttemptingAnyStorageWrite() = runTest {
        val current = PracticeBackup(listOf(preset("p")), listOf(practiceSet("s", "p")))
        val store = store(current)
        val before = store.data.first()
        store.failUpdates = true
        val invalid = PracticeBackup(listOf(preset("new")), listOf(practiceSet("new-set", "missing")))

        assertFailsWith<IllegalArgumentException> { PracticeBackupRepository(store).import(PracticeBackupCodec.encode(invalid)) }

        assertEquals(before, store.data.first())
    }

    @Test
    fun emptyImportIsANoOpAndCannotSuppressPendingLegacyMigration() = runTest {
        val current = PracticeBackup(listOf(preset("p")), listOf(practiceSet("s", "p")))
        val store = store(current)
        val before = store.data.first()
        store.failUpdates = true

        assertEquals(BackupImportResult(0, 0), PracticeBackupRepository(store).import(PracticeBackupCodec.encode(PracticeBackup(emptyList(), emptyList()))))

        assertEquals(before, store.data.first())
        assertFalse(store.data.first()[migrationKey] ?: true)
    }

    private fun store(backup: PracticeBackup) = InMemoryPreferencesDataStore(
        mutablePreferencesOf(
            presetsKey to PracticePresetCodec.encode(backup.presets),
            setsKey to PracticeSetCodec.encode(backup.sets),
            migrationKey to false,
            backgroundKey to true,
        ),
    )
}

private fun preset(id: String, name: String = "Warmup") = PracticePreset(
    id = id,
    name = name,
    createdAtEpochMillis = 100L,
    lastUsedAtEpochMillis = 200L,
    isFavourite = true,
    sortPosition = 0,
    bpm = 96,
    timeSignature = TimeSignature.FOUR_FOUR,
    subdivision = Subdivision.TRIPLET,
    beats = listOf(Beat.HIGH, Beat.LOW, Beat.MUTE, Beat.LOW),
    countInEnabled = true,
)

private fun practiceSet(id: String, presetId: String, name: String = "Daily practice") = PracticeSet(
    id = id,
    name = name,
    createdAtEpochMillis = 300L,
    updatedAtEpochMillis = 400L,
    lastStartedAtEpochMillis = 500L,
    lastCompletedAtEpochMillis = 600L,
    sortPosition = 0,
    steps = listOf(PracticeSetStep("step-$id", presetId, PracticeStepTarget.Duration(2))),
)
