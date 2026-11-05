package com.merkost.metronome.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import com.merkost.metronome.model.UiText
import com.merkost.metronome.model.asString
import com.merkost.metronome.ui.AppAnimations
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.phrase_counter_description
import metronome.shared.generated.resources.phrase_edit
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhraseBarCounter(
    label: UiText?,
    phraseLength: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var shown by remember { mutableStateOf(label) }
    val counterDescription = pluralStringResource(Res.plurals.phrase_counter_description, phraseLength, phraseLength)
    val editLabel = stringResource(Res.string.phrase_edit)
    if (label != null) shown = label
    AnimatedVisibility(
        visible = label != null,
        enter = AppAnimations.expandEnter,
        exit = AppAnimations.shrinkExit,
        modifier = modifier,
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clearAndSetSemantics {
                    contentDescription = counterDescription
                    role = Role.Button
                    onClick(label = editLabel) {
                        onClick()
                        true
                    }
                },
        ) {
            AnimatedContent(
                targetState = shown,
                transitionSpec = { AppAnimations.fadeThroughFixedSize },
                label = "phraseBarLabel",
            ) { text ->
                Text(
                    text = text?.asString().orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
