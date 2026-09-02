package com.merkost.metronome.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.metronome.presets.MigrationResult
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.PracticePresetDraft
import com.merkost.metronome.presets.PracticePresetRepository
import com.merkost.metronome.presets.PresetMutationResult
import com.merkost.metronome.presets.PresetValidationError
import com.merkost.metronome.practiceSets.PracticeSetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PracticePresetsUiState(
    val presets: List<PracticePreset> = emptyList(),
    val favourites: List<PracticePreset> = emptyList(),
    val recents: List<PracticePreset> = emptyList(),
    val isReordering: Boolean = false,
    val migrationFailed: Boolean = false,
)

sealed interface PresetUiEvent {
    data class Saved(val preset: PracticePreset) : PresetUiEvent
    data class Updated(val preset: PracticePreset) : PresetUiEvent
    data class Duplicated(val preset: PracticePreset) : PresetUiEvent
    data class Deleted(val name: String, val id: String) : PresetUiEvent
    data object LimitReached : PresetUiEvent
    data class InUse(val setNames: List<String>) : PresetUiEvent
    data class Invalid(val error: PresetValidationError) : PresetUiEvent
    data object StorageFailure : PresetUiEvent
}

class PracticePresetsViewModel(
    private val repository: PracticePresetRepository,
    private val countInEnabled: Flow<Boolean>,
    scope: CoroutineScope? = null,
    private val practiceSetRepository: PracticeSetRepository? = null,
) : ViewModel() {
    private val workerScope = scope ?: viewModelScope
    private val mutableUiState = MutableStateFlow(PracticePresetsUiState())
    val uiState: StateFlow<PracticePresetsUiState> = mutableUiState.asStateFlow()

    private val eventChannel = Channel<PresetUiEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        workerScope.launch {
            repository.presets.collect { presets ->
                val displayedPresets = presets.sortedByDescending { it.isFavourite }
                mutableUiState.update { current ->
                    current.copy(
                        presets = displayedPresets,
                        favourites = displayedPresets.filter { it.isFavourite }.take(MAX_QUICK_PRESETS),
                        recents = presets
                            .filter { it.lastUsedAtEpochMillis != null }
                            .sortedByDescending { it.lastUsedAtEpochMillis }
                            .take(MAX_QUICK_PRESETS),
                    )
                }
            }
        }
        retryMigration()
    }

    fun retryMigration() {
        workerScope.launch {
            val result = repository.migrateLegacy(countInEnabled.first())
            mutableUiState.update { current ->
                current.copy(migrationFailed = result is MigrationResult.Failed)
            }
        }
    }

    fun create(draft: PracticePresetDraft) {
        workerScope.launch {
            handleMutation(repository.create(draft.normalized())) { preset ->
                PresetUiEvent.Saved(preset)
            }
        }
    }

    fun update(id: String, draft: PracticePresetDraft) {
        workerScope.launch {
            handleMutation(repository.update(id, draft.normalized())) { preset ->
                PresetUiEvent.Updated(preset)
            }
        }
    }

    fun duplicate(id: String, name: String) {
        workerScope.launch {
            handleMutation(repository.duplicate(id, name.trim())) { preset ->
                PresetUiEvent.Duplicated(preset)
            }
        }
    }

    fun delete(id: String) {
        val name = uiState.value.presets.firstOrNull { it.id == id }?.name.orEmpty()
        workerScope.launch {
            val references = practiceSetRepository?.setsReferencingPreset(id).orEmpty()
            if (references.isNotEmpty()) {
                eventChannel.send(PresetUiEvent.InUse(references.map { it.name }))
                return@launch
            }
            when (val result = repository.delete(id)) {
                is PresetMutationResult.Success -> eventChannel.send(PresetUiEvent.Deleted(name, id))
                else -> emitFailure(result)
            }
        }
    }

    fun toggleFavourite(id: String) {
        workerScope.launch {
            val result = repository.toggleFavourite(id)
            if (result !is PresetMutationResult.Success) emitFailure(result)
        }
    }

    fun move(id: String, targetIndex: Int) {
        val current = uiState.value.presets
        val sourceIndex = current.indexOfFirst { it.id == id }
        if (sourceIndex == -1) return
        val destination = targetIndex.coerceIn(current.indices)
        if (sourceIndex == destination) return
        val reordered = current.toMutableList().apply {
            add(destination, removeAt(sourceIndex))
        }
        workerScope.launch {
            val result = repository.reorder(reordered.map { it.id })
            if (result !is PresetMutationResult.Success) emitFailure(result)
        }
    }

    fun setReordering(reordering: Boolean) {
        mutableUiState.update { it.copy(isReordering = reordering) }
    }

    private suspend fun handleMutation(
        result: PresetMutationResult,
        successEvent: (PracticePreset) -> PresetUiEvent,
    ) {
        when (result) {
            is PresetMutationResult.Success -> result.preset?.let { eventChannel.send(successEvent(it)) }
            else -> emitFailure(result)
        }
    }

    private suspend fun emitFailure(result: PresetMutationResult) {
        val event = when (result) {
            PresetMutationResult.LimitReached -> PresetUiEvent.LimitReached
            is PresetMutationResult.Invalid -> PresetUiEvent.Invalid(result.error)
            else -> PresetUiEvent.StorageFailure
        }
        eventChannel.send(event)
    }

    private companion object {
        const val MAX_QUICK_PRESETS = 4
    }
}
