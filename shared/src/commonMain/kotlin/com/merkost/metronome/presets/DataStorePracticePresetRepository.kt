package com.merkost.metronome.presets

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.merkost.metronome.model.SavedTempo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStorePracticePresetRepository(
    private val dataStore: DataStore<Preferences>,
    private val nextId: () -> String,
    private val nowMillis: () -> Long,
) : PracticePresetRepository {
    override val presets: Flow<List<PracticePreset>> = dataStore.data.map { preferences ->
        decode(preferences).ordered()
    }

    override suspend fun migrateLegacy(countInEnabled: Boolean): MigrationResult {
        var result: MigrationResult = MigrationResult.Failed("Migration did not run")
        return try {
            dataStore.edit { preferences ->
                if (preferences[PRESETS_MIGRATED] == true) {
                    result = MigrationResult.AlreadyComplete
                    return@edit
                }
                val current = decode(preferences).ordered()
                val legacy = preferences[LEGACY_SAVED_TEMPOS]
                    .orEmpty()
                    .lineSequence()
                    .mapNotNull(SavedTempo::decode)
                    .toList()
                val usedIds = current.mapTo(mutableSetOf()) { it.id }
                val usedNames = current.mapTo(mutableSetOf()) { it.name }
                val capacity = (PracticePreset.MAX_PRESETS - current.size).coerceAtLeast(0)
                val created = legacy.take(capacity).mapIndexed { index, tempo ->
                    val baseName = "${tempo.bpm} BPM · ${tempo.timeSignature.label}"
                    val name = collisionSafeName(baseName, usedNames)
                    PracticePreset(
                        id = uniqueId(usedIds),
                        name = name,
                        createdAtEpochMillis = nowMillis(),
                        lastUsedAtEpochMillis = null,
                        isFavourite = false,
                        sortPosition = current.size + index,
                        bpm = tempo.bpm,
                        timeSignature = tempo.timeSignature,
                        subdivision = tempo.subdivision,
                        beats = tempo.timeSignature.defaultBeats,
                        countInEnabled = countInEnabled,
                    )
                }
                preferences[PRESETS] = PracticePresetCodec.encode((current + created).normalizedOrder())
                preferences[PRESETS_MIGRATED] = true
                result = MigrationResult.Migrated(created.size)
            }
            result
        } catch (throwable: Throwable) {
            MigrationResult.Failed(throwable.message ?: "Migration failed")
        }
    }

    override suspend fun create(draft: PracticePresetDraft): PresetMutationResult {
        val normalized = draft.normalized()
        normalized.validationError?.let { return PresetMutationResult.Invalid(it) }
        return mutate { current ->
            if (current.size >= PracticePreset.MAX_PRESETS) {
                return@mutate current to PresetMutationResult.LimitReached
            }
            val createdAtEpochMillis = nowMillis()
            val preset = normalized.toPreset(
                id = uniqueId(current.mapTo(mutableSetOf()) { it.id }),
                createdAtEpochMillis = createdAtEpochMillis,
                lastUsedAtEpochMillis = createdAtEpochMillis,
                sortPosition = current.size,
            )
            (current + preset) to PresetMutationResult.Success(preset)
        }
    }

    override suspend fun update(id: String, draft: PracticePresetDraft): PresetMutationResult {
        val normalized = draft.normalized()
        normalized.validationError?.let { return PresetMutationResult.Invalid(it) }
        return mutate { current ->
            val existing = current.firstOrNull { it.id == id }
                ?: return@mutate current to PresetMutationResult.NotFound
            val updated = existing.copy(
                name = normalized.name,
                bpm = normalized.bpm,
                timeSignature = normalized.timeSignature,
                subdivision = normalized.subdivision,
                beats = normalized.beats,
                countInEnabled = normalized.countInEnabled,
            )
            current.map { if (it.id == id) updated else it } to PresetMutationResult.Success(updated)
        }
    }

    override suspend fun duplicate(id: String, name: String): PresetMutationResult = mutate { current ->
        val source = current.firstOrNull { it.id == id }
            ?: return@mutate current to PresetMutationResult.NotFound
        if (current.size >= PracticePreset.MAX_PRESETS) {
            return@mutate current to PresetMutationResult.LimitReached
        }
        val draft = source.toDraft(name).normalized()
        draft.validationError?.let {
            return@mutate current to PresetMutationResult.Invalid(it)
        }
        val duplicate = draft.toPreset(
            id = uniqueId(current.mapTo(mutableSetOf()) { it.id }),
            createdAtEpochMillis = nowMillis(),
            sortPosition = source.sortPosition + 1,
        )
        val insertionIndex = current.indexOfFirst { it.id == id } + 1
        current.toMutableList().apply { add(insertionIndex, duplicate) } to PresetMutationResult.Success(duplicate)
    }

    override suspend fun delete(id: String): PresetMutationResult = mutate { current ->
        if (current.none { it.id == id }) {
            current to PresetMutationResult.NotFound
        } else {
            current.filterNot { it.id == id } to PresetMutationResult.Success()
        }
    }

    override suspend fun reorder(orderedIds: List<String>): PresetMutationResult = mutate { current ->
        val requested = orderedIds.distinct().mapNotNull { id -> current.firstOrNull { it.id == id } }
        val requestedIds = requested.mapTo(mutableSetOf()) { it.id }
        (requested + current.filterNot { it.id in requestedIds }) to PresetMutationResult.Success()
    }

    override suspend fun toggleFavourite(id: String): PresetMutationResult = mutate { current ->
        val existing = current.firstOrNull { it.id == id }
            ?: return@mutate current to PresetMutationResult.NotFound
        val updated = existing.copy(isFavourite = !existing.isFavourite)
        current.map { if (it.id == id) updated else it } to PresetMutationResult.Success(updated)
    }

    override suspend fun markUsed(id: String): PresetMutationResult = mutate { current ->
        val existing = current.firstOrNull { it.id == id }
            ?: return@mutate current to PresetMutationResult.NotFound
        val updated = existing.copy(lastUsedAtEpochMillis = nowMillis())
        current.map { if (it.id == id) updated else it } to PresetMutationResult.Success(updated)
    }

    private suspend fun mutate(
        transform: (List<PracticePreset>) -> Pair<List<PracticePreset>, PresetMutationResult>,
    ): PresetMutationResult {
        var result: PresetMutationResult = PresetMutationResult.StorageFailure("Mutation did not run")
        return try {
            dataStore.edit { preferences ->
                val (updated, mutationResult) = transform(decode(preferences).ordered())
                if (mutationResult is PresetMutationResult.Success) {
                    preferences[PRESETS] = PracticePresetCodec.encode(updated.normalizedOrder())
                }
                result = mutationResult
            }
            result
        } catch (throwable: Throwable) {
            PresetMutationResult.StorageFailure(throwable.message ?: "Storage failed")
        }
    }

    private fun decode(preferences: Preferences): List<PracticePreset> =
        PracticePresetCodec.decode(preferences[PRESETS])

    private fun uniqueId(usedIds: MutableSet<String>): String {
        var candidate: String
        do {
            candidate = nextId()
        } while (candidate.isBlank() || candidate in usedIds)
        usedIds += candidate
        return candidate
    }

    private fun collisionSafeName(baseName: String, usedNames: MutableSet<String>): String {
        var candidate = baseName
        var suffix = 2
        while (candidate in usedNames) {
            candidate = "$baseName ($suffix)"
            suffix += 1
        }
        usedNames += candidate
        return candidate
    }

    private fun List<PracticePreset>.ordered(): List<PracticePreset> =
        sortedWith(compareBy<PracticePreset> { it.sortPosition }.thenBy { it.createdAtEpochMillis })

    private fun List<PracticePreset>.normalizedOrder(): List<PracticePreset> =
        mapIndexed { index, preset -> preset.copy(sortPosition = index) }

    private fun PracticePresetDraft.toPreset(
        id: String,
        createdAtEpochMillis: Long,
        lastUsedAtEpochMillis: Long? = null,
        sortPosition: Int,
    ): PracticePreset = PracticePreset(
        id = id,
        name = name,
        createdAtEpochMillis = createdAtEpochMillis,
        lastUsedAtEpochMillis = lastUsedAtEpochMillis,
        isFavourite = false,
        sortPosition = sortPosition,
        bpm = bpm,
        timeSignature = timeSignature,
        subdivision = subdivision,
        beats = beats,
        countInEnabled = countInEnabled,
    )

    private companion object {
        val PRESETS = stringPreferencesKey("PRACTICE_PRESETS")
        val PRESETS_MIGRATED = booleanPreferencesKey("PRACTICE_PRESETS_MIGRATED")
        val LEGACY_SAVED_TEMPOS = stringPreferencesKey("SAVED_TEMPOS")
    }
}
