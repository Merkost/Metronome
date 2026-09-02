package com.merkost.metronome.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.metronome.practiceSets.PracticeSessionController
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.practiceSets.PracticeSetDraft
import com.merkost.metronome.practiceSets.PracticeSetMutationResult
import com.merkost.metronome.practiceSets.PracticeSetRepository
import com.merkost.metronome.practiceSets.PracticeSetStep
import com.merkost.metronome.practiceSets.PracticeSetValidationError
import com.merkost.metronome.practiceSets.PracticeStepTarget
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.PracticePresetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PracticeSetEditorState(
    val sourceId: String?,
    val expectedUpdatedAtEpochMillis: Long?,
    val name: String,
    val steps: List<PracticeSetStep>,
    val isReordering: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
)

data class PracticeSetsUiState(
    val sets: List<PracticeSet> = emptyList(),
    val presets: List<PracticePreset> = emptyList(),
    val editor: PracticeSetEditorState? = null,
    val activeSourceSetId: String? = null,
    val isReordering: Boolean = false,
)

sealed interface PracticeSetUiEvent {
    data class Saved(val practiceSet: PracticeSet) : PracticeSetUiEvent
    data class Updated(val practiceSet: PracticeSet) : PracticeSetUiEvent
    data class Deleted(val name: String) : PracticeSetUiEvent
    data class Invalid(val error: PracticeSetValidationError) : PracticeSetUiEvent
    data object ActiveSetLocked : PracticeSetUiEvent
    data object LimitReached : PracticeSetUiEvent
    data object Conflict : PracticeSetUiEvent
    data object StorageFailure : PracticeSetUiEvent
}

class PracticeSetsViewModel(
    private val repository: PracticeSetRepository,
    private val presetRepository: PracticePresetRepository,
    private val sessionController: PracticeSessionController,
    private val nextStepId: () -> String,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private val workerScope = scope ?: viewModelScope
    private val mutableUiState = MutableStateFlow(PracticeSetsUiState())
    val uiState: StateFlow<PracticeSetsUiState> = mutableUiState.asStateFlow()

    private val eventChannel = Channel<PracticeSetUiEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        workerScope.launch {
            repository.sets.collect { sets ->
                mutableUiState.update { it.copy(sets = sets) }
            }
        }
        workerScope.launch {
            presetRepository.presets.collect { presets ->
                mutableUiState.update { it.copy(presets = presets) }
            }
        }
        workerScope.launch {
            sessionController.state.collect { sessionState ->
                mutableUiState.update {
                    it.copy(activeSourceSetId = sessionState.session?.sourceSetId)
                }
            }
        }
    }

    fun beginCreate() {
        mutableUiState.update {
            it.copy(
                editor = PracticeSetEditorState(
                    sourceId = null,
                    expectedUpdatedAtEpochMillis = null,
                    name = "",
                    steps = emptyList(),
                ),
            )
        }
    }

    fun beginEdit(id: String) {
        if (id == uiState.value.activeSourceSetId) {
            emit(PracticeSetUiEvent.ActiveSetLocked)
            return
        }
        val practiceSet = uiState.value.sets.firstOrNull { it.id == id } ?: return
        mutableUiState.update {
            it.copy(
                editor = PracticeSetEditorState(
                    sourceId = practiceSet.id,
                    expectedUpdatedAtEpochMillis = practiceSet.updatedAtEpochMillis,
                    name = practiceSet.name,
                    steps = practiceSet.steps,
                ),
            )
        }
    }

    fun setName(name: String) {
        updateEditor { it.copy(name = name, hasUnsavedChanges = true) }
    }

    fun addPreset(preset: PracticePreset) {
        updateEditor { editor ->
            if (editor.steps.size >= PracticeSet.MAX_STEPS) return@updateEditor editor
            editor.copy(
                steps = editor.steps + PracticeSetStep(uniqueStepId(editor.steps), preset.id, PracticeStepTarget.None),
                hasUnsavedChanges = true,
            )
        }
    }

    fun removeStep(id: String) {
        updateEditor { editor ->
            editor.copy(
                steps = editor.steps.filterNot { it.id == id },
                hasUnsavedChanges = true,
            )
        }
    }

    fun moveStep(id: String, targetIndex: Int) {
        updateEditor { editor ->
            val sourceIndex = editor.steps.indexOfFirst { it.id == id }
            if (sourceIndex == -1) return@updateEditor editor
            val destination = targetIndex.coerceIn(editor.steps.indices)
            if (sourceIndex == destination) return@updateEditor editor
            editor.copy(
                steps = editor.steps.toMutableList().apply {
                    add(destination, removeAt(sourceIndex))
                },
                hasUnsavedChanges = true,
            )
        }
    }

    fun setTarget(stepId: String, target: PracticeStepTarget) {
        updateEditor { editor ->
            editor.copy(
                steps = editor.steps.map { step ->
                    if (step.id == stepId) step.copy(target = target) else step
                },
                hasUnsavedChanges = true,
            )
        }
    }

    fun setEditorReordering(isReordering: Boolean) {
        updateEditor { it.copy(isReordering = isReordering) }
    }

    fun cancelEditing() {
        mutableUiState.update { it.copy(editor = null) }
    }

    fun save() {
        val editor = uiState.value.editor ?: return
        val draft = PracticeSetDraft(editor.name, editor.steps).normalized()
        draft.validationError?.let {
            emit(PracticeSetUiEvent.Invalid(it))
            return
        }
        workerScope.launch {
            val result = if (editor.sourceId == null) {
                repository.create(draft)
            } else {
                repository.update(
                    id = editor.sourceId,
                    expectedUpdatedAtEpochMillis = editor.expectedUpdatedAtEpochMillis ?: return@launch,
                    draft = draft,
                )
            }
            when (result) {
                is PracticeSetMutationResult.Success -> {
                    val saved = result.practiceSet ?: return@launch
                    mutableUiState.update { it.copy(editor = null) }
                    eventChannel.send(
                        if (editor.sourceId == null) PracticeSetUiEvent.Saved(saved)
                        else PracticeSetUiEvent.Updated(saved),
                    )
                }
                else -> emitFailure(result)
            }
        }
    }

    fun delete(id: String) {
        if (id == uiState.value.activeSourceSetId) {
            emit(PracticeSetUiEvent.ActiveSetLocked)
            return
        }
        val name = uiState.value.sets.firstOrNull { it.id == id }?.name.orEmpty()
        workerScope.launch {
            when (val result = repository.delete(id)) {
                is PracticeSetMutationResult.Success -> eventChannel.send(PracticeSetUiEvent.Deleted(name))
                else -> emitFailure(result)
            }
        }
    }

    fun move(id: String, targetIndex: Int) {
        val current = uiState.value.sets
        val sourceIndex = current.indexOfFirst { it.id == id }
        if (sourceIndex == -1) return
        val destination = targetIndex.coerceIn(current.indices)
        if (sourceIndex == destination) return
        val reordered = current.toMutableList().apply { add(destination, removeAt(sourceIndex)) }
        workerScope.launch {
            val result = repository.reorder(reordered.map { it.id })
            if (result !is PracticeSetMutationResult.Success) emitFailure(result)
        }
    }

    fun setReordering(isReordering: Boolean) {
        mutableUiState.update { it.copy(isReordering = isReordering) }
    }

    private fun updateEditor(transform: (PracticeSetEditorState) -> PracticeSetEditorState) {
        mutableUiState.update { current ->
            current.editor?.let { current.copy(editor = transform(it)) } ?: current
        }
    }

    private fun uniqueStepId(existing: List<PracticeSetStep>): String {
        val used = existing.mapTo(mutableSetOf()) { it.id }
        var candidate: String
        do {
            candidate = nextStepId()
        } while (candidate.isBlank() || candidate in used)
        return candidate
    }

    private fun emit(event: PracticeSetUiEvent) {
        workerScope.launch { eventChannel.send(event) }
    }

    private suspend fun emitFailure(result: PracticeSetMutationResult) {
        eventChannel.send(
            when (result) {
                PracticeSetMutationResult.LimitReached -> PracticeSetUiEvent.LimitReached
                PracticeSetMutationResult.Conflict -> PracticeSetUiEvent.Conflict
                is PracticeSetMutationResult.Invalid -> PracticeSetUiEvent.Invalid(result.error)
                else -> PracticeSetUiEvent.StorageFailure
            },
        )
    }
}
