package com.merkost.metronome.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.merkost.metronome.model.PracticeDay
import com.merkost.metronome.model.PracticeWeek
import com.merkost.metronome.model.asString
import com.merkost.metronome.model.weekStripDescription
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.spacingSmall
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.week_day_friday
import metronome.shared.generated.resources.week_day_monday
import metronome.shared.generated.resources.week_day_saturday
import metronome.shared.generated.resources.week_day_sunday
import metronome.shared.generated.resources.week_day_thursday
import metronome.shared.generated.resources.week_day_tuesday
import metronome.shared.generated.resources.week_day_wednesday
import org.jetbrains.compose.resources.stringResource

@Composable
fun WeekStrip(week: PracticeWeek, modifier: Modifier = Modifier) {
    val description = weekStripDescription(week).asString()
    val dayLetters = listOf(
        stringResource(Res.string.week_day_monday),
        stringResource(Res.string.week_day_tuesday),
        stringResource(Res.string.week_day_wednesday),
        stringResource(Res.string.week_day_thursday),
        stringResource(Res.string.week_day_friday),
        stringResource(Res.string.week_day_saturday),
        stringResource(Res.string.week_day_sunday),
    )
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        week.days.forEachIndexed { index, day -> WeekDot(dayLetters[index], day) }
    }
}

@Composable
private fun WeekDot(letter: String, day: PracticeDay) {
    val colors = MaterialTheme.colorScheme
    val fill by animateColorAsState(
        targetValue = if (day.practiced) colors.onSurface else colors.surfaceContainerHigh,
        animationSpec = AppAnimations.standard(),
        label = "weekDotFill",
    )
    val ink = if (day.practiced) colors.surface else if (day.isFuture) colors.onSurfaceVariant.copy(alpha = 0.6f) else colors.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(fill)
            .then(if (day.isToday && !day.practiced) Modifier.border(1.5.dp, colors.onSurface, CircleShape) else Modifier)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(letter, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = ink)
    }
}
