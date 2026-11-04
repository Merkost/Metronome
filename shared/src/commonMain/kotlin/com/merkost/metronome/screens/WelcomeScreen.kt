package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Headphones
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.Timer
import com.merkost.metronome.components.MetronomeMark
import com.merkost.metronome.components.OnboardingIllustration
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.PressedScaleSurface
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.maxContentWidth
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.pressScale
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    isSaving: Boolean,
    error: String?,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accessibilityLayout = LocalDensity.current.fontScale > 1.4f
    var completionPending by remember { mutableStateOf(false) }
    var lastError by remember { mutableStateOf(error) }
    val busy = isSaving || completionPending
    LaunchedEffect(error, isSaving) {
        if (error != null) {
            lastError = error
            if (!isSaving) completionPending = false
        }
    }
    val complete: (() -> Unit) -> Unit = { action ->
        if (!isSaving && !completionPending) {
            completionPending = true
            action()
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Box(
                        Modifier.size(minimumTouchTargetSize).testTag("welcomeBrandMark").semantics {
                            if (accessibilityLayout) {
                                contentDescription = "Metronome"
                                heading()
                            }
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        MetronomeMark(Modifier.size(28.dp), MaterialTheme.colorScheme.primary)
                    }
                },
                title = {
                    if (!accessibilityLayout) {
                        Text(
                            "Metronome",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.testTag("welcomeWordmark").semantics { heading() },
                        )
                    }
                },
                actions = { TextButton(onClick = { complete(onSkip) }, enabled = !busy) { Text("Skip") } },
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    Modifier.widthIn(max = maxContentWidth).fillMaxWidth().navigationBarsPadding()
                        .padding(horizontal = horizontalPadding, vertical = spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    AnimatedVisibility(error != null, enter = AppAnimations.expandEnter, exit = AppAnimations.shrinkExit) {
                        Text(
                            (error ?: lastError).orEmpty(), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                    WelcomeAction(
                        label = if (busy) "Getting ready…" else "Get started",
                        busy = busy,
                        onClick = { complete(onComplete) },
                    )
                }
            }
        },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.Center) {
            BoxWithConstraints(Modifier.widthIn(max = maxContentWidth).fillMaxSize()) {
                val heroHeight = minOf(210.dp, maxHeight * if (accessibilityLayout) 0.22f else 0.30f)
                Column(
                    Modifier.fillMaxSize().testTag("welcomeContent").verticalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPadding, vertical = spacingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacingMedium, Alignment.CenterVertically),
                ) {
                    OnboardingIllustration(Modifier.fillMaxWidth().height(heroHeight).testTag("welcomeGraphic"))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                        Text(
                            "Find your rhythm.",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().semantics { heading() },
                        )
                        Text(
                            "A steady beat, at your pace.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                        WelcomeFeature(Lucide.SlidersHorizontal, "Your pace", "Tap a tempo. Find your flow.", "pace")
                        WelcomeFeature(Lucide.Headphones, "Your sound", "Clicks, accents and subdivisions.", "sound")
                        WelcomeFeature(Lucide.Timer, "Your practice", "Timers, trainers and saved routines.", "practice")
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeFeature(icon: ImageVector, title: String, description: String, tag: String) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = minimumTouchTargetSize).testTag("welcomeFeature-$tag")
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacingMedium),
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WelcomeAction(label: String, busy: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val textWidth = with(density) { (maxWidth - spacingMedium * 2 - spacingSmall - 24.dp).roundToPx().coerceAtLeast(1) }
        val textHeight = with(density) {
            listOf("Get started", "Getting ready…").maxOf {
                measurer.measure(AnnotatedString(it), style, constraints = Constraints(maxWidth = textWidth)).size.height
            }.toDp()
        }
        Button(
            onClick = onClick, enabled = !busy, interactionSource = interaction, shape = CircleShape,
            contentPadding = PaddingValues(horizontal = spacingMedium, vertical = spacingSmall),
            modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp).testTag("welcomeAction").pressScale(interaction, PressedScaleSurface),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
                AnimatedContent(label, modifier = Modifier.weight(1f).height(textHeight), transitionSpec = { AppAnimations.fadeThroughFixedSize }, label = "welcomeActionLabel") {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                        Text(it, style = style)
                    }
                }
                Box(Modifier.size(24.dp).testTag("welcomeActionIndicator"), contentAlignment = Alignment.Center) {
                    if (busy) CircularProgressIndicator(Modifier.size(20.dp), color = LocalContentColor.current, strokeWidth = 2.dp)
                    else Icon(if (LocalLayoutDirection.current == LayoutDirection.Rtl) Lucide.ArrowLeft else Lucide.ArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    MaterialTheme { WelcomeScreen(false, null, {}, {}) }
}
