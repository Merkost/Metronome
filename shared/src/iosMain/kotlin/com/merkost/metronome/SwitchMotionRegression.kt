package com.merkost.metronome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.merkost.metronome.screens.SettingsSwitch
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.cornerRadiusXLarge
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.ui.theme.Typography
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import platform.Foundation.NSProcessInfo

private const val RegressionDurationMillis = 32_000L
private val RegressionViewportHeight = 550.dp

private data class RegressionPhase(
    val name: String,
    val flowVisible: Boolean,
    val beatVisible: Boolean,
    val alpha: Float = 1f,
    val clipFraction: Float = 1f,
    val movement: Float = 0f,
    val scrimVisible: Boolean = false,
    val sheetVisible: Boolean = false,
)

private fun regressionPhase(elapsedMillis: Long): RegressionPhase {
    val bothVisible = elapsedMillis >= 6_000L && elapsedMillis !in 6_160L..<6_320L
    val flowVisible = elapsedMillis in 2_000L..<4_000L || bothVisible
    val base = RegressionPhase("closed", flowVisible, bothVisible)
    fun progress(start: Long): Float = ((elapsedMillis - start) / 2_000f).coerceIn(0f, 1f)
    return when {
        elapsedMillis < 2_000L -> base
        elapsedMillis < 4_000L -> base.copy(name = "flow-enter")
        elapsedMillis < 6_000L -> base.copy(name = "flow-exit")
        elapsedMillis < 6_160L -> base.copy(name = "both-enter")
        elapsedMillis < 6_320L -> base.copy(name = "reverse-exit")
        elapsedMillis < 8_000L -> base.copy(name = "reverse-enter")
        elapsedMillis < 10_000L -> base.copy(name = "both-visible")
        elapsedMillis < 12_000L -> base.copy(name = "ancestor-fade-out", alpha = 1f - progress(10_000L))
        elapsedMillis < 14_000L -> base.copy(name = "ancestor-invisible", alpha = 0f)
        elapsedMillis < 16_000L -> base.copy(name = "ancestor-fade-in", alpha = progress(14_000L))
        elapsedMillis < 18_000L -> base.copy(name = "ancestor-clip-out", clipFraction = 1f - progress(16_000L))
        elapsedMillis < 20_000L -> base.copy(name = "ancestor-clipped", clipFraction = 0f)
        elapsedMillis < 22_000L -> base.copy(name = "ancestor-clip-in", clipFraction = progress(20_000L))
        elapsedMillis < 24_000L -> base.copy(name = "ancestor-move", movement = progress(22_000L))
        elapsedMillis < 26_000L -> base.copy(name = "ancestor-return", movement = 1f - progress(24_000L))
        elapsedMillis < 28_000L -> base.copy(name = "scrim", scrimVisible = true)
        elapsedMillis < 30_000L -> base.copy(name = "sheet-scrim", scrimVisible = true, sheetVisible = true)
        elapsedMillis < RegressionDurationMillis -> base.copy(name = "restored")
        else -> base.copy(name = "complete")
    }
}

@Composable
internal fun SwitchMotionRegressionScreen(freezeAtMillis: Long? = null) {
    val arguments = remember { NSProcessInfo.processInfo.arguments.filterIsInstance<String>() }
    val frozenMillis = remember(freezeAtMillis) {
        freezeAtMillis ?: arguments.firstOrNull { it.startsWith("--switch-motion-regression-freeze-ms=") }
            ?.substringAfter('=')?.toLongOrNull()?.coerceIn(0L, RegressionDurationMillis)
    }
    val offsetMillis = remember {
        arguments.firstOrNull { it.startsWith("--switch-motion-regression-offset-ms=") }
            ?.substringAfter('=')?.toLongOrNull()?.coerceIn(0L, RegressionDurationMillis) ?: 0L
    }
    var elapsedMillis by remember { mutableLongStateOf(frozenMillis ?: offsetMillis) }
    var countIn by remember { mutableStateOf(true) }
    var keepAwake by remember { mutableStateOf(false) }
    var backgroundPlayback by remember { mutableStateOf(true) }
    var liveActivity by remember { mutableStateOf(false) }
    var beatFlash by remember { mutableStateOf(true) }
    var sheetControl by remember { mutableStateOf(true) }
    val geometryReported = remember { booleanArrayOf(false) }
    val phase = regressionPhase(elapsedMillis)
    val palette = AppColorScheme.MINT_GREEN.lightColor

    LaunchedEffect(frozenMillis, offsetMillis) {
        if (frozenMillis != null) return@LaunchedEffect
        val started = TimeSource.Monotonic.markNow()
        while (elapsedMillis < RegressionDurationMillis) {
            elapsedMillis = (offsetMillis + started.elapsedNow().inWholeMilliseconds)
                .coerceAtMost(RegressionDurationMillis)
            delay(16L)
        }
    }
    LaunchedEffect(Unit) {
        println("switch-motion-regression {\"kind\":\"fixture\",\"durationMillis\":$RegressionDurationMillis,\"viewportHeightDp\":550,\"primaryArgb\":\"${palette.primary.toArgb().toUInt().toString(16)}\",\"surfaceArgb\":\"${palette.surface.toArgb().toUInt().toString(16)}\",\"cardArgb\":\"${palette.surfaceContainerLow.toArgb().toUInt().toString(16)}\"}")
    }
    LaunchedEffect(phase.name) {
        println("switch-motion-regression {\"kind\":\"phase\",\"name\":\"${phase.name}\",\"elapsedMillis\":$elapsedMillis,\"alpha\":${phase.alpha},\"clipFraction\":${phase.clipFraction},\"movement\":${phase.movement},\"flowVisible\":${phase.flowVisible},\"beatVisible\":${phase.beatVisible}}")
    }

    MaterialTheme(colorScheme = palette, typography = Typography) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).safeDrawingPadding()) {
            Column(Modifier.fillMaxSize().padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                Text("Switch motion regression", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${phase.name} · ${elapsedMillis}ms\nalpha=${(phase.alpha * 100).toInt()}% clip=${(phase.clipFraction * 100).toInt()}% y=${(phase.movement * 80).toInt()}dp",
                    modifier = Modifier.height(40.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    Modifier.fillMaxWidth().height(RegressionViewportHeight * phase.clipFraction)
                        .onGloballyPositioned { coordinates ->
                            if (!geometryReported[0]) {
                                geometryReported[0] = true
                                val bounds = coordinates.boundsInWindow()
                                println("switch-motion-regression {\"kind\":\"viewport\",\"leftPx\":${bounds.left},\"topPx\":${bounds.top},\"rightPx\":${bounds.right},\"bottomPx\":${bounds.bottom}}")
                            }
                        }
                        .clipToBounds().graphicsLayer {
                            alpha = phase.alpha
                            translationY = 80.dp.toPx() * phase.movement
                        }
                ) {
                    Column(
                        Modifier.fillMaxWidth().wrapContentHeight(Alignment.Top, unbounded = true)
                            .requiredHeight(RegressionViewportHeight),
                        verticalArrangement = Arrangement.spacedBy(spacingMedium),
                    ) {
                        RegressionSettingsGroup("Keep your flow.", "Practice & playback", phase.flowVisible) {
                            SettingsSwitch("Count-in", countIn, { countIn = it }, "One bar to ease you in")
                            SettingsSwitch("Keep screen awake", keepAwake, { keepAwake = it }, "While the beat is playing")
                            SettingsSwitch("Background playback", backgroundPlayback, { backgroundPlayback = it }, "Keep going when you leave the app")
                            SettingsSwitch("Live Activity", liveActivity, { liveActivity = it }, "Tempo and timer on the Lock Screen")
                        }
                        RegressionSettingsGroup("See the beat.", "Beat display & motion", phase.beatVisible) {
                            SettingsSwitch("Beat flash", beatFlash, { beatFlash = it }, "A little pulse while you play")
                        }
                    }
                }
            }
            if (phase.scrimVisible) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)))
            }
            if (phase.sheetVisible) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .padding(horizontalPadding).height(320.dp),
                    shape = RoundedCornerShape(cornerRadiusXLarge),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
                        Text("Sheet over settings", style = MaterialTheme.typography.titleMedium)
                        SettingsSwitch("Sheet control", sheetControl, { sheetControl = it }, "The backdrop stays beneath this sheet")
                    }
                }
            }
        }
    }
}

@Composable
private fun RegressionSettingsGroup(
    title: String,
    subtitle: String,
    visible: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(shape = RoundedCornerShape(cornerRadiusXLarge), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column {
            Column(Modifier.fillMaxWidth().padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AnimatedVisibility(visible, enter = AppAnimations.expandEnter, exit = AppAnimations.shrinkExit) {
                Column(
                    Modifier.padding(start = horizontalPadding, end = horizontalPadding, bottom = horizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(spacingMedium),
                    content = content,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SwitchMotionRegressionPreview() {
    SwitchMotionRegressionScreen(freezeAtMillis = 8_000L)
}

@Preview
@Composable
private fun RegressionSettingsGroupPreview() {
    MaterialTheme(colorScheme = AppColorScheme.MINT_GREEN.lightColor, typography = Typography) {
        RegressionSettingsGroup("Keep your flow.", "Practice & playback", true) {
            SettingsSwitch("Count-in", true, {}, "One bar to ease you in")
        }
    }
}
