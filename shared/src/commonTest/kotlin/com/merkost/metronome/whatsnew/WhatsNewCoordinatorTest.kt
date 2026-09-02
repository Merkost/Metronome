package com.merkost.metronome.whatsnew

import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.platform.AppVersionInfo
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeVersionProvider(private val version: String?) : AppVersionProvider {
    override fun getAppVersion(): AppVersionInfo? = version?.let { AppVersionInfo(it, 1L) }
}

class WhatsNewCoordinatorTest {

    private fun coordinator(
        store: InMemoryPreferencesDataStore,
        version: String? = RELEASE_NOTES_VERSION,
    ) = WhatsNewCoordinator(AppDatastoreImpl(store), FakeVersionProvider(version))

    @Test
    fun aFreshInstallRecordsTheVersionInsteadOfShowingTheSheet() = runTest {
        val store = InMemoryPreferencesDataStore()
        val datastore = AppDatastoreImpl(store)
        val coordinator = coordinator(store)

        assertFalse(coordinator.shouldShow())
        assertEquals(RELEASE_NOTES_VERSION, datastore.lastSeenReleaseVersion.first())
    }

    @Test
    fun aFreshInstallStillSeesNothingAfterFinishingOnboarding() = runTest {
        val store = InMemoryPreferencesDataStore()
        val datastore = AppDatastoreImpl(store)
        val coordinator = coordinator(store)

        assertFalse(coordinator.shouldShow())
        datastore.saveOnboardingComplete(true)

        assertFalse(coordinator.shouldShow())
    }

    @Test
    fun anUpgradingUserSeesTheSheetOnceAndThenNeverAgain() = runTest {
        val store = InMemoryPreferencesDataStore()
        val datastore = AppDatastoreImpl(store)
        datastore.saveOnboardingComplete(true)
        val coordinator = coordinator(store)

        assertTrue(coordinator.shouldShow())
        coordinator.markSeen()

        assertFalse(coordinator.shouldShow())
        assertEquals(RELEASE_NOTES_VERSION, datastore.lastSeenReleaseVersion.first())
    }

    @Test
    fun anUnknownVersionNeverShowsTheSheet() = runTest {
        val store = InMemoryPreferencesDataStore()
        val coordinator = coordinator(store, version = null)

        assertFalse(coordinator.shouldShow())
    }
}
