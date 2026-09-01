package com.merkost.metronome.review

interface ReviewPromptStore {
    suspend fun read(): ReviewPromptRecord

    suspend fun markRequested(version: String, atMillis: Long)
}
