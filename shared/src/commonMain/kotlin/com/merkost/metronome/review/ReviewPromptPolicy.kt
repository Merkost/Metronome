package com.merkost.metronome.review

const val MIN_REVIEW_PRACTICE_MILLIS = 300_000L
const val REVIEW_PAUSE_DELAY_MILLIS = 2_000L
const val REVIEW_COOLDOWN_MILLIS = 180L * 24L * 60L * 60L * 1_000L

data class ReviewPromptSnapshot(
    val totalPracticeMillis: Long,
    val sessionPracticeMillis: Long,
    val isPlaying: Boolean,
    val hasActiveTimer: Boolean,
    val hasActiveTempoTrainer: Boolean,
    val hasActiveGapTrainer: Boolean,
    val hasActivePracticeSession: Boolean = false,
    val isTimerSheetVisible: Boolean,
    val isTempoSheetVisible: Boolean,
    val isOnboardingVisible: Boolean,
    val isPresetManagementVisible: Boolean = false,
)

data class ReviewPromptRecord(
    val lastRequestedVersion: String? = null,
    val lastRequestedAtMillis: Long? = null,
)

fun shouldRequestReview(
    snapshot: ReviewPromptSnapshot,
    record: ReviewPromptRecord,
    currentVersion: String,
    nowMillis: Long,
): Boolean {
    if (snapshot.totalPracticeMillis < MIN_REVIEW_PRACTICE_MILLIS) return false
    if (snapshot.sessionPracticeMillis < MIN_REVIEW_PRACTICE_MILLIS) return false
    if (snapshot.isPlaying) return false
    if (snapshot.hasActiveTimer) return false
    if (snapshot.hasActiveTempoTrainer) return false
    if (snapshot.hasActiveGapTrainer) return false
    if (snapshot.hasActivePracticeSession) return false
    if (snapshot.isTimerSheetVisible) return false
    if (snapshot.isTempoSheetVisible) return false
    if (snapshot.isPresetManagementVisible) return false
    if (snapshot.isOnboardingVisible) return false
    if (record.lastRequestedVersion == currentVersion) return false

    val lastRequestedAtMillis = record.lastRequestedAtMillis
    if (
        lastRequestedAtMillis != null &&
        nowMillis - lastRequestedAtMillis < REVIEW_COOLDOWN_MILLIS
    ) {
        return false
    }

    return true
}
