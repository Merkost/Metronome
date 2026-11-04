package com.merkost.metronome.platform

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DesktopSettingsPathsTest {
    @Test
    fun sandboxHomeKeepsSettingsInsideTheAppContainer() {
        assertEquals(
            Path.of("/Users/test/Library/Containers/com.merkost.metronome/Data/Library/Application Support/Metronome"),
            DesktopSettingsPaths.dataDirectory(null, "/Users/test/Library/Containers/com.merkost.metronome/Data"),
        )
    }

    @Test
    fun explicitTestDirectoryOverridesTheHomeWithoutChangingGlobalProperties() {
        assertEquals(Path.of("/private/tmp/metronome-owned-test"), DesktopSettingsPaths.dataDirectory("/private/tmp/metronome-owned-test", "/Users/test"))
    }

    @Test
    fun relativeOverridesCannotPlaceSettingsInTheCurrentCheckout() {
        assertFailsWith<IllegalArgumentException> { DesktopSettingsPaths.dataDirectory("app-settings", "/Users/test") }
    }
}
