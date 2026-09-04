package com.merkost.metronome.practiceSets

import kotlinx.coroutines.flow.Flow

interface PracticeSetRepository {
    val sets: Flow<List<PracticeSet>>

    suspend fun create(draft: PracticeSetDraft): PracticeSetMutationResult
    suspend fun update(
        id: String,
        expectedUpdatedAtEpochMillis: Long,
        draft: PracticeSetDraft,
    ): PracticeSetMutationResult
    suspend fun delete(id: String): PracticeSetMutationResult
    suspend fun reorder(orderedIds: List<String>): PracticeSetMutationResult
    suspend fun markStarted(id: String): PracticeSetMutationResult
    suspend fun markCompleted(id: String): PracticeSetMutationResult
    suspend fun setsReferencingPreset(presetId: String): List<PracticeSet>
}

sealed interface PracticeSetMutationResult {
    data class Success(val practiceSet: PracticeSet? = null) : PracticeSetMutationResult
    data object LimitReached : PracticeSetMutationResult
    data object NotFound : PracticeSetMutationResult
    data object Conflict : PracticeSetMutationResult
    data class Invalid(val error: PracticeSetValidationError) : PracticeSetMutationResult
    data class StorageFailure(val reason: String) : PracticeSetMutationResult
}
