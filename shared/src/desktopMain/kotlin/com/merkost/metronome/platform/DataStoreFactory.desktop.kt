package com.merkost.metronome.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.nio.file.Files
import okio.Path.Companion.toOkioPath

fun createDataStore(): DataStore<Preferences> {
    val directory = DesktopSettingsPaths.dataDirectory()
    check(directory.isAbsolute) { "Metronome needs an absolute application data directory." }
    Files.createDirectories(directory)
    return PreferenceDataStoreFactory.createWithPath {
        directory.resolve("appSettings.preferences_pb").toOkioPath()
    }
}
