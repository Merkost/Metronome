package com.merkost.metronome.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowLeft
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.starter_card_action
import metronome.shared.generated.resources.starter_card_summary
import metronome.shared.generated.resources.starter_card_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PracticeStarterCard(onPreview: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth()
            .heightIn(min = minimumTouchTargetSize)
            .clip(RoundedCornerShape(cornerRadiusLarge))
            .pressableSurface(onPreview)
            .padding(vertical = spacingMedium),
        horizontalArrangement = Arrangement.spacedBy(spacingMedium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
            Text(stringResource(Res.string.starter_card_title), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(Res.string.starter_card_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(stringResource(Res.string.starter_card_action), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Icon(if (LocalLayoutDirection.current == LayoutDirection.Rtl) Lucide.ArrowLeft else Lucide.ArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview
@Composable
private fun PracticeStarterCardPreview() {
    MaterialTheme { PracticeStarterCard({}) }
}
