package com.merkost.metronome.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
 * A browser has no filesystem, so the okio-backed DataStore used on Android and
 * iOS is unavailable, and DataStore's own Storage plumbing is internal.
 * Implementing the two-method DataStore contract directly is simpler and keeps
 * the shared code untouched.
 *
 * Settings therefore live for the lifetime of the tab. That is the honest
 * behaviour for a try-it-here build: nothing is written to the visitor's
 * machine, and the web app is explicitly a demo of the real one.
 */
fun createDataStore(): DataStore<Preferences> = InMemoryPreferencesDataStore()

private class InMemoryPreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    private val mutex = Mutex()

    override val data: Flow<Preferences> = state.asStateFlow()

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences
    ): Preferences = mutex.withLock {
        val updated = transform(state.value)
        state.value = updated
        updated
    }
}
