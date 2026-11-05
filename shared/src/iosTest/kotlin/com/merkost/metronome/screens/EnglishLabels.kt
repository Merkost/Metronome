package com.merkost.metronome.screens

import com.merkost.metronome.model.BeatDisplayStyle
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.model.ThemeMode

internal val ClickSound.displayName: String
    get() = when (this) {
        ClickSound.WOOD -> "Wood"
        ClickSound.CLICK -> "Click"
        ClickSound.CLASSIC -> "Classic"
        ClickSound.SOFT -> "Soft"
        ClickSound.RIM -> "Rim"
        ClickSound.CLAVE -> "Clave"
        ClickSound.STUDIO -> "Studio"
    }

internal val ClickSound.description: String
    get() = when (this) {
        ClickSound.WOOD -> "Warm, familiar wooden click"
        ClickSound.CLICK -> "Bright and sharply defined"
        ClickSound.CLASSIC -> "The original metronome pulse"
        ClickSound.SOFT -> "Rounded attack, gentle decay"
        ClickSound.RIM -> "Dry, percussive and short"
        ClickSound.CLAVE -> "Focused wooden resonance"
        ClickSound.STUDIO -> "Clean, precise electronic click"
    }

internal val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "System"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }

internal val BeatDisplayStyle.label: String
    get() = when (this) {
        BeatDisplayStyle.DOTS -> "Dots"
        BeatDisplayStyle.PENDULUM -> "Pendulum"
    }
