package com.merkost.metronome.practiceSets

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStorePracticeSessionRepository(
    private val dataStore: DataStore<Preferences>,
) : PracticeSessionRepository {
    override val session: Flow<ActivePracticeSession?> = dataStore.data.map { preferences ->
        PracticeSessionCodec.decode(preferences[ACTIVE_PRACTICE_SESSION])
    }

    override suspend fun save(session: ActivePracticeSession): Boolean = runCatching {
        require(session.isValid)
        dataStore.edit { preferences ->
            preferences[ACTIVE_PRACTICE_SESSION] = PracticeSessionCodec.encode(session)
        }
    }.isSuccess

    override suspend fun clear(): Boolean = runCatching {
        dataStore.edit { preferences ->
            preferences.remove(ACTIVE_PRACTICE_SESSION)
        }
    }.isSuccess

    private companion object {
        val ACTIVE_PRACTICE_SESSION = stringPreferencesKey("ACTIVE_PRACTICE_SESSION")
    }
}
