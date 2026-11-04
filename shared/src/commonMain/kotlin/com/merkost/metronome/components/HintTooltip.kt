package com.merkost.metronome.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppPresentation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HintTooltip(
    hint: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (hint == null || LocalAppPresentation.current != AppPresentation.Desktop) {
        Box(modifier) { content() }
        return
    }
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(hint) } },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content,
    )
}
