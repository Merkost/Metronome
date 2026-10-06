package com.merkost.metronome.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.merkost.metronome.ui.cornerRadiusXLarge
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.sheetButtonHeight
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun PracticeStarterCard(onPreview: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(cornerRadiusXLarge), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            Text("Try a starter routine", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("A steady warm-up", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("Three gentle steps. Six minutes.\nMake it your own before saving.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(60, 72, 84).forEachIndexed { index, bpm ->
                    Column(verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
                        Text(bpm.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("BPM · 2 min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    if (index < 2) Icon(Lucide.ArrowRight, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                }
            }
            Button(onClick = onPreview, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight)) {
                Text("Preview & edit", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Icon(Lucide.ArrowRight, null, modifier = Modifier.size(18.dp))
            }
            Text("Nothing is added until you save the routine.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Preview
@Composable
private fun PracticeStarterCardPreview() {
    MaterialTheme { PracticeStarterCard({}) }
}
