package com.merkost.metronome.model

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.subdivision_eighth
import metronome.shared.generated.resources.subdivision_quarter
import metronome.shared.generated.resources.subdivision_sixteenth
import metronome.shared.generated.resources.subdivision_triplet
import org.jetbrains.compose.resources.StringResource

enum class Subdivision(val clicksPerBeat: Int, val label: String, val labelRes: StringResource) {
    QUARTER(1, "Quarter", Res.string.subdivision_quarter),
    EIGHTH(2, "Eighths", Res.string.subdivision_eighth),
    TRIPLET(3, "Triplets", Res.string.subdivision_triplet),
    SIXTEENTH(4, "16ths", Res.string.subdivision_sixteenth);
}
