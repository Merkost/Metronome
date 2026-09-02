package com.merkost.metronome.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.ListMusic
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.Sparkles
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.ui.appearIn
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.sheetButtonHeight
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.whats_new_again_body
import metronome.shared.generated.resources.whats_new_again_title
import metronome.shared.generated.resources.whats_new_dismiss
import metronome.shared.generated.resources.whats_new_motion_body
import metronome.shared.generated.resources.whats_new_motion_title
import metronome.shared.generated.resources.whats_new_presets_body
import metronome.shared.generated.resources.whats_new_presets_title
import metronome.shared.generated.resources.whats_new_sets_body
import metronome.shared.generated.resources.whats_new_sets_title
import metronome.shared.generated.resources.whats_new_subtitle
import metronome.shared.generated.resources.whats_new_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class ReleaseHighlight(
    val icon: ImageVector,
    val title: StringResource,
    val body: StringResource,
)

private val RELEASE_HIGHLIGHTS = listOf(
    ReleaseHighlight(Lucide.Bookmark, Res.string.whats_new_presets_title, Res.string.whats_new_presets_body),
    ReleaseHighlight(Lucide.ListMusic, Res.string.whats_new_sets_title, Res.string.whats_new_sets_body),
    ReleaseHighlight(Lucide.RotateCcw, Res.string.whats_new_again_title, Res.string.whats_new_again_body),
    ReleaseHighlight(Lucide.Sparkles, Res.string.whats_new_motion_title, Res.string.whats_new_motion_body),
)

private const val HIGHLIGHT_STAGGER_MILLIS = 55

@Composable
fun WhatsNewSheet(
    version: String,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(title = stringResource(Res.string.whats_new_title), onDismiss = onDismiss) { dismissAnimated ->
        Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            Text(
                text = stringResource(Res.string.whats_new_subtitle, version),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.appearIn(),
            )
            RELEASE_HIGHLIGHTS.forEachIndexed { index, highlight ->
                HighlightRow(
                    highlight = highlight,
                    modifier = Modifier.appearIn(delayMillis = HIGHLIGHT_STAGGER_MILLIS * (index + 1)),
                )
            }
            Button(
                onClick = dismissAnimated,
                shape = CircleShape,
                modifier = Modifier
                    .appearIn(delayMillis = HIGHLIGHT_STAGGER_MILLIS * (RELEASE_HIGHLIGHTS.size + 1))
                    .fillMaxWidth()
                    .height(sheetButtonHeight),
            ) {
                Text(stringResource(Res.string.whats_new_dismiss), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HighlightRow(
    highlight: ReleaseHighlight,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacingMedium),
    ) {
        Surface(
            shape = RoundedCornerShape(cornerRadiusLarge),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        ) {
            Icon(
                imageVector = highlight.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp).size(20.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacingSmall / 2),
        ) {
            Text(
                text = stringResource(highlight.title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(highlight.body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
