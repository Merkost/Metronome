package com.merkost.metronome.presets

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.MAX_BPM
import com.merkost.metronome.model.MIN_BPM
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature

data class PracticePreset(
    val id: String,
    val name: String,
    val createdAtEpochMillis: Long,
    val lastUsedAtEpochMillis: Long?,
    val isFavourite: Boolean,
    val sortPosition: Int,
    val bpm: Int,
    val timeSignature: TimeSignature,
    val subdivision: Subdivision,
    val beats: List<Beat>,
    val countInEnabled: Boolean,
) {
    val rhythmSummary: String
        get() = buildString {
            append("$bpm BPM · ${timeSignature.label}")
            if (subdivision != Subdivision.QUARTER) {
                append(" · ${subdivision.label}")
            }
        }

    fun toDraft(name: String = this.name): PracticePresetDraft = PracticePresetDraft(
        name = name,
        bpm = bpm,
        timeSignature = timeSignature,
        subdivision = subdivision,
        beats = beats,
        countInEnabled = countInEnabled,
    )

    companion object {
        const val MAX_PRESETS = 50
        const val MAX_NAME_LENGTH = 80
    }
}

data class PracticePresetDraft(
    val name: String,
    val bpm: Int,
    val timeSignature: TimeSignature,
    val subdivision: Subdivision,
    val beats: List<Beat>,
    val countInEnabled: Boolean,
) {
    val normalizedName: String
        get() = name.trim()

    val validationError: PresetValidationError?
        get() = when {
            normalizedName.isEmpty() -> PresetValidationError.EMPTY_NAME
            normalizedName.length > PracticePreset.MAX_NAME_LENGTH -> PresetValidationError.NAME_TOO_LONG
            bpm !in MIN_BPM..MAX_BPM -> PresetValidationError.INVALID_BPM
            beats.size != timeSignature.defaultBeats.size -> PresetValidationError.INVALID_BEATS
            else -> null
        }

    fun normalized(): PracticePresetDraft = copy(
        name = normalizedName,
        beats = beats.toList(),
    )
}

enum class PresetValidationError {
    EMPTY_NAME,
    NAME_TOO_LONG,
    INVALID_BPM,
    INVALID_BEATS,
}

fun MetronomeState.applyPracticePreset(preset: PracticePreset): MetronomeState = copy(
    rhythm = preset.bpm,
    timeSignature = preset.timeSignature,
    subdivision = preset.subdivision,
    beats = preset.beats,
)
