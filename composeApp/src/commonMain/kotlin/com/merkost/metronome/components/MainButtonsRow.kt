package com.merkost.metronome.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.merkost.metronome.model.StopWatchState
import com.merkost.metronome.ui.defaultPlayButtonSize
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.pulseOnChange
import com.merkost.metronome.ui.spacingSmall

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainButtonsRow(
    modifier: Modifier,
    isPlaying: Boolean,
    stopWatchState: StopWatchState,
    onPlayPause: (isPlaying: Boolean) -> Unit,
    onTempoTap: () -> Unit,
    onStopwatchLongPress: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        var secondaryButtonSize by remember {
            mutableStateOf(IntSize.Zero)
        }

        val density = LocalDensity.current

        MySecondaryButton(
            onClick = {},
            shape = RoundedCornerShape(30),
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onStopwatchLongPress
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    spacingSmall,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        density.run { secondaryButtonSize.height.toDp() },
                        defaultPlayButtonSize
                    )
                    .padding(16.dp)
            ) {
                Icon(Icons.Rounded.Alarm, Icons.Rounded.Alarm.name)
                Text(
                    text = TimestampMillisecondsFormatter.format(stopWatchState.elapsedTime),
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }

        Spacer(modifier = Modifier.size(horizontalPadding))
        PlayButton(
            Modifier,
            isPlaying = isPlaying,
            onClick = { onPlayPause(isPlaying) }
        )
        Spacer(modifier = Modifier.size(horizontalPadding))

        // Tap tempo with pulse feedback via reusable modifier
        var tapCount by remember { mutableStateOf(0) }

        MySecondaryButton(
            onClick = {
                tapCount++
                onTempoTap()
            },
            shape = RoundedCornerShape(30),
            modifier = Modifier
                .pulseOnChange(tapCount)
                .onSizeChanged {
                    secondaryButtonSize = it
                }
                .weight(1f)
                .fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    spacingSmall,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
                    .heightIn(max = defaultPlayButtonSize)
            ) {
                Icon(Icons.Rounded.TouchApp, Icons.Rounded.TouchApp.name)
                Text(
                    text = "Tap\nTempo",
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
