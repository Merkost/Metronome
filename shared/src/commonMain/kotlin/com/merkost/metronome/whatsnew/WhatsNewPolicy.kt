package com.merkost.metronome.whatsnew

const val RELEASE_NOTES_VERSION = "1.3.0"

fun shouldShowWhatsNew(
    currentVersion: String?,
    lastSeenVersion: String?,
    hasCompletedOnboarding: Boolean,
    notesVersion: String = RELEASE_NOTES_VERSION,
): Boolean {
    if (currentVersion.isNullOrBlank()) return false
    if (currentVersion != notesVersion) return false
    if (lastSeenVersion == currentVersion) return false
    if (lastSeenVersion == null && !hasCompletedOnboarding) return false
    return true
}
