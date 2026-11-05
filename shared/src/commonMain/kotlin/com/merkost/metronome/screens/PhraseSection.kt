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
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.phrase_bars
import metronome.shared.generated.resources.phrase_hint
import metronome.shared.generated.resources.phrase_off
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhraseSectionContent(
    phraseLength: Int,
    onPhraseLengthChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Text(
            text = stringResource(Res.string.phrase_hint),
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
                    label = if (length == 0) stringResource(Res.string.phrase_off) else length.toString(),
                )
            }
        }
    }
}

@Composable
fun phraseSummary(phraseLength: Int): String =
    if (phraseLength == 0) stringResource(Res.string.phrase_off) else pluralStringResource(Res.plurals.phrase_bars, phraseLength, phraseLength)
