package com.merkost.metronome.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.merkost.metronome.components.AppChip
import com.merkost.metronome.model.PHRASE_LENGTHS
import com.merkost.metronome.ui.spacingSmall

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhraseSectionContent(
    phraseLength: Int,
    onPhraseLengthChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Text(
            text = "Accents the first bar of each phrase",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
            verticalArrangement = Arrangement.spacedBy(spacingSmall),
        ) {
            PHRASE_LENGTHS.forEach { length ->
                AppChip(
                    selected = phraseLength == length,
                    onClick = { onPhraseLengthChanged(length) },
                    label = if (length == 0) "Off" else length.toString(),
                )
            }
        }
    }
}

fun phraseSummary(phraseLength: Int): String = if (phraseLength == 0) "Off" else "$phraseLength bars"
