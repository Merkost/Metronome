package com.merkost.metronome.presets

import kotlinx.coroutines.flow.Flow

interface PracticePresetRepository {
    val presets: Flow<List<PracticePreset>>

    suspend fun migrateLegacy(countInEnabled: Boolean): MigrationResult
    suspend fun create(draft: PracticePresetDraft): PresetMutationResult
    suspend fun update(id: String, draft: PracticePresetDraft): PresetMutationResult
    suspend fun duplicate(id: String, name: String): PresetMutationResult
    suspend fun delete(id: String): PresetMutationResult
    suspend fun reorder(orderedIds: List<String>): PresetMutationResult
    suspend fun toggleFavourite(id: String): PresetMutationResult
    suspend fun markUsed(id: String): PresetMutationResult
}

sealed interface PresetMutationResult {
    data class Success(val preset: PracticePreset? = null) : PresetMutationResult
    data object LimitReached : PresetMutationResult
    data object NotFound : PresetMutationResult
    data class Invalid(val error: PresetValidationError) : PresetMutationResult
    data class StorageFailure(val reason: String) : PresetMutationResult
}

sealed interface MigrationResult {
    data object AlreadyComplete : MigrationResult
    data class Migrated(val count: Int) : MigrationResult
    data class Failed(val reason: String) : MigrationResult
}
