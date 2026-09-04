package com.merkost.metronome.review

interface InAppReviewRequester {
    fun requestReview(): Boolean
}

interface AsyncInAppReviewRequester {
    suspend fun awaitReviewRequest(): Boolean
}
