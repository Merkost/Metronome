package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
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
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.sheetButtonHeight
import com.merkost.metronome.ui.PressedScaleSurface
import com.merkost.metronome.ui.pressScale
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ExactTempoSheet(currentBpm: Int, onApply: (Int) -> Unit, onDismiss: () -> Unit) {
    AppBottomSheet(title = "Set tempo", onDismiss = onDismiss) { dismissAnimated ->
        ExactTempoContent(currentBpm, onApply, dismissAnimated)
    }
}

internal fun sanitizeTempoInput(text: String): String = text.filter { it.isDigit() }.take(3)

internal fun tempoInputShowsError(text: String): Boolean {
    if (text.isEmpty()) return false
    val value = text.toIntOrNull()
    return value == null || value !in MIN_BPM..MAX_BPM
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExactTempoContent(currentBpm: Int, onApply: (Int) -> Unit, onDismiss: () -> Unit) {
    var input by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentBpm.toString(), selection = TextRange(0, currentBpm.toString().length)))
    }
    val bpm = input.text.toIntOrNull()
    val valid = bpm != null && bpm in MIN_BPM..MAX_BPM
    val focusRequester = remember { FocusRequester() }
    val applyInteraction = remember { MutableInteractionSource() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val apply: () -> Unit = {
        if (valid && bpm != null) {
            onApply(bpm)
            keyboard?.hide()
            onDismiss()
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        Text("$MIN_BPM–$MAX_BPM BPM", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AppTextField(
            value = input,
            onValueChange = { input = it.copy(text = sanitizeTempoInput(it.text)) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            label = "Tempo in BPM",
            isError = tempoInputShowsError(input.text),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { apply() }),
        )
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacingSmall), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
            TempoAdjustment.entries.forEach { adjustment ->
                val adjusted = bpm?.let(adjustment::applyTo)
                TextButton(
                    onClick = {
                        adjusted?.let { value ->
                            val text = value.toString()
                            input = TextFieldValue(text, selection = TextRange(text.length))
                        }
                    },
                    enabled = adjusted != null,
                    modifier = Modifier.heightIn(min = minimumTouchTargetSize).widthIn(min = minimumTouchTargetSize)
                        .semantics { contentDescription = adjustment.description },
                ) {
                    Text(adjustment.label)
                }
            }
        }
        AnimatedVisibility(visible = tempoInputShowsError(input.text), enter = AppAnimations.expandEnter, exit = AppAnimations.shrinkExit) {
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

internal enum class TempoAdjustment(val label: String, val description: String) {
    MINUS_FIVE("−5", "Decrease tempo by 5 BPM"),
    HALF("÷2", "Halve tempo"),
    DOUBLE("×2", "Double tempo"),
    PLUS_FIVE("+5", "Increase tempo by 5 BPM");

    fun applyTo(bpm: Int): Int? {
        if (bpm !in MIN_BPM..MAX_BPM) return null
        val result = when (this) {
            MINUS_FIVE -> bpm - 5
            HALF -> bpm / 2
            DOUBLE -> bpm * 2
            PLUS_FIVE -> bpm + 5
        }
        return result.takeIf { it in MIN_BPM..MAX_BPM }
    }
}

@Preview
@Composable
private fun ExactTempoSheetPreview() {
    MaterialTheme { ExactTempoSheet(80, {}, {}) }
}

@Preview
@Composable
private fun ExactTempoContentPreview() {
    MaterialTheme { ExactTempoContent(80, {}, {}) }
}
