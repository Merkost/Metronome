package com.merkost.metronome.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun createDataStore(): DataStore<Preferences> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )?.path ?: error("Could not resolve NSDocumentDirectory")
    val datastoreDir = "$documentDirectory/datastore"
    NSFileManager.defaultManager.createDirectoryAtPath(datastoreDir, withIntermediateDirectories = true, attributes = null, error = null)
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            "$datastoreDir/appSettings.preferences_pb".toPath()
        }
    )
}
