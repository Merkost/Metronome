package com.merkost.metronome.model

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.sound_clave
import metronome.shared.generated.resources.sound_clave_description
import metronome.shared.generated.resources.sound_classic
import metronome.shared.generated.resources.sound_classic_description
import metronome.shared.generated.resources.sound_click
import metronome.shared.generated.resources.sound_click_description
import metronome.shared.generated.resources.sound_rim
import metronome.shared.generated.resources.sound_rim_description
import metronome.shared.generated.resources.sound_soft
import metronome.shared.generated.resources.sound_soft_description
import metronome.shared.generated.resources.sound_studio
import metronome.shared.generated.resources.sound_studio_description
import metronome.shared.generated.resources.sound_wood
import metronome.shared.generated.resources.sound_wood_description
import org.jetbrains.compose.resources.StringResource

enum class ClickSound(val nameRes: StringResource, val emoji: String, val descriptionRes: StringResource) {
    WOOD(Res.string.sound_wood, "🪵", Res.string.sound_wood_description),
    CLICK(Res.string.sound_click, "👆", Res.string.sound_click_description),
    CLASSIC(Res.string.sound_classic, "🔔", Res.string.sound_classic_description),
    SOFT(Res.string.sound_soft, "☁️", Res.string.sound_soft_description),
    RIM(Res.string.sound_rim, "🥁", Res.string.sound_rim_description),
    CLAVE(Res.string.sound_clave, "🪵", Res.string.sound_clave_description),
    STUDIO(Res.string.sound_studio, "🎛️", Res.string.sound_studio_description);
}
