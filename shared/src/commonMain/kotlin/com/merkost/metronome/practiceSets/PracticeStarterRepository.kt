package com.merkost.metronome.practiceSets

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.PracticePresetCodec
import kotlinx.coroutines.CancellationException

private val starterPresetsKey = stringPreferencesKey("PRACTICE_PRESETS")
private val starterSetsKey = stringPreferencesKey("PRACTICE_SETS")

data class PracticeStarterDraft(val name: String, val steps: List<PracticeSetStep>, val pendingPresets: List<PracticePreset>)

sealed interface PracticeStarterResult {
    data class Saved(val practiceSet: PracticeSet) : PracticeStarterResult
    data object PresetLimitReached : PracticeStarterResult
    data object SetLimitReached : PracticeStarterResult
    data object MissingPreset : PracticeStarterResult
    data class Invalid(val error: PracticeSetValidationError) : PracticeStarterResult
    data object StorageFailure : PracticeStarterResult
}

class PracticeStarterRepository(
    private val dataStore: DataStore<Preferences>,
    private val nextId: () -> String,
    private val nowMillis: () -> Long,
) {
    fun draft(existingPresets: List<PracticePreset>): PracticeStarterDraft {
        val names = listOf("Easy start", "Build the pulse", "Find your flow")
        val tempos = listOf(60, 72, 84)
        val pending = mutableListOf<PracticePreset>()
        val used = existingPresets.mapTo(mutableSetOf()) { it.id }
        val steps = tempos.mapIndexed { index, bpm ->
            val subdivision = if (index == 1) Subdivision.EIGHTH else Subdivision.QUARTER
            val preset = existingPresets.firstOrNull { it.matches(bpm, subdivision) }
                ?: PracticePreset(
                    id = uniqueId(used),
                    name = names[index],
                    createdAtEpochMillis = 0L,
                    lastUsedAtEpochMillis = null,
                    isFavourite = false,
                    sortPosition = existingPresets.size + pending.size,
                    bpm = bpm,
                    timeSignature = TimeSignature.FOUR_FOUR,
                    subdivision = subdivision,
                    beats = TimeSignature.FOUR_FOUR.defaultBeats,
                    countInEnabled = false,
                ).also { pending += it }
            PracticeSetStep(uniqueId(used), preset.id, PracticeStepTarget.Duration(2))
        }
        return PracticeStarterDraft("A steady warm-up", steps, pending)
    }

    suspend fun save(draft: PracticeSetDraft, pendingPresets: List<PracticePreset>): PracticeStarterResult {
        val normalized = draft.normalized()
        normalized.validationError?.let { return PracticeStarterResult.Invalid(it) }
        var result: PracticeStarterResult = PracticeStarterResult.StorageFailure
        return try {
            dataStore.edit { preferences ->
                val currentPresets = PracticePresetCodec.decode(preferences[starterPresetsKey])
                val currentSets = PracticeSetCodec.decode(preferences[starterSetsKey])
                if (currentSets.size >= PracticeSet.MAX_SETS) {
                    result = PracticeStarterResult.SetLimitReached
                    return@edit
                }
                val used = (currentPresets.map { it.id } + currentSets.map { it.id }).toMutableSet()
                val additions = mutableListOf<PracticePreset>()
                val usedNames = currentPresets.mapTo(mutableSetOf()) { it.name }
                val mappings = mutableMapOf<String, String>()
                val now = nowMillis()
                val needed = normalized.steps.mapTo(mutableSetOf()) { it.presetId }
                pendingPresets.filter { it.id in needed }.forEach { pending ->
                    if (pending.toDraft().validationError != null) {
                        result = PracticeStarterResult.StorageFailure
                        return@edit
                    }
                    val existing = currentPresets.firstOrNull { it.matches(pending.bpm, pending.subdivision) }
                    if (existing != null) {
                        mappings[pending.id] = existing.id
                    } else {
                        val id = if (pending.id.isNotBlank() && used.add(pending.id)) pending.id else uniqueId(used)
                        mappings[pending.id] = id
                        additions += pending.copy(id = id, name = uniqueName(pending.name, usedNames), createdAtEpochMillis = now, sortPosition = currentPresets.size + additions.size)
                    }
                }
                if (currentPresets.size + additions.size > PracticePreset.MAX_PRESETS) {
                    result = PracticeStarterResult.PresetLimitReached
                    return@edit
                }
                val steps = normalized.steps.map { it.copy(presetId = mappings[it.presetId] ?: it.presetId) }
                val availableIds = (currentPresets + additions).mapTo(mutableSetOf()) { it.id }
                if (steps.any { it.presetId !in availableIds }) {
                    result = PracticeStarterResult.MissingPreset
                    return@edit
                }
                val created = PracticeSet(
                    id = uniqueId(used),
                    name = normalized.name,
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                    lastStartedAtEpochMillis = null,
                    lastCompletedAtEpochMillis = null,
                    sortPosition = currentSets.size,
                    steps = steps,
                )
                val encodedPresets = PracticePresetCodec.encode(currentPresets + additions)
                val encodedSets = PracticeSetCodec.encode(currentSets + created)
                preferences[starterPresetsKey] = encodedPresets
                preferences[starterSetsKey] = encodedSets
                result = PracticeStarterResult.Saved(created)
            }
            result
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            PracticeStarterResult.StorageFailure
        }
    }

    private fun uniqueName(base: String, used: MutableSet<String>): String {
        var name = base
        var suffix = 2
        while (!used.add(name)) name = "$base (${suffix++})"
        return name
    }

    private fun uniqueId(used: MutableSet<String>): String {
        repeat(128) {
            val candidate = nextId()
            if (candidate.isNotBlank() && used.add(candidate)) return candidate
        }
        error("Couldn't create a unique starter ID")
    }
}

private fun PracticePreset.matches(bpm: Int, subdivision: Subdivision): Boolean =
    this.bpm == bpm && timeSignature == TimeSignature.FOUR_FOUR && this.subdivision == subdivision &&
        beats == TimeSignature.FOUR_FOUR.defaultBeats && !countInEnabled
