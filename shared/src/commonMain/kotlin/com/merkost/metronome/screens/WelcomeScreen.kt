package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Headphones
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.merkost.metronome.components.MetronomeMark
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.appearIn
import com.merkost.metronome.ui.cornerRadiusMedium
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.maxContentWidth
import com.merkost.metronome.ui.isAppMotionReduced
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.pressScale
import com.merkost.metronome.ui.PressedScaleSurface
import com.merkost.metronome.ui.spacingLarge
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

private val startingPaces = listOf("Easy" to 60, "Steady" to 80, "Brisk" to 120)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    bpm: Int,
    selectedSound: ClickSound,
    isListening: Boolean,
    isSaving: Boolean,
    error: String?,
    onListen: () -> Unit,
    onPaceSelected: (Int) -> Unit,
    onChooseSound: () -> Unit,
    onStart: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startInteraction = remember { MutableInteractionSource() }
    val skipInteraction = remember { MutableInteractionSource() }
    var lastError by remember { mutableStateOf(error) }
    LaunchedEffect(error) { error?.let { lastError = it } }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MetronomeMark(Modifier.size(28.dp), MaterialTheme.colorScheme.primary)
                        Text("Metronome", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSkip,
                        enabled = !isSaving,
                        interactionSource = skipInteraction,
                        modifier = Modifier.pressScale(skipInteraction, PressedScaleSurface),
                    ) {
                        Text("Skip intro")
                    }
                },
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .widthIn(max = maxContentWidth)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = horizontalPadding, vertical = spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    AnimatedVisibility(
                        visible = error != null,
                        enter = AppAnimations.expandEnter,
                        exit = AppAnimations.shrinkExit,
                    ) {
                        Text(
                            text = (error ?: lastError).orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                    Button(
                        onClick = onStart,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp).pressScale(startInteraction, PressedScaleSurface),
                        interactionSource = startInteraction,
                        shape = CircleShape,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                        ) {
                            Text(
                                text = if (isSaving) "Getting ready…" else "Start practicing",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1f),
                            )
                            AnimatedContent(
                                targetState = isSaving,
                                transitionSpec = { AppAnimations.fadeScaleTransform },
                                label = "welcomeStart",
                            ) { saving ->
                                if (saving) {
                                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Lucide.ArrowRight, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = maxContentWidth)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = spacingMedium),
                verticalArrangement = Arrangement.spacedBy(spacingMedium),
            ) {
                Column(
                    modifier = Modifier.appearIn(),
                    verticalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    Text(
                        "Hello, rhythm.",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        "A good practice starts with a little pulse.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.widthIn(max = 280.dp),
                    )
                }
                WelcomeListenMark(
                    bpm = bpm,
                    isListening = isListening,
                    enabled = !isSaving,
                    onClick = onListen,
                    modifier = Modifier.align(Alignment.CenterHorizontally).appearIn(delayMillis = 60),
                )
                Column(
                    modifier = Modifier.fillMaxWidth().appearIn(delayMillis = 100),
                    verticalArrangement = Arrangement.spacedBy(spacingMedium),
                ) {
                    Text(
                        "Pick a starting pace.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    WelcomePaceSelector(bpm, !isSaving, onPaceSelected)
                    Text(
                        "Fine-tune everything when you’re in.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minimumTouchTargetSize)
                        .clip(RoundedCornerShape(cornerRadiusMedium))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .pressableSurface(onClick = onChooseSound, enabled = !isSaving)
                        .padding(spacingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    Icon(Lucide.Headphones, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("Pick your click", style = MaterialTheme.typography.labelLarge)
                        Text(
                            selectedSound.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Lucide.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(spacingSmall))
            }
        }
    }
}

@Composable
private fun WelcomeListenMark(
    bpm: Int,
    isListening: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionReduced = isAppMotionReduced()
    var pulse by remember { mutableIntStateOf(0) }
    LaunchedEffect(isListening, bpm, motionReduced) {
        pulse = 0
        if (isListening && !motionReduced) {
            while (true) {
                pulse++
                delay(60_000L / bpm.coerceAtLeast(1))
            }
        }
    }
    val rotation by animateFloatAsState(
        targetValue = if (!isListening || motionReduced) 0f else if (pulse % 2 == 0) -6f else 6f,
        animationSpec = AppAnimations.expressive(),
        label = "welcomeMarkRotation",
    )
    val scale by animateFloatAsState(
        targetValue = if (isListening && !motionReduced) 1.045f else 1f,
        animationSpec = AppAnimations.expressive(),
        label = "welcomeMarkScale",
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacingMedium),
    ) {
        Box(
            modifier = Modifier
                .size(196.dp)
                .semantics { contentDescription = if (isListening) "Stop pace preview" else "Hear your starting pace, $bpm BPM" }
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                .pressableSurface(onClick = onClick, enabled = enabled, rippled = false),
        ) {
            MetronomeMark(
                Modifier.align(Alignment.Center).size(112.dp).graphicsLayer {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                },
                MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(minimumTouchTargetSize)
                    .border(5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.inverseSurface),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = isListening,
                    transitionSpec = { AppAnimations.fadeScaleTransform },
                    label = "welcomePreviewIcon",
                ) { listening ->
                    Icon(
                        if (listening) Lucide.Pause else Lucide.Play,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }
        }
        AnimatedContent(
            targetState = isListening,
            transitionSpec = { AppAnimations.fadeThrough },
            label = "welcomePreviewHint",
        ) { listening ->
            Text(
                if (listening) "That’s your pace." else "Tap to hear it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

@Composable
private fun WelcomePaceSelector(
    bpm: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val selectedIndex = startingPaces.indexOfFirst { it.second == bpm }.coerceAtLeast(0)
        val paceWidth = (maxWidth - spacingSmall * 2) / 3
        val paceHeight = 78.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
        val offset by animateDpAsState(
            targetValue = (paceWidth + spacingSmall) * selectedIndex,
            animationSpec = AppAnimations.emphasized(),
            label = "welcomeSelectedPace",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
            repeat(3) {
                Box(
                    Modifier.weight(1f).height(paceHeight)
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(cornerRadiusMedium)),
                )
            }
        }
        Box(
            Modifier.offset(x = offset).size(paceWidth, paceHeight)
                .background(MaterialTheme.colorScheme.tertiaryFixed, RoundedCornerShape(cornerRadiusMedium)),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
            startingPaces.forEach { (name, value) ->
                val chosen = bpm == value
                val foreground by animateColorAsState(
                    targetValue = if (chosen) MaterialTheme.colorScheme.onTertiaryFixed else MaterialTheme.colorScheme.onSurface,
                    animationSpec = AppAnimations.standard(),
                    label = "welcomePaceColor",
                )
                Column(
                    modifier = Modifier.weight(1f).heightIn(min = paceHeight)
                        .semantics {
                            selected = chosen
                            contentDescription = "$name, $value BPM"
                        }
                        .clip(RoundedCornerShape(cornerRadiusMedium))
                        .pressableSurface(onClick = { onSelect(value) }, enabled = enabled)
                        .padding(vertical = spacingMedium, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    Text(name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = foreground, textAlign = TextAlign.Center)
                    Text("$value BPM", style = MaterialTheme.typography.labelSmall, color = foreground, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(80, ClickSound.WOOD, false, false, null, {}, {}, {}, {}, {})
    }
}

@Preview
@Composable
private fun WelcomeScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        WelcomeScreen(120, ClickSound.CLICK, true, false, null, {}, {}, {}, {}, {})
    }
}

@Preview
@Composable
private fun WelcomeListenMarkPreview() {
    MaterialTheme {
        WelcomeListenMark(80, false, true, {})
    }
}

@Preview
@Composable
private fun WelcomePaceSelectorPreview() {
    MaterialTheme {
        WelcomePaceSelector(80, true, {})
    }
}
