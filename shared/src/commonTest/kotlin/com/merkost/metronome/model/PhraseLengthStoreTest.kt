package com.merkost.metronome.model

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PhraseLengthStoreTest {
    @Test
    fun aMissingLengthReadsAsOff() = runTest {
        val store = AppDatastoreImpl(InMemoryPreferencesDataStore())
        assertEquals(0, store.phraseLength.first())
    }

    @Test
    fun anUnknownLengthReadsAsOff() = runTest {
        val preferences = InMemoryPreferencesDataStore()
        preferences.edit { it[intPreferencesKey("PHRASE_LENGTH")] = 7 }
        assertEquals(0, AppDatastoreImpl(preferences).phraseLength.first())
    }

    @Test
    fun theLengthIsRemembered() = runTest {
        val store = AppDatastoreImpl(InMemoryPreferencesDataStore())
        store.savePhraseLength(12)
        assertEquals(12, store.phraseLength.first())
    }

    @Test
    fun savingAnUnknownLengthStoresOff() = runTest {
        val store = AppDatastoreImpl(InMemoryPreferencesDataStore())
        store.savePhraseLength(12)
        store.savePhraseLength(5)
        assertEquals(0, store.phraseLength.first())
    }
}
