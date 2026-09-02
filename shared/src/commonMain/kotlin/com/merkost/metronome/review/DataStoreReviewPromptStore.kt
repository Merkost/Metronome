package com.merkost.metronome.review

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class DataStoreReviewPromptStore(
    private val dataStore: DataStore<Preferences>,
) : ReviewPromptStore {
    private companion object {
        val LastReviewVersion = stringPreferencesKey("last_review_version")
        val LastReviewAtMillis = longPreferencesKey("last_review_at_millis")
    }

    override suspend fun read(): ReviewPromptRecord {
        val preferences = dataStore.data.first()
        return ReviewPromptRecord(
            lastRequestedVersion = preferences[LastReviewVersion],
            lastRequestedAtMillis = preferences[LastReviewAtMillis],
        )
    }

    override suspend fun markRequested(version: String, atMillis: Long) {
        dataStore.edit { preferences ->
            preferences[LastReviewVersion] = version
            preferences[LastReviewAtMillis] = atMillis
        }
    }
}
