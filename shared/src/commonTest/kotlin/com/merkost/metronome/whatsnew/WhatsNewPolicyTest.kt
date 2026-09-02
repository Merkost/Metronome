package com.merkost.metronome.whatsnew

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WhatsNewPolicyTest {

    @Test
    fun freshInstallDoesNotSeeWhatsNew() {
        assertFalse(
            shouldShowWhatsNew(
                currentVersion = "1.3.0",
                lastSeenVersion = null,
                hasCompletedOnboarding = false,
                notesVersion = "1.3.0",
            )
        )
    }

    @Test
    fun upgradeFromABuildBeforeTheKeyExistedSeesWhatsNew() {
        assertTrue(
            shouldShowWhatsNew(
                currentVersion = "1.3.0",
                lastSeenVersion = null,
                hasCompletedOnboarding = true,
                notesVersion = "1.3.0",
            )
        )
    }

    @Test
    fun upgradeFromAnEarlierRecordedVersionSeesWhatsNew() {
        assertTrue(
            shouldShowWhatsNew(
                currentVersion = "1.3.0",
                lastSeenVersion = "1.2.1",
                hasCompletedOnboarding = true,
                notesVersion = "1.3.0",
            )
        )
    }

    @Test
    fun theSameVersionIsOnlyShownOnce() {
        assertFalse(
            shouldShowWhatsNew(
                currentVersion = "1.3.0",
                lastSeenVersion = "1.3.0",
                hasCompletedOnboarding = true,
                notesVersion = "1.3.0",
            )
        )
    }

    @Test
    fun aBuildWithoutNotesShowsNothing() {
        assertFalse(
            shouldShowWhatsNew(
                currentVersion = "1.4.0",
                lastSeenVersion = "1.3.0",
                hasCompletedOnboarding = true,
                notesVersion = "1.3.0",
            )
        )
    }

    @Test
    fun anUnknownVersionShowsNothing() {
        assertFalse(
            shouldShowWhatsNew(
                currentVersion = null,
                lastSeenVersion = "1.2.1",
                hasCompletedOnboarding = true,
                notesVersion = "1.3.0",
            )
        )
        assertFalse(
            shouldShowWhatsNew(
                currentVersion = "  ",
                lastSeenVersion = "1.2.1",
                hasCompletedOnboarding = true,
                notesVersion = "1.3.0",
            )
        )
    }

    @Test
    fun theShippedNotesVersionMatchesTheReleaseBeingShipped() {
        assertTrue(RELEASE_NOTES_VERSION.isNotBlank())
    }
}
