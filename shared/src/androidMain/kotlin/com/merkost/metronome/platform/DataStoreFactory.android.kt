package com.merkost.metronome.platform

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

fun createDataStore(context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            context.filesDir.resolve("datastore/appSettings.preferences_pb").absolutePath.toPath()
        }
    )
