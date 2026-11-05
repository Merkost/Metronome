package com.merkost.metronome.model

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.theme_dark
import metronome.shared.generated.resources.theme_light
import metronome.shared.generated.resources.theme_system
import org.jetbrains.compose.resources.StringResource

enum class ThemeMode(val storageKey: String, val labelRes: StringResource) {
    SYSTEM("SYSTEM", Res.string.theme_system),
    LIGHT("LIGHT", Res.string.theme_light),
    DARK("DARK", Res.string.theme_dark);

    companion object {
        fun fromStorageKey(key: String?): ThemeMode =
            entries.firstOrNull { it.storageKey == key } ?: SYSTEM
    }
}
