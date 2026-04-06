package com.merkost.metronome.platform

data class AppVersionInfo(val versionName: String, val versionNumber: Long)

interface AppVersionProvider {
    fun getAppVersion(): AppVersionInfo?
}
