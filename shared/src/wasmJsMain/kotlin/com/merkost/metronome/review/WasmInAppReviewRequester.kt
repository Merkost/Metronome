package com.merkost.metronome.review

// A browser has no store review prompt. Returning false means the coordinator
// records the attempt as declined and does not re-ask, rather than looping.
class WasmInAppReviewRequester : InAppReviewRequester {
    override fun requestReview(): Boolean = false
}
