package com.merkost.metronome.practiceSets

import kotlinx.coroutines.flow.Flow

interface PracticeSessionRepository {
    val session: Flow<ActivePracticeSession?>

    suspend fun save(session: ActivePracticeSession): Boolean
    suspend fun clear(): Boolean
}
