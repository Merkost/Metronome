package com.merkost.metronome.whatsnew

import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.platform.AppVersionProvider
import kotlinx.coroutines.flow.first

class WhatsNewCoordinator(
    private val appDatastore: AppDatastore,
    private val appVersionProvider: AppVersionProvider,
) {
    fun currentVersion(): String? = appVersionProvider.getAppVersion()?.versionName

    suspend fun shouldShow(): Boolean {
        val current = currentVersion() ?: return false
        val lastSeen = appDatastore.lastSeenReleaseVersion.first()
        val show = shouldShowWhatsNew(
            currentVersion = current,
            lastSeenVersion = lastSeen,
            hasCompletedOnboarding = appDatastore.onboardingComplete.first(),
        )
        if (!show && lastSeen == null) {
            appDatastore.saveLastSeenReleaseVersion(current)
        }
        return show
    }

    suspend fun markSeen() {
        val version = currentVersion() ?: return
        appDatastore.saveLastSeenReleaseVersion(version)
    }
}
