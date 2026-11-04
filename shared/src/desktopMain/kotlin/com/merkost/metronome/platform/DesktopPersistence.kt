package com.merkost.metronome.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

interface DesktopPersistence {
    suspend fun awaitPendingWrites()
}

class DesktopDataStorePersistence(private val dataStore: DataStore<Preferences>) : DesktopPersistence {
    override suspend fun awaitPendingWrites() {
        dataStore.updateData { it }
    }
}
