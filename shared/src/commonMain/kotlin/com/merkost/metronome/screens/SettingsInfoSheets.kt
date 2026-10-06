package com.merkost.metronome.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.merkost.metronome.ui.cornerRadiusXLarge
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
) {
    AppBottomSheet("Your practice", onDismiss) { dismissAnimated ->
        SettingsPracticeContent(todayTime, totalTime, practiceStreak, playing, onReset, dismissAnimated) {
            PracticeBackupControls()
        }
    }
}

@Composable
private fun SettingsPracticeContent(
    todayTime: Long,
    totalTime: Long,
    practiceStreak: Int,
    playing: Boolean,
    onReset: () -> Unit,
    onDone: () -> Unit,
    backupControls: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        Text("A quiet record of time spent practicing.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(shape = RoundedCornerShape(cornerRadiusXLarge), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingSmall), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Today", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(TimestampMillisecondsFormatter.formatHuman(todayTime), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Time counts while the metronome plays.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
            SettingsPracticeStatistic("Total practice", TimestampMillisecondsFormatter.formatHuman(totalTime), Modifier.weight(1f))
            SettingsPracticeStatistic("Practice streak", "$practiceStreak ${if (practiceStreak == 1) "day" else "days"}", Modifier.weight(1f))
        }
        Text("Practice time is saved when playback pauses. Practice statistics are stored on this device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        backupControls()
        Surface(shape = RoundedCornerShape(cornerRadiusLarge), color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                Text("Start a fresh count.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Clear practice statistics. Your presets and practice sets are kept.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onReset, enabled = !playing, modifier = Modifier.heightIn(min = minimumTouchTargetSize)) {
                    Text("Reset practice statistics", fontWeight = FontWeight.SemiBold)
                }
                if (playing) Text("Pause playback before resetting your statistics.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Button(onClick = onDone, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight)) {
            Text("Done", fontWeight = FontWeight.Bold)
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
    AppBottomSheet("Help & about", onDismiss) { dismissAnimated ->
        SettingsAboutContent(version, onGuide, onWhatsNew, onContact, onRate, onWebsite, onPrivacy, dismissAnimated)
    }
}

@Composable
private fun SettingsAboutContent(
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
        Column(Modifier.fillMaxWidth().padding(vertical = spacingMedium), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
            Image(painterResource(Res.drawable.ic_launcher), null, modifier = Modifier.size(72.dp).clip(RoundedCornerShape(cornerRadiusLarge)))
            Text("Metronome", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("Practice & Tempo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (version.isNotBlank()) Text("Version $version", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SettingsLink("Practice guide", "A few taps to find your rhythm", Lucide.BookOpen, onGuide)
        SettingsLink("What's new", "Fresh additions for your practice", Lucide.Sparkles, onWhatsNew)
        SettingsLink("Contact support", "Ask a question or share feedback", Lucide.Mail, onContact)
        SettingsLink("Rate the app", "Help someone else find their rhythm", Lucide.Star, onRate)
        SettingsLink("Metronome website", "Web app, support & more", Lucide.ExternalLink, onWebsite, external = true)
        SettingsLink("Privacy policy", "How your information is handled", Lucide.ShieldCheck, onPrivacy, external = true)
        Button(onClick = onDone, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight)) {
            Text("Done", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun SettingsGuideSheet(onDismiss: () -> Unit) {
    AppBottomSheet("Practice guide", onDismiss) { dismissAnimated -> SettingsGuideContent(dismissAnimated) }
}

@Composable
private fun SettingsGuideContent(onDone: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        SettingsGuideStep("Find your pace.", "Slide the tempo, tap a rhythm or enter an exact BPM. Press Play when you're ready.")
        SettingsGuideStep("Shape the bar.", "Tap a beat to cycle through normal, accent and mute. A filled dot plays the accent sound; the outer ring follows the active beat.")
        SettingsGuideStep("Keep a good setup.", "Save a preset to return to a tempo and rhythm. Arrange presets into a practice set with time or bar goals.")
        SettingsGuideStep("Build your rhythm.", "Use the timer for a focused block, Tempo trainer to build speed and Gap trainer to practice through silence.")
        Button(onClick = onDone, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight)) {
            Text("Got it", fontWeight = FontWeight.Bold)
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
    MaterialTheme { SettingsPracticeContent(720_000L, 18_600_000L, 3, false, {}, {}, {}) }
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
    MaterialTheme { SettingsGuideStep("Find your pace.", "Slide the tempo, tap a rhythm or enter an exact BPM.") }
}
