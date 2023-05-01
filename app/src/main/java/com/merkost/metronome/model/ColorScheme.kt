package com.merkost.metronome.model

import androidx.compose.ui.graphics.Color
import com.merkost.metronome.ui.theme.Melrose
import com.merkost.metronome.ui.theme.MintGreen
import com.merkost.metronome.ui.theme.Periwinkle
import com.merkost.metronome.ui.theme.PinkLace

enum class ColorScheme(val color: Color) {
    WHITE(Color.White), MELROSE(Melrose), PERIWINKLE(Periwinkle), MINTGREEN(MintGreen),
    PINKLACE(PinkLace);
}