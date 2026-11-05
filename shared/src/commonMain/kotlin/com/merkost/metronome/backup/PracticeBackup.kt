package com.merkost.metronome.backup

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.merkost.metronome.model.UiText
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.practiceSets.PracticeSetCodec
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.PracticePresetCodec
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlinx.coroutines.flow.first
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.backup_duplicate_setups
import metronome.shared.generated.resources.backup_incomplete
import metronome.shared.generated.resources.backup_invalid
import metronome.shared.generated.resources.backup_invalid_setup
import metronome.shared.generated.resources.backup_missing_preset
import metronome.shared.generated.resources.backup_no_room_presets
import metronome.shared.generated.resources.backup_no_room_sets
import metronome.shared.generated.resources.backup_too_many_setups
import metronome.shared.generated.resources.backup_unexpected_contents
import org.jetbrains.compose.resources.StringResource

const val MaximumBackupCharacters = 1_000_000

data class PracticeBackup(val presets: List<PracticePreset>, val sets: List<PracticeSet>)

data class BackupImportResult(val presetsAdded: Int, val setsAdded: Int)

class BackupException(val text: UiText) : IllegalArgumentException()

fun backupError(resource: StringResource): Nothing = throw BackupException(UiText.of(resource))

@OptIn(ExperimentalContracts::class)
fun requireBackup(condition: Boolean, resource: StringResource) {
    contract { returns() implies condition }
    if (!condition) backupError(resource)
}

fun Throwable.backupText(fallback: StringResource): UiText =
    (this as? BackupException)?.text ?: UiText.of(fallback)

object PracticeBackupCodec {
    private const val Header = "Metronome backup v1\n"

    fun encode(backup: PracticeBackup): String {
        val presets = PracticePresetCodec.encode(backup.presets)
        val sets = PracticeSetCodec.encode(backup.sets)
        return "$Header${presets.length}\n$presets${sets.length}\n$sets"
    }

    fun decode(raw: String): PracticeBackup {
        requireBackup(raw.length <= MaximumBackupCharacters && raw.startsWith(Header), Res.string.backup_invalid)
        var offset = Header.length
        fun section(): String {
            val end = raw.indexOf('\n', offset)
            requireBackup(end >= offset, Res.string.backup_incomplete)
            val length = raw.substring(offset, end).toIntOrNull()
            requireBackup(length != null && length >= 0 && length <= raw.length - end - 1, Res.string.backup_incomplete)
            offset = end + 1
            return raw.substring(offset, offset + length).also { offset += length }
        }
        val presetsRaw = section()
        val setsRaw = section()
        requireBackup(offset == raw.length, Res.string.backup_unexpected_contents)
        val presets = PracticePresetCodec.decode(presetsRaw)
        val sets = PracticeSetCodec.decode(setsRaw)
        requireBackup(presets.size == presetsRaw.lineSequence().count { it.isNotEmpty() } && sets.size == setsRaw.lineSequence().count { it.isNotEmpty() }, Res.string.backup_invalid_setup)
        requireBackup(presets.size <= PracticePreset.MAX_PRESETS && sets.size <= PracticeSet.MAX_SETS, Res.string.backup_too_many_setups)
        requireBackup(presets.map { it.id }.distinct().size == presets.size && sets.map { it.id }.distinct().size == sets.size, Res.string.backup_duplicate_setups)
        val presetIds = presets.mapTo(mutableSetOf()) { it.id }
        requireBackup(sets.all { set -> set.steps.all { it.presetId in presetIds } }, Res.string.backup_missing_preset)
        return PracticeBackup(presets, sets)
    }
}

class PracticeBackupRepository(private val dataStore: DataStore<Preferences>) {
    private val presetsKey = stringPreferencesKey("PRACTICE_PRESETS")
    private val setsKey = stringPreferencesKey("PRACTICE_SETS")

    suspend fun export(): String {
        val preferences = dataStore.data.first()
        val backup = PracticeBackup(PracticePresetCodec.decode(preferences[presetsKey]), PracticeSetCodec.decode(preferences[setsKey]))
        return PracticeBackupCodec.encode(backup)
    }

    suspend fun import(raw: String): BackupImportResult {
        val incoming = PracticeBackupCodec.decode(raw)
        if (incoming.presets.isEmpty() && incoming.sets.isEmpty()) return BackupImportResult(0, 0)
        dataStore.edit { preferences ->
            val currentPresets = PracticePresetCodec.decode(preferences[presetsKey])
            val currentSets = PracticeSetCodec.decode(preferences[setsKey])
            requireBackup(currentPresets.size + incoming.presets.size <= PracticePreset.MAX_PRESETS, Res.string.backup_no_room_presets)
            requireBackup(currentSets.size + incoming.sets.size <= PracticeSet.MAX_SETS, Res.string.backup_no_room_sets)
            val ids = currentPresets.mapTo(mutableSetOf()) { it.id }
            fun unique(source: String, used: MutableSet<String>): String {
                var candidate = source
                var number = 1
                while (!used.add(candidate)) candidate = "$source-import-${number++}"
                return candidate
            }
            val mappings = incoming.presets.associate { it.id to unique(it.id, ids) }
            val addedPresets = incoming.presets.mapIndexed { index, preset ->
                preset.copy(id = mappings.getValue(preset.id), sortPosition = currentPresets.size + index)
            }
            val setIds = currentSets.mapTo(mutableSetOf()) { it.id }
            val addedSets = incoming.sets.mapIndexed { index, set ->
                set.copy(id = unique(set.id, setIds), sortPosition = currentSets.size + index, steps = set.steps.map { it.copy(presetId = mappings.getValue(it.presetId)) })
            }
            preferences[presetsKey] = PracticePresetCodec.encode(currentPresets + addedPresets)
            preferences[setsKey] = PracticeSetCodec.encode(currentSets + addedSets)
        }
        return BackupImportResult(incoming.presets.size, incoming.sets.size)
    }
}
