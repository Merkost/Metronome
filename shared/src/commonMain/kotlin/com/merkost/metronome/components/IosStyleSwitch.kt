package com.merkost.metronome.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.merkost.metronome.ui.theme.readableThemeColor
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.minimumTouchTargetSize
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun IosStyleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val latestChecked by rememberUpdatedState(checked)
    val latestOnChange by rememberUpdatedState(onCheckedChange)
    var dragPosition by remember { mutableStateOf<Float?>(null) }
    val progress by animateFloatAsState(
        targetValue = dragPosition ?: if (checked) 1f else 0f,
        animationSpec = if (dragPosition == null) AppAnimations.standard() else snap(),
        label = "iosSwitchPosition",
    )
    val track by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = AppAnimations.standard(),
        label = "iosSwitchTrack",
    )
    val animatedThumb by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = AppAnimations.standard(),
        label = "iosSwitchThumb",
    )
    val thumb = readableThemeColor(animatedThumb, track, MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.inverseOnSurface)
    val outline = MaterialTheme.colorScheme.outlineVariant
    val thumbScale by animateFloatAsState(if (pressed || dragPosition != null) 1.04f else 1f, AppAnimations.press(), label = "iosSwitchTouch")
    val travel = 20.dp
    val travelPx = with(LocalDensity.current) { travel.toPx() }
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val latestProgress by rememberUpdatedState(progress)
    val latestTravelPx by rememberUpdatedState(travelPx)
    val latestDirection by rememberUpdatedState(direction)
    Box(
        modifier = modifier
            .size(64.dp, minimumTouchTargetSize)
            .toggleable(value = checked, interactionSource = interactions, indication = null, role = Role.Switch, onValueChange = onCheckedChange)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragPosition = latestProgress },
                    onHorizontalDrag = { change, delta ->
                        change.consume()
                        dragPosition = ((dragPosition ?: if (latestChecked) 1f else 0f) + delta * latestDirection / latestTravelPx).coerceIn(0f, 1f)
                    },
                    onDragCancel = { dragPosition = null },
                    onDragEnd = {
                        val next = (dragPosition ?: if (latestChecked) 1f else 0f) >= 0.5f
                        dragPosition = null
                        if (next != latestChecked) latestOnChange(next)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(52.dp, 32.dp).clip(RoundedCornerShape(50)).background(track).border(1.dp, outline, RoundedCornerShape(50))) {
            Box(
                Modifier.align(Alignment.CenterStart).offset(x = 2.dp + travel * progress)
                    .size(28.dp * thumbScale).clip(CircleShape).background(thumb).border(0.5.dp, outline, CircleShape),
            )
        }
    }
}

@Preview
@Composable
private fun IosStyleSwitchPreview() {
    MaterialTheme { IosStyleSwitch(true, {}) }
}
