package com.merkost.metronome.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.InstrumentKeyAction
import com.merkost.metronome.navigation.instrumentKeyAction

internal fun Modifier.desktopInstrumentKeys(
    presentation: AppPresentation,
    onTogglePlayback: () -> Unit,
    onIncreaseTempo: () -> Unit,
    onDecreaseTempo: () -> Unit,
): Modifier = composed {
    var instrumentFocused by remember { mutableStateOf(false) }
    onFocusChanged { instrumentFocused = it.isFocused }.onKeyEvent { event ->
        if (!instrumentFocused) false
        else when (instrumentKeyAction(
            presentation = presentation,
            key = event.key,
            type = event.type,
            modified = event.isAltPressed || event.isCtrlPressed || event.isMetaPressed || event.isShiftPressed,
        )) {
            InstrumentKeyAction.TogglePlayback -> { onTogglePlayback(); true }
            InstrumentKeyAction.IncreaseTempo -> { onIncreaseTempo(); true }
            InstrumentKeyAction.DecreaseTempo -> { onDecreaseTempo(); true }
            null -> false
        }
    }
}
