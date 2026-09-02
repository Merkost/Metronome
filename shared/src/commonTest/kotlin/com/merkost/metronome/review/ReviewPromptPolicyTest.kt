package com.merkost.metronome.review

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewPromptPolicyTest {
    private val eligible = ReviewPromptSnapshot(
        totalPracticeMillis = 300_000L,
        sessionPracticeMillis = 300_000L,
        isPlaying = false,
        hasActiveTimer = false,
        hasActiveTempoTrainer = false,
        hasActiveGapTrainer = false,
        isTimerSheetVisible = false,
        isTempoSheetVisible = false,
        isOnboardingVisible = false,
    )

    @Test
    fun requestsAtFiveMinutesOnAnIdlePause() {
        assertTrue(
            shouldRequestReview(
                snapshot = eligible,
                record = ReviewPromptRecord(),
                currentVersion = "1.3.0",
                nowMillis = 20_000_000_000L,
            )
        )
    }

    @Test
    fun rejectsPracticeBelowFiveMinutes() {
        assertFalse(
            shouldRequestReview(
                snapshot = eligible.copy(sessionPracticeMillis = 299_999L),
                record = ReviewPromptRecord(),
                currentVersion = "1.3.0",
                nowMillis = 20_000_000_000L,
            )
        )
    }

    @Test
    fun rejectsPlaybackThatResumed() {
        assertFalse(
            shouldRequestReview(
                snapshot = eligible.copy(isPlaying = true),
                record = ReviewPromptRecord(),
                currentVersion = "1.3.0",
                nowMillis = 20_000_000_000L,
            )
        )
    }

    @Test
    fun rejectsEveryActivePracticeSurface() {
        val blocked = listOf(
            eligible.copy(hasActiveTimer = true),
            eligible.copy(hasActiveTempoTrainer = true),
            eligible.copy(hasActiveGapTrainer = true),
            eligible.copy(hasActivePracticeSession = true),
            eligible.copy(isTimerSheetVisible = true),
            eligible.copy(isTempoSheetVisible = true),
            eligible.copy(isPresetManagementVisible = true),
            eligible.copy(isOnboardingVisible = true),
        )

        assertTrue(
            blocked.all {
                !shouldRequestReview(
                    snapshot = it,
                    record = ReviewPromptRecord(),
                    currentVersion = "1.3.0",
                    nowMillis = 20_000_000_000L,
                )
            }
        )
    }

    @Test
    fun rejectsTheSameVersionAndTheCooldownWindow() {
        val now = 20_000_000_000L

        assertFalse(
            shouldRequestReview(
                snapshot = eligible,
                record = ReviewPromptRecord("1.3.0", now - REVIEW_COOLDOWN_MILLIS - 1L),
                currentVersion = "1.3.0",
                nowMillis = now,
            )
        )
        assertFalse(
            shouldRequestReview(
                snapshot = eligible,
                record = ReviewPromptRecord("1.2.0", now - REVIEW_COOLDOWN_MILLIS + 1L),
                currentVersion = "1.3.0",
                nowMillis = now,
            )
        )
    }

    @Test
    fun allowsANewVersionAtTheCooldownBoundary() {
        val now = 20_000_000_000L

        assertTrue(
            shouldRequestReview(
                snapshot = eligible,
                record = ReviewPromptRecord("1.2.0", now - REVIEW_COOLDOWN_MILLIS),
                currentVersion = "1.3.0",
                nowMillis = now,
            )
        )
    }
}
