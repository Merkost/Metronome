package com.merkost.metronome.model

enum class ClickSound(val displayName: String, val emoji: String, val description: String) {
    WOOD("Wood", "\uD83E\uDEB5", "Warm, familiar wooden click"),
    CLICK("Click", "\uD83D\uDC46", "Bright and sharply defined"),
    CLASSIC("Classic", "\uD83D\uDD14", "The original metronome pulse"),
    SOFT("Soft", "\u2601\uFE0F", "Rounded attack, gentle decay"),
    RIM("Rim", "\uD83E\uDD41", "Dry, percussive and short"),
    CLAVE("Clave", "\uD83E\uDEB5", "Focused wooden resonance"),
    STUDIO("Studio", "\uD83C\uDF9B\uFE0F", "Clean, precise electronic click");
}
