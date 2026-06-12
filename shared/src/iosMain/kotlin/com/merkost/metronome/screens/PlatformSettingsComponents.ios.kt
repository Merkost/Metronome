package com.merkost.metronome.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.outputVolume
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSSelectorFromString
import platform.MediaPlayer.MPVolumeView
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UISlider
import platform.UIKit.UISwitch
import platform.UIKit.UIView
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun VolumeSlider() {
    val volumeBridge = remember { createVolumeBridge() }
    var currentVolume by remember {
        mutableFloatStateOf(AVAudioSession.sharedInstance().outputVolume)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    var volumeTouched by remember { mutableIntStateOf(0) }

    LaunchedEffect(isDragged, volumeTouched) {
        if (isDragged) return@LaunchedEffect
        delay(1500)
        while (true) {
            currentVolume = AVAudioSession.sharedInstance().outputVolume
            delay(500)
        }
    }

    SettingsRow(title = "Volume") {
        Text(
            text = "${(currentVolume * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            value = currentVolume,
            onValueChange = { value ->
                currentVolume = value.coerceIn(0f, 1f)
                volumeTouched += 1
                volumeBridge.setVolume(currentVolume)
            },
            valueRange = 0f..1f,
            interactionSource = interactionSource,
            colors = SliderDefaults.colors(
                inactiveTickColor = Color.Transparent
            )
        )
        UIKitView(
            factory = { volumeBridge },
            modifier = Modifier
                .size(1.dp),
            update = { view ->
                view.refreshVolumeBridge()
            },
            properties = hiddenNativeViewProperties()
        )
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun PlatformSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val onTint = MaterialTheme.colorScheme.primary
    val latestOnChange = rememberUpdatedState(onCheckedChange)

    UIKitView(
        factory = {
            NativeSwitchContainer { latestOnChange.value(it) }
        },
        modifier = Modifier.size(width = 64.dp, height = 40.dp),
        update = { view ->
            view.update(checked = checked, onTintColor = onTint.toUIColor())
        },
        properties = nativeSwitchProperties()
    )
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class NativeSwitchContainer(
    private val onChange: (Boolean) -> Unit
) : UIView(frame = CGRectMake(0.0, 0.0, 64.0, 40.0)) {
    private val control = UISwitch()
    private var lastModelValue: Boolean? = null
    private var pendingUserValue: Boolean? = null
    private var pendingBaselineValue: Boolean? = null
    private var pendingModelMoved = false
    private var pendingUpdateCount = 0

    init {
        clipsToBounds = false
        control.clipsToBounds = false
        control.sizeToFit()
        control.addTarget(
            target = this,
            action = NSSelectorFromString("onValueChanged:"),
            forControlEvents = UIControlEventValueChanged
        )
        addSubview(control)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        layoutControl()
    }

    fun update(checked: Boolean, onTintColor: UIColor) {
        if (control.onTintColor != onTintColor) {
            control.onTintColor = onTintColor
        }
        val pending = pendingUserValue

        if (pending != null) {
            pendingUpdateCount += 1
            if (checked != pendingBaselineValue) {
                pendingModelMoved = true
            }
            if (checked == pending && (pending != pendingBaselineValue || pendingModelMoved || pendingUpdateCount > 1)) {
                pendingUserValue = null
                pendingBaselineValue = null
                pendingModelMoved = false
                pendingUpdateCount = 0
            }
            lastModelValue = checked
            layoutControl()
            return
        }

        if (control.on != checked) {
            control.setOn(checked, animated = false)
        }
        lastModelValue = checked
        layoutControl()
    }

    @ObjCAction
    fun onValueChanged(sender: UISwitch) {
        pendingUserValue = sender.on
        pendingBaselineValue = lastModelValue
        pendingModelMoved = false
        pendingUpdateCount = 0
        onChange(sender.on)
    }

    private fun layoutControl() {
        control.sizeToFit()
        val width = bounds.useContents { size.width }
        val height = bounds.useContents { size.height }
        control.center = CGPointMake(width / 2.0, height / 2.0)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun nativeSwitchProperties() = UIKitInteropProperties(
    interactionMode = UIKitInteropInteractionMode.NonCooperative,
    isNativeAccessibilityEnabled = true,
    placedAsOverlay = true
)

@OptIn(ExperimentalComposeUiApi::class)
private fun hiddenNativeViewProperties() = UIKitInteropProperties(
    interactionMode = null,
    isNativeAccessibilityEnabled = false,
    placedAsOverlay = true
)

@Composable
actual fun BackgroundPlayPermissionCheck(backgroundPlayEnabled: Boolean) {
}

@Composable
actual fun LiveActivitySettingsRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SettingsSwitch(
        "Live Activity",
        checked,
        onCheckedChange,
        subtitle = "Tempo and timer on the Lock Screen",
    )
}

private fun createVolumeBridge() = MPVolumeView().apply {
    refreshVolumeBridge()
}

private fun MPVolumeView.refreshVolumeBridge() {
    alpha = 0.01
    clipsToBounds = true
    setShowsVolumeSlider(true)
    layoutIfNeeded()
}

private fun MPVolumeView.setVolume(value: Float) {
    refreshVolumeBridge()
    val slider = systemSlider() ?: return
    dispatch_after(
        dispatch_time(DISPATCH_TIME_NOW, 10_000_000L),
        dispatch_get_main_queue()
    ) {
        slider.setValue(value.coerceIn(0f, 1f), animated = false)
        slider.sendActionsForControlEvents(UIControlEventValueChanged)
    }
}

private fun MPVolumeView.systemSlider(): UISlider? =
    subviews.firstNotNullOfOrNull { it as? UISlider }

private fun Color.toUIColor(): UIColor = UIColor(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble()
)
