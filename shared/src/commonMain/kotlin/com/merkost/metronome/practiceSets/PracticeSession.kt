package com.merkost.metronome.practiceSets

import com.merkost.metronome.presets.PracticePreset

enum class PracticePlaybackIntent {
    Paused,
    Running,
}

data class ResolvedPracticeStep(
    val stepId: String,
    val preset: PracticePreset,
    val target: PracticeStepTarget,
)

data class ActivePracticeSession(
    val id: String,
    val sourceSetId: String,
    val setName: String,
    val steps: List<ResolvedPracticeStep>,
    val currentStepIndex: Int,
    val pendingStepIndex: Int?,
    val elapsedMillis: Long,
    val completedBars: Int,
    val playbackIntent: PracticePlaybackIntent,
    val targetReached: Boolean,
    val currentStepEdited: Boolean,
    val startedAtEpochMillis: Long,
    val lastCheckpointAtEpochMillis: Long,
) {
    val currentStep: ResolvedPracticeStep
        get() = steps[currentStepIndex]

    val isLastStep: Boolean
        get() = currentStepIndex == steps.lastIndex

    internal val isValid: Boolean
        get() = id.isNotBlank() &&
            sourceSetId.isNotBlank() &&
            setName.isNotBlank() &&
            steps.size in 1..PracticeSet.MAX_STEPS &&
            steps.all { it.stepId.isNotBlank() && it.preset.id.isNotBlank() && it.target.isValid() } &&
            steps.map { it.stepId }.distinct().size == steps.size &&
            currentStepIndex in steps.indices &&
            pendingStepIndex?.let { it in steps.indices } != false &&
            elapsedMillis >= 0L &&
            completedBars >= 0 &&
            startedAtEpochMillis >= 0L &&
            lastCheckpointAtEpochMillis >= 0L
}
