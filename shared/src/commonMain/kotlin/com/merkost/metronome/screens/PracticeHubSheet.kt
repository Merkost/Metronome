package com.merkost.metronome.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ChartNoAxesColumn
import com.composables.icons.lucide.Timer
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.VolumeX
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.TimestampMillisecondsFormatter
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PracticeHubSheet(
    presetCount: Int,
    practiceSetCount: Int,
    timerMinutes: Int,
    todayPracticeTime: Long,
    practiceStreak: Int,
    totalPracticeTime: Long,
    onOpenTimer: () -> Unit,
    onOpenTempoTrainer: () -> Unit,
    onOpenGapTrainer: () -> Unit,
    onOpenPresets: () -> Unit,
    onOpenPracticeSets: () -> Unit,
    onOpenPracticeData: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(title = "Practice", onDismiss = onDismiss) { dismissAnimated ->
        Text(
            "A little structure goes a long way.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = spacingMedium),
            verticalArrangement = Arrangement.spacedBy(spacingSmall),
        ) {
            Text("Your practice", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            PracticeHubRow(Lucide.Bookmark, "Saved presets", "Return to a setup you love", value = presetCount.toString()) {
                onOpenPresets()
                dismissAnimated()
            }
            PracticeHubRow(Lucide.Layers, "Practice sets", "Turn your presets into a routine", value = practiceSetCount.toString()) {
                onOpenPracticeSets()
                dismissAnimated()
            }
            Text("Training tools", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = spacingMedium))
            PracticeHubRow(Lucide.Timer, "Practice timer", "Make time for a focused block", value = "$timerMinutes min") {
                onOpenTimer()
                dismissAnimated()
            }
            PracticeHubRow(Lucide.TrendingUp, "Tempo trainer", "Build speed, one bar at a time") {
                onOpenTempoTrainer()
                dismissAnimated()
            }
            PracticeHubRow(Lucide.VolumeX, "Gap trainer", "Keep the pulse through silence") {
                onOpenGapTrainer()
                dismissAnimated()
            }
            Text("Practice summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = spacingMedium))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = spacingSmall),
                horizontalArrangement = Arrangement.spacedBy(spacingSmall),
            ) {
                PracticeHubStatistic("Today", TimestampMillisecondsFormatter.formatHuman(todayPracticeTime), Modifier.weight(1f))
                PracticeHubStatistic("Streak", "$practiceStreak ${if (practiceStreak == 1) "day" else "days"}", Modifier.weight(1f))
                PracticeHubStatistic("Total", TimestampMillisecondsFormatter.formatHuman(totalPracticeTime), Modifier.weight(1f))
            }
            PracticeHubRow(Lucide.ChartNoAxesColumn, "View practice data", "Statistics, backups & a fresh start") {
                onOpenPracticeData()
                dismissAnimated()
            }
            Text(
                "Your setups and practice data stay on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacingSmall),
            )
        }
    }
}

@Composable
private fun PracticeHubRow(
    icon: ImageVector,
    title: String,
    caption: String,
    value: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .heightIn(min = minimumTouchTargetSize)
            .clip(RoundedCornerShape(cornerRadiusLarge))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .pressableSurface(onClick)
            .padding(spacingMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacingMedium),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
            Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        value?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
        Icon(Lucide.ArrowRight, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PracticeHubStatistic(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview
@Composable
private fun PracticeHubSheetPreview() {
    MaterialTheme {
        PracticeHubSheet(4, 2, 15, 720_000L, 3, 8_400_000L, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview
@Composable
private fun PracticeHubRowPreview() {
    MaterialTheme {
        PracticeHubRow(Lucide.TrendingUp, "Tempo trainer", "Build speed, one bar at a time", onClick = {})
    }
}

@Preview
@Composable
private fun PracticeHubStatisticPreview() {
    MaterialTheme {
        PracticeHubStatistic("Today", "12 min")
    }
}
