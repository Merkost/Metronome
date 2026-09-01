package com.merkost.metronome.practiceSets

sealed interface PracticeStepTarget {
    data object None : PracticeStepTarget
    data class Duration(val minutes: Int) : PracticeStepTarget
    data class Bars(val count: Int) : PracticeStepTarget
}

data class PracticeSetStep(
    val id: String,
    val presetId: String,
    val target: PracticeStepTarget,
)

data class PracticeSet(
    val id: String,
    val name: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val lastStartedAtEpochMillis: Long?,
    val lastCompletedAtEpochMillis: Long?,
    val sortPosition: Int,
    val steps: List<PracticeSetStep>,
) {
    fun toDraft(): PracticeSetDraft = PracticeSetDraft(name = name, steps = steps)

    companion object {
        const val MAX_SETS = 30
        const val MAX_STEPS = 20
        const val MAX_NAME_LENGTH = 80
        const val MAX_DURATION_MINUTES = 120
        const val MAX_BARS = 999
    }
}

fun List<PracticeSet>.mostRecentlyCompleted(): PracticeSet? =
    filter { it.lastCompletedAtEpochMillis != null }
        .maxWithOrNull(compareBy<PracticeSet> { it.lastCompletedAtEpochMillis }.thenBy { it.id })

fun practiceAgainSet(
    sets: List<PracticeSet>,
    activeSourceSetId: String?,
): PracticeSet? = if (activeSourceSetId == null) sets.mostRecentlyCompleted() else null

suspend fun recordPracticeCompletion(
    command: PracticeSessionCommand.SessionFinished,
    repository: PracticeSetRepository?,
): Boolean {
    if (command.reason == PracticeSessionFinishReason.Replaced) return true
    return repository?.markCompleted(command.sourceSetId) is PracticeSetMutationResult.Success
}

data class PracticeSetDraft(
    val name: String,
    val steps: List<PracticeSetStep>,
) {
    val normalizedName: String
        get() = name.trim()

    val validationError: PracticeSetValidationError?
        get() = when {
            normalizedName.isEmpty() -> PracticeSetValidationError.EMPTY_NAME
            normalizedName.length > PracticeSet.MAX_NAME_LENGTH -> PracticeSetValidationError.NAME_TOO_LONG
            steps.isEmpty() -> PracticeSetValidationError.EMPTY_STEPS
            steps.size > PracticeSet.MAX_STEPS -> PracticeSetValidationError.TOO_MANY_STEPS
            steps.any { it.id.isBlank() } -> PracticeSetValidationError.EMPTY_STEP_ID
            steps.map { it.id }.distinct().size != steps.size -> PracticeSetValidationError.DUPLICATE_STEP_ID
            steps.any { it.presetId.isBlank() } -> PracticeSetValidationError.EMPTY_PRESET_ID
            steps.any { !it.target.isValid() } -> PracticeSetValidationError.INVALID_TARGET
            else -> null
        }

    fun normalized(): PracticeSetDraft = copy(
        name = normalizedName,
        steps = steps.toList(),
    )
}

enum class PracticeSetValidationError {
    EMPTY_NAME,
    NAME_TOO_LONG,
    EMPTY_STEPS,
    TOO_MANY_STEPS,
    EMPTY_STEP_ID,
    DUPLICATE_STEP_ID,
    EMPTY_PRESET_ID,
    INVALID_TARGET,
}

internal fun PracticeStepTarget.isValid(): Boolean = when (this) {
    PracticeStepTarget.None -> true
    is PracticeStepTarget.Duration -> minutes in 1..PracticeSet.MAX_DURATION_MINUTES
    is PracticeStepTarget.Bars -> count in 1..PracticeSet.MAX_BARS
}
