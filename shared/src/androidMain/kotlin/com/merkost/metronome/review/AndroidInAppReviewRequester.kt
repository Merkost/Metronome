package com.merkost.metronome.review

import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidInAppReviewRequester(
    private val activityProvider: CurrentActivityProvider,
) : InAppReviewRequester, AsyncInAppReviewRequester {
    override fun requestReview(): Boolean = false

    override suspend fun awaitReviewRequest(): Boolean {
        val activity = activityProvider.current() ?: return false
        val manager = ReviewManagerFactory.create(activity)
        return suspendCancellableCoroutine { continuation ->
            manager.requestReviewFlow().addOnCompleteListener { request ->
                if (!request.isSuccessful) {
                    if (continuation.isActive) continuation.resume(false)
                    return@addOnCompleteListener
                }
                manager.launchReviewFlow(activity, request.result).addOnCompleteListener { launch ->
                    if (continuation.isActive) continuation.resume(launch.isSuccessful)
                }
            }
        }
    }
}
