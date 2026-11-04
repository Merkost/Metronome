package com.merkost.metronome.platform

import java.nio.file.Path

object DesktopSettingsPaths {
    fun dataDirectory(
        overrideDirectory: String? = System.getProperty("metronome.dataDir"),
        homeDirectory: String? = System.getProperty("user.home")?.takeIf(String::isNotBlank) ?: System.getenv("HOME"),
    ): Path {
        val override = overrideDirectory?.takeIf(String::isNotBlank)
        val path = if (override != null) {
            Path.of(override)
        } else {
            val home = requireNotNull(homeDirectory?.takeIf(String::isNotBlank)) {
                "Metronome couldn't find your application data folder."
            }
            Path.of(home).resolve("Library/Application Support/Metronome")
        }
        require(path.isAbsolute) { "Metronome needs an absolute application data folder." }
        return path.normalize()
    }
}
