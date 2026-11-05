package com.merkost.metronome.screens

import metronome.shared.generated.resources.about_contact
import metronome.shared.generated.resources.about_contact_description
import metronome.shared.generated.resources.about_guide
import metronome.shared.generated.resources.about_guide_description
import metronome.shared.generated.resources.about_privacy
import metronome.shared.generated.resources.about_privacy_description
import metronome.shared.generated.resources.about_rate
import metronome.shared.generated.resources.about_rate_description
import metronome.shared.generated.resources.about_version
import metronome.shared.generated.resources.about_website
import metronome.shared.generated.resources.about_website_description
import metronome.shared.generated.resources.common_got_it
import metronome.shared.generated.resources.done
import metronome.shared.generated.resources.info_rhythm_body
import metronome.shared.generated.resources.info_rhythm_title
import metronome.shared.generated.resources.info_saved_body
import metronome.shared.generated.resources.info_saved_title
import metronome.shared.generated.resources.info_tempo_body
import metronome.shared.generated.resources.info_tempo_title
import metronome.shared.generated.resources.info_training_body
import metronome.shared.generated.resources.info_training_title
import metronome.shared.generated.resources.settings_help_about
import metronome.shared.generated.resources.settings_latest_updates
import metronome.shared.generated.resources.settings_practice_data
import metronome.shared.generated.resources.settings_practice_streak
import metronome.shared.generated.resources.settings_reset_description
import metronome.shared.generated.resources.settings_reset_pause_hint
import metronome.shared.generated.resources.settings_reset_practice_statistics
import metronome.shared.generated.resources.settings_reset_statistics
import metronome.shared.generated.resources.settings_statistics
import metronome.shared.generated.resources.settings_statistics_note
import metronome.shared.generated.resources.settings_streak_days
import metronome.shared.generated.resources.settings_today
import metronome.shared.generated.resources.settings_total_practice
import metronome.shared.generated.resources.settings_week_empty
import metronome.shared.generated.resources.whats_new_title
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import com.merkost.metronome.components.WeekStrip
import com.merkost.metronome.model.PracticeWeek
import com.merkost.metronome.model.asString
import com.merkost.metronome.model.weekSentence
import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.ExternalLink
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Star
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.PracticeBackupControls
import com.merkost.metronome.components.TimestampMillisecondsFormatter
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.sheetButtonHeight
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.ic_launcher
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SettingsPracticeSheet(
    todayTime: Long,
    totalTime: Long,
    practiceStreak: Int,
    playing: Boolean,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    week: PracticeWeek? = null,
) {
    AppBottomSheet(stringResource(Res.string.settings_practice_data), onDismiss) { dismissAnimated ->
        SettingsPracticeContent(todayTime, totalTime, practiceStreak, playing, onReset, dismissAnimated, week = week) {
            PracticeBackupControls()
        }
    }
}

@Composable
internal fun SettingsPracticeContent(
    todayTime: Long,
    totalTime: Long,
    practiceStreak: Int,
    playing: Boolean,
    onReset: () -> Unit,
    onDone: () -> Unit,
    week: PracticeWeek? = null,
    backupControls: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        Text(stringResource(Res.string.settings_statistics), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (week != null) {
            Surface(shape = RoundedCornerShape(cornerRadiusLarge), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    Text(weekSentence(week)?.asString() ?: stringResource(Res.string.settings_week_empty), style = MaterialTheme.typography.bodyMedium)
                    WeekStrip(week)
                }
            }
        }
        Surface(shape = RoundedCornerShape(cornerRadiusLarge), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                Text(stringResource(Res.string.settings_today), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(TimestampMillisecondsFormatter.formatHuman(todayTime), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
            SettingsPracticeStatistic(stringResource(Res.string.settings_total_practice), TimestampMillisecondsFormatter.formatHuman(totalTime), Modifier.weight(1f))
            SettingsPracticeStatistic(stringResource(Res.string.settings_practice_streak), pluralStringResource(Res.plurals.settings_streak_days, practiceStreak, practiceStreak), Modifier.weight(1f))
        }
        Text(stringResource(Res.string.settings_statistics_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        backupControls()
        Surface(shape = RoundedCornerShape(cornerRadiusLarge), color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                Text(stringResource(Res.string.settings_reset_statistics), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(Res.string.settings_reset_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onReset, enabled = !playing, modifier = Modifier.heightIn(min = minimumTouchTargetSize)) {
                    Text(stringResource(Res.string.settings_reset_practice_statistics), fontWeight = FontWeight.SemiBold)
                }
                if (playing) Text(stringResource(Res.string.settings_reset_pause_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Button(onClick = onDone, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight)) {
            Text(stringResource(Res.string.done), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsPracticeStatistic(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(cornerRadiusLarge), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = modifier) {
        Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun SettingsAboutSheet(
    version: String,
    onGuide: () -> Unit,
    onWhatsNew: () -> Unit,
    onContact: () -> Unit,
    onRate: () -> Unit,
    onWebsite: () -> Unit,
    onPrivacy: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(stringResource(Res.string.settings_help_about), onDismiss) { dismissAnimated ->
        SettingsAboutContent(version, onGuide, onWhatsNew, onContact, onRate, onWebsite, onPrivacy, dismissAnimated)
    }
}

@Composable
internal fun SettingsAboutContent(
    version: String,
    onGuide: () -> Unit,
    onWhatsNew: () -> Unit,
    onContact: () -> Unit,
    onRate: () -> Unit,
    onWebsite: () -> Unit,
    onPrivacy: () -> Unit,
    onDone: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Row(Modifier.fillMaxWidth().padding(vertical = spacingSmall), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacingMedium)) {
            Image(painterResource(Res.drawable.ic_launcher), null, modifier = Modifier.size(minimumTouchTargetSize).clip(RoundedCornerShape(cornerRadiusLarge)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
                Text("Metronome", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (version.isNotBlank()) Text(stringResource(Res.string.about_version, version), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        SettingsLink(stringResource(Res.string.about_guide), stringResource(Res.string.about_guide_description), Lucide.BookOpen, onGuide)
        SettingsLink(stringResource(Res.string.whats_new_title), stringResource(Res.string.settings_latest_updates), Lucide.Sparkles, onWhatsNew)
        SettingsLink(stringResource(Res.string.about_contact), stringResource(Res.string.about_contact_description), Lucide.Mail, onContact)
        SettingsLink(stringResource(Res.string.about_rate), stringResource(Res.string.about_rate_description), Lucide.Star, onRate)
        SettingsLink(stringResource(Res.string.about_website), stringResource(Res.string.about_website_description), Lucide.ExternalLink, onWebsite, external = true)
        SettingsLink(stringResource(Res.string.about_privacy), stringResource(Res.string.about_privacy_description), Lucide.ShieldCheck, onPrivacy, external = true)
        Button(onClick = onDone, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight)) {
            Text(stringResource(Res.string.done), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun SettingsGuideSheet(onDismiss: () -> Unit) {
    AppBottomSheet(stringResource(Res.string.about_guide), onDismiss) { dismissAnimated -> SettingsGuideContent(dismissAnimated) }
}

@Composable
private fun SettingsGuideContent(onDone: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        SettingsGuideStep(stringResource(Res.string.info_tempo_title), stringResource(Res.string.info_tempo_body))
        SettingsGuideStep(stringResource(Res.string.info_rhythm_title), stringResource(Res.string.info_rhythm_body))
        SettingsGuideStep(stringResource(Res.string.info_saved_title), stringResource(Res.string.info_saved_body))
        SettingsGuideStep(stringResource(Res.string.info_training_title), stringResource(Res.string.info_training_body))
        Button(onClick = onDone, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight)) {
            Text(stringResource(Res.string.common_got_it), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsGuideStep(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview
@Composable
private fun SettingsPracticePreview() {
    MaterialTheme { SettingsPracticeContent(720_000L, 18_600_000L, 3, false, {}, {}) {} }
}

@Preview
@Composable
private fun SettingsPracticeStatisticPreview() {
    MaterialTheme { SettingsPracticeStatistic("Total practice", "5 h 10 min") }
}

@Preview
@Composable
private fun SettingsAboutPreview() {
    MaterialTheme { SettingsAboutContent("1.4.0", {}, {}, {}, {}, {}, {}, {}) }
}

@Preview
@Composable
private fun SettingsGuidePreview() {
    MaterialTheme { SettingsGuideContent({}) }
}

@Preview
@Composable
private fun SettingsGuideStepPreview() {
    MaterialTheme { SettingsGuideStep("Tempo", "Slide the tempo, tap a rhythm or enter an exact BPM.") }
}
