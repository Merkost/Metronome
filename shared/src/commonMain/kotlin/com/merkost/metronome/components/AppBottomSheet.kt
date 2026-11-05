package com.merkost.metronome.components

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.common_close_named
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppPresentation
import com.merkost.metronome.navigation.LocalAppViewportSize
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.isAppMotionReduced
import com.merkost.metronome.ui.cornerRadiusXLarge
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.maxContentWidth
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.spacingSmall
import com.merkost.metronome.ui.spacingLarge
import com.merkost.metronome.ui.spacingMedium
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@Composable
fun AppBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.(dismissAnimated: () -> Unit) -> Unit,
) {
    if (LocalAppPresentation.current == AppPresentation.Desktop) {
        DesktopAppSheet(title, onDismiss, content)
    } else {
        AppBottomSheetUi(title, onDismiss, content = content)
    }
}

@Composable
private fun DesktopAppSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.(dismissAnimated: () -> Unit) -> Unit,
) {
    val viewport = LocalAppViewportSize.current
    val availableWidth = if (viewport.width.value.isFinite()) viewport.width else 720.dp
    val availableHeight = if (viewport.height.value.isFinite()) viewport.height else 784.dp
    val width = (availableWidth - horizontalPadding * 2).coerceIn(200.dp, maxContentWidth)
    val height = (availableHeight - spacingLarge * 2).coerceIn(200.dp, 720.dp)
    val reduced = isAppMotionReduced()
    val progress = remember { Animatable(if (reduced) 1f else 0f) }
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    var closing by remember { mutableStateOf(false) }
    val initialFocus = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { progress.animateTo(1f, AppAnimations.emphasized()) }
    val dismissAnimated: () -> Unit = {
        if (!closing) {
            closing = true
            scope.launch {
                progress.animateTo(0f, AppAnimations.quick())
                currentOnDismiss()
            }
        }
    }
    Dialog(onDismissRequest = dismissAnimated, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        LaunchedEffect(Unit) { if (!hasFocus) initialFocus.requestFocus() }
        Surface(
            modifier = Modifier.width(width).heightIn(max = height)
                .onKeyEvent {
                    if (it.key == Key.Escape && it.type == KeyEventType.KeyDown &&
                        !it.isAltPressed && !it.isCtrlPressed && !it.isMetaPressed && !it.isShiftPressed
                    ) {
                        dismissAnimated()
                        true
                    } else false
                }
                .onFocusChanged { hasFocus = it.hasFocus }
                .focusRequester(initialFocus).focusable()
                .semantics { paneTitle = title }.graphicsLayer {
                alpha = progress.value
                scaleX = if (reduced) 1f else 0.97f + progress.value * 0.03f
                scaleY = scaleX
            },
            shape = RoundedCornerShape(cornerRadiusXLarge),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = horizontalPadding, end = spacingSmall, top = spacingSmall, bottom = spacingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        title,
                        modifier = Modifier.weight(1f).semantics { heading() },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    AppIconButton(onClick = dismissAnimated, enabled = !closing, modifier = Modifier.size(minimumTouchTargetSize)) {
                        Icon(Lucide.X, stringResource(Res.string.common_close_named, title))
                    }
                }
                AppBottomSheetBody(
                    title = title,
                    onDismissAnimated = dismissAnimated,
                    modifier = Modifier.weight(1f, fill = false),
                    showTitle = false,
                    content = content,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppBottomSheetUi(
    title: String,
    onDismiss: () -> Unit,
    safeInsets: @Composable () -> WindowInsets = { WindowInsets.safeDrawing },
    content: @Composable ColumnScope.(dismissAnimated: () -> Unit) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismissAnimated: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }
    ModalBottomSheet(
        modifier = Modifier.appBottomSheetSafeArea(safeInsets()) {
            if (sheetState.hasExpandedState) sheetState.requireOffset() else 0f
        },
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = cornerRadiusXLarge, topEnd = cornerRadiusXLarge),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { safeInsets().only(WindowInsetsSides.Bottom) },
    ) {
        AppBottomSheetBody(title, dismissAnimated, modifier = Modifier.weight(1f, fill = false), content = content)
    }
}

internal fun Modifier.appBottomSheetSafeArea(insets: WindowInsets, rawOffset: () -> Float): Modifier =
    windowInsetsPadding(insets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
        .offset { IntOffset(0, -rawOffset().roundToInt().coerceAtMost(0)) }

@Composable
internal fun AppBottomSheetBody(
    title: String,
    onDismissAnimated: () -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    content: @Composable ColumnScope.(dismissAnimated: () -> Unit) -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier.widthIn(max = maxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = horizontalPadding, end = horizontalPadding, bottom = spacingLarge),
        ) {
            if (showTitle) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(spacingMedium))
            }
            content(onDismissAnimated)
        }
    }
}

@Preview
@Composable
private fun AppBottomSheetBodyPreview() {
    MaterialTheme { AppBottomSheetBody("Pick your click.", {}) { Text("Preview a sound to find your rhythm.") } }
}
