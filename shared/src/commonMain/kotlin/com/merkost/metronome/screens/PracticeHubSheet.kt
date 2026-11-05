package com.merkost.metronome.screens

import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ChevronLeft
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowLeft
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
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.duration_minutes
import metronome.shared.generated.resources.hub_gap_trainer
import metronome.shared.generated.resources.hub_practice_data
import metronome.shared.generated.resources.hub_practice_data_caption
import metronome.shared.generated.resources.hub_practice_sets
import metronome.shared.generated.resources.hub_practice_summary
import metronome.shared.generated.resources.hub_presets
import metronome.shared.generated.resources.hub_privacy
import metronome.shared.generated.resources.hub_saved_setups
import metronome.shared.generated.resources.hub_streak
import metronome.shared.generated.resources.hub_streak_days
import metronome.shared.generated.resources.hub_tempo_trainer
import metronome.shared.generated.resources.hub_timer
import metronome.shared.generated.resources.hub_title
import metronome.shared.generated.resources.hub_today
import metronome.shared.generated.resources.hub_total
import metronome.shared.generated.resources.hub_training_tools
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

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
    AppBottomSheet(title = stringResource(Res.string.hub_title), onDismiss = onDismiss) { dismissAnimated ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacingSmall),
        ) {
            Text(stringResource(Res.string.hub_saved_setups), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
            PracticeHubRow(Lucide.Bookmark, stringResource(Res.string.hub_presets), value = presetCount.toString()) {
                onOpenPresets()
                dismissAnimated()
            }
            PracticeHubRow(Lucide.Layers, stringResource(Res.string.hub_practice_sets), value = practiceSetCount.toString()) {
                onOpenPracticeSets()
                dismissAnimated()
            }
            Text(stringResource(Res.string.hub_training_tools), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = spacingMedium))
            PracticeHubRow(Lucide.Timer, stringResource(Res.string.hub_timer), value = stringResource(Res.string.duration_minutes, timerMinutes)) {
                onOpenTimer()
                dismissAnimated()
            }
            PracticeHubRow(Lucide.TrendingUp, stringResource(Res.string.hub_tempo_trainer)) {
                onOpenTempoTrainer()
                dismissAnimated()
            }
            PracticeHubRow(Lucide.VolumeX, stringResource(Res.string.hub_gap_trainer)) {
                onOpenGapTrainer()
                dismissAnimated()
            }
            HorizontalDivider(modifier = Modifier.padding(top = spacingMedium))
            Text(stringResource(Res.string.hub_practice_summary), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = spacingMedium))
            if (LocalDensity.current.fontScale >= 1.3f) {
                Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    PracticeHubStatistic(stringResource(Res.string.hub_today), TimestampMillisecondsFormatter.formatHuman(todayPracticeTime))
                    PracticeHubStatistic(stringResource(Res.string.hub_streak), pluralStringResource(Res.plurals.hub_streak_days, practiceStreak, practiceStreak))
                    PracticeHubStatistic(stringResource(Res.string.hub_total), TimestampMillisecondsFormatter.formatHuman(totalPracticeTime))
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = spacingSmall),
                    horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    PracticeHubStatistic(stringResource(Res.string.hub_today), TimestampMillisecondsFormatter.formatHuman(todayPracticeTime), Modifier.weight(1f))
                    PracticeHubStatistic(stringResource(Res.string.hub_streak), pluralStringResource(Res.plurals.hub_streak_days, practiceStreak, practiceStreak), Modifier.weight(1f))
                    PracticeHubStatistic(stringResource(Res.string.hub_total), TimestampMillisecondsFormatter.formatHuman(totalPracticeTime), Modifier.weight(1f))
                }
            }
            PracticeHubRow(Lucide.ChartNoAxesColumn, stringResource(Res.string.hub_practice_data), stringResource(Res.string.hub_practice_data_caption)) {
                onOpenPracticeData()
                dismissAnimated()
            }
            Text(
                stringResource(Res.string.hub_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacingSmall),
            )
        }
    }
}

@Composable
internal fun PracticeHubRow(
    icon: ImageVector,
    title: String,
    caption: String? = null,
    value: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .heightIn(min = minimumTouchTargetSize)
            .clip(RoundedCornerShape(cornerRadiusLarge))
            .pressableSurface(onClick)
            .padding(vertical = spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacingMedium),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
            caption?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        value?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Icon(if (LocalLayoutDirection.current == LayoutDirection.Rtl) Lucide.ChevronLeft else Lucide.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
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
        PracticeHubStatistic(stringResource(Res.string.hub_today), "12 min")
    }
}
