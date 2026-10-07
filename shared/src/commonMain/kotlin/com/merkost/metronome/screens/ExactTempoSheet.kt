package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.AppTextField
import com.merkost.metronome.model.MAX_BPM
import com.merkost.metronome.model.MIN_BPM
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.sheetButtonHeight
import com.merkost.metronome.ui.PressedScaleSurface
import com.merkost.metronome.ui.pressScale
import com.merkost.metronome.ui.spacingMedium
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ExactTempoSheet(
    currentBpm: Int,
    onApply: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var input by remember {
        mutableStateOf(TextFieldValue(currentBpm.toString(), selection = TextRange(0, currentBpm.toString().length)))
    }
    val bpm = input.text.toIntOrNull()
    val valid = bpm != null && bpm in MIN_BPM..MAX_BPM
    val focusRequester = remember { FocusRequester() }
    val applyInteraction = remember { MutableInteractionSource() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    AppBottomSheet(title = "Set tempo", onDismiss = onDismiss) { dismissAnimated ->
        val apply: () -> Unit = {
            if (valid && bpm != null) {
                onApply(bpm)
                keyboard?.hide()
                dismissAnimated()
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            Text(
                "Choose your pace, from $MIN_BPM to $MAX_BPM BPM.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                label = "Tempo in BPM",
                isError = !valid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { apply() }),
            )
            AnimatedVisibility(
                visible = !valid,
                enter = AppAnimations.expandEnter,
                exit = AppAnimations.shrinkExit,
            ) {
                Text(
                    "Enter a whole number from $MIN_BPM to $MAX_BPM.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            Button(
                onClick = apply,
                enabled = valid,
                modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight).pressScale(applyInteraction, PressedScaleSurface),
                interactionSource = applyInteraction,
            ) {
                Text("Set tempo")
            }
        }
    }
}

@Preview
@Composable
private fun ExactTempoSheetPreview() {
    MaterialTheme {
        ExactTempoSheet(80, {}, {})
    }
}
