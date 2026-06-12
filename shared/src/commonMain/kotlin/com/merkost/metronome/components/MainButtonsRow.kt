package com.merkost.metronome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.merkost.metronome.model.StopWatchState
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.spacingSmall

@Composable
fun MainButtonsRow(
    modifier: Modifier,
    isPlaying: Boolean,
    stopWatchState: StopWatchState,
    timerGoal: Long?,
    timerRemaining: Long,
    onPlayPause: (isPlaying: Boolean) -> Unit,
    onTempoTap: () -> Unit,
    onTimerClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimerChip(
            modifier = Modifier.weight(1f),
            stopWatchElapsed = stopWatchState.elapsedTime,
            timerGoal = timerGoal,
            timerRemaining = timerRemaining,
            isPlaying = isPlaying,
            onClick = onTimerClick,
        )

        Spacer(modifier = Modifier.size(horizontalPadding))
        PlayButton(
            Modifier,
            isPlaying = isPlaying,
            onClick = { onPlayPause(isPlaying) }
        )
        Spacer(modifier = Modifier.size(horizontalPadding))

        val haptics = LocalHapticFeedback.current
        MySecondaryButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTempoTap()
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacingSmall, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 24.dp)
                    .padding(16.dp)
            ) {
                Icon(Icons.Rounded.TouchApp, Icons.Rounded.TouchApp.name)
                Text(
                    text = "Tap Tempo",
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
