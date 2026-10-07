package com.merkost.metronome.backup

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.practiceSets.PracticeSetCodec
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.PracticePresetCodec
import kotlinx.coroutines.flow.first

const val MaximumBackupCharacters = 1_000_000

data class PracticeBackup(val presets: List<PracticePreset>, val sets: List<PracticeSet>)

data class BackupImportResult(val presetsAdded: Int, val setsAdded: Int)

object PracticeBackupCodec {
    private const val Header = "Metronome backup v1\n"

    fun encode(backup: PracticeBackup): String {
        val presets = PracticePresetCodec.encode(backup.presets)
        val sets = PracticeSetCodec.encode(backup.sets)
        return "$Header${presets.length}\n$presets${sets.length}\n$sets"
    }

    fun decode(raw: String): PracticeBackup {
        require(raw.length <= MaximumBackupCharacters && raw.startsWith(Header)) { "Choose a valid Metronome backup." }
        var offset = Header.length
        fun section(): String {
            val end = raw.indexOf('\n', offset)
            require(end >= offset) { "The backup is incomplete." }
            val length = raw.substring(offset, end).toIntOrNull()
            require(length != null && length >= 0 && length <= raw.length - end - 1) { "The backup is incomplete." }
            offset = end + 1
            return raw.substring(offset, offset + length).also { offset += length }
        }
        val presetsRaw = section()
        val setsRaw = section()
        require(offset == raw.length) { "The backup has unexpected contents." }
        val presets = PracticePresetCodec.decode(presetsRaw)
        val sets = PracticeSetCodec.decode(setsRaw)
        require(presets.size == presetsRaw.lineSequence().count { it.isNotEmpty() } && sets.size == setsRaw.lineSequence().count { it.isNotEmpty() }) { "The backup contains an invalid setup." }
        require(presets.size <= PracticePreset.MAX_PRESETS && sets.size <= PracticeSet.MAX_SETS) { "The backup contains too many setups." }
        require(presets.map { it.id }.distinct().size == presets.size && sets.map { it.id }.distinct().size == sets.size) { "The backup contains duplicate setups." }
        val presetIds = presets.mapTo(mutableSetOf()) { it.id }
        require(sets.all { set -> set.steps.all { it.presetId in presetIds } }) { "The backup is missing a preset used by a set." }
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
            require(currentPresets.size + incoming.presets.size <= PracticePreset.MAX_PRESETS) { "There isn't enough room for these presets. Your current setups are kept." }
            require(currentSets.size + incoming.sets.size <= PracticeSet.MAX_SETS) { "There isn't enough room for these sets. Your current setups are kept." }
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
