package com.merkost.metronome.practiceSets

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStorePracticeSetRepository(
    private val dataStore: DataStore<Preferences>,
    private val nextId: () -> String,
    private val nowMillis: () -> Long,
) : PracticeSetRepository {
    override val sets: Flow<List<PracticeSet>> = dataStore.data.map { preferences ->
        decode(preferences).ordered()
    }

    override suspend fun create(draft: PracticeSetDraft): PracticeSetMutationResult {
        val normalized = draft.normalized()
        normalized.validationError?.let { return PracticeSetMutationResult.Invalid(it) }
        return mutate { current ->
            if (current.size >= PracticeSet.MAX_SETS) {
                return@mutate current to PracticeSetMutationResult.LimitReached
            }
            val now = nowMillis()
            val created = PracticeSet(
                id = uniqueId(current.mapTo(mutableSetOf()) { it.id }),
                name = normalized.name,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                lastStartedAtEpochMillis = null,
                lastCompletedAtEpochMillis = null,
                sortPosition = current.size,
                steps = normalized.steps,
            )
            (current + created) to PracticeSetMutationResult.Success(created)
        }
    }

    override suspend fun update(
        id: String,
        expectedUpdatedAtEpochMillis: Long,
        draft: PracticeSetDraft,
    ): PracticeSetMutationResult {
        val normalized = draft.normalized()
        normalized.validationError?.let { return PracticeSetMutationResult.Invalid(it) }
        return mutate { current ->
            val existing = current.firstOrNull { it.id == id }
                ?: return@mutate current to PracticeSetMutationResult.NotFound
            if (existing.updatedAtEpochMillis != expectedUpdatedAtEpochMillis) {
                return@mutate current to PracticeSetMutationResult.Conflict
            }
            val updated = existing.copy(
                name = normalized.name,
                updatedAtEpochMillis = nowMillis(),
                steps = normalized.steps,
            )
            current.map { if (it.id == id) updated else it } to PracticeSetMutationResult.Success(updated)
        }
    }

    override suspend fun delete(id: String): PracticeSetMutationResult = mutate { current ->
        if (current.none { it.id == id }) {
            current to PracticeSetMutationResult.NotFound
        } else {
            current.filterNot { it.id == id } to PracticeSetMutationResult.Success()
        }
    }

    override suspend fun reorder(orderedIds: List<String>): PracticeSetMutationResult = mutate { current ->
        val requested = orderedIds.distinct().mapNotNull { id -> current.firstOrNull { it.id == id } }
        val requestedIds = requested.mapTo(mutableSetOf()) { it.id }
        (requested + current.filterNot { it.id in requestedIds }) to PracticeSetMutationResult.Success()
    }

    override suspend fun markStarted(id: String): PracticeSetMutationResult = mutate { current ->
        val existing = current.firstOrNull { it.id == id }
            ?: return@mutate current to PracticeSetMutationResult.NotFound
        val now = nowMillis()
        val updated = existing.copy(
            updatedAtEpochMillis = now,
            lastStartedAtEpochMillis = now,
        )
        current.map { if (it.id == id) updated else it } to PracticeSetMutationResult.Success(updated)
    }

    override suspend fun markCompleted(id: String): PracticeSetMutationResult = mutate { current ->
        val existing = current.firstOrNull { it.id == id }
            ?: return@mutate current to PracticeSetMutationResult.NotFound
        val now = nowMillis()
        val updated = existing.copy(
            updatedAtEpochMillis = now,
            lastCompletedAtEpochMillis = now,
        )
        current.map { if (it.id == id) updated else it } to PracticeSetMutationResult.Success(updated)
    }

    override suspend fun setsReferencingPreset(presetId: String): List<PracticeSet> =
        sets.first().filter { practiceSet -> practiceSet.steps.any { it.presetId == presetId } }

    private suspend fun mutate(
        transform: (List<PracticeSet>) -> Pair<List<PracticeSet>, PracticeSetMutationResult>,
    ): PracticeSetMutationResult {
        var result: PracticeSetMutationResult = PracticeSetMutationResult.StorageFailure("Mutation did not run")
        return try {
            dataStore.edit { preferences ->
                val (updated, mutationResult) = transform(decode(preferences).ordered())
                if (mutationResult is PracticeSetMutationResult.Success) {
                    preferences[PRACTICE_SETS] = PracticeSetCodec.encode(updated.normalizedOrder())
                }
                result = mutationResult
            }
            result
        } catch (throwable: Throwable) {
            PracticeSetMutationResult.StorageFailure(throwable.message ?: "Storage failed")
        }
    }

    private fun decode(preferences: Preferences): List<PracticeSet> =
        PracticeSetCodec.decode(preferences[PRACTICE_SETS])

    private fun uniqueId(usedIds: MutableSet<String>): String {
        var candidate: String
        do {
            candidate = nextId()
        } while (candidate.isBlank() || candidate in usedIds)
        usedIds += candidate
        return candidate
    }

    private fun List<PracticeSet>.ordered(): List<PracticeSet> =
        sortedWith(compareBy<PracticeSet> { it.sortPosition }.thenBy { it.createdAtEpochMillis })

    private fun List<PracticeSet>.normalizedOrder(): List<PracticeSet> =
        mapIndexed { index, practiceSet -> practiceSet.copy(sortPosition = index) }

    private companion object {
        val PRACTICE_SETS = stringPreferencesKey("PRACTICE_SETS")
    }
}
