package com.merkost.metronome.presets

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ActivePresetState(
    val active: PracticePreset? = null,
    val pending: PracticePreset? = null,
    val isEdited: Boolean = false,
)

class ActivePresetTracker {
    private val mutableState = MutableStateFlow(ActivePresetState())
    val state: StateFlow<ActivePresetState> = mutableState.asStateFlow()

    fun pending(preset: PracticePreset) {
        mutableState.update { it.copy(pending = preset) }
    }

    fun request(preset: PracticePreset, isPlaying: Boolean): PracticePreset? {
        return if (isPlaying) {
            pending(preset)
            null
        } else {
            applied(preset)
            preset
        }
    }

    fun consumePendingAtBarBoundary(): PracticePreset? {
        val preset = mutableState.value.pending ?: return null
        applied(preset)
        return preset
    }

    fun applied(preset: PracticePreset) {
        mutableState.value = ActivePresetState(active = preset)
    }

    fun changed() {
        mutableState.update { current ->
            if (current.active == null) current else current.copy(isEdited = true)
        }
    }

    fun removed(id: String) {
        mutableState.update { current ->
            current.copy(
                active = current.active?.takeUnless { it.id == id },
                pending = current.pending?.takeUnless { it.id == id },
                isEdited = if (current.active?.id == id) false else current.isEdited,
            )
        }
    }
}
