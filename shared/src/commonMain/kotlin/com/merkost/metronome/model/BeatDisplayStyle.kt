package com.merkost.metronome.model

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.beat_display_dots
import metronome.shared.generated.resources.beat_display_pendulum
import org.jetbrains.compose.resources.StringResource

enum class BeatDisplayStyle(val labelRes: StringResource) {
    DOTS(Res.string.beat_display_dots),
    PENDULUM(Res.string.beat_display_pendulum);
}
