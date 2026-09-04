package com.merkost.metronome.review

import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.currentTimeMillis
import kotlinx.coroutines.delay

class ReviewPromptCoordinator(
    private val store: ReviewPromptStore,
    private val requester: InAppReviewRequester,
    private val appVersionProvider: AppVersionProvider,
    private val nowMillis: () -> Long = ::currentTimeMillis,
) {
    suspend fun requestAfterQuietPause(
        snapshot: suspend () -> ReviewPromptSnapshot,
    ): Boolean {
        delay(REVIEW_PAUSE_DELAY_MILLIS)
        return requestIfEligible(snapshot())
    }

    suspend fun requestIfEligible(snapshot: ReviewPromptSnapshot): Boolean {
        val version = appVersionProvider.getAppVersion()?.versionName ?: return false
        val now = nowMillis()
        val record = store.read()
        if (!shouldRequestReview(snapshot, record, version, now)) return false
        val accepted = if (requester is AsyncInAppReviewRequester) {
            requester.awaitReviewRequest()
        } else {
            requester.requestReview()
        }
        if (!accepted) return false

        store.markRequested(version, now)
        return true
    }
}
