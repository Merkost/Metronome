package com.merkost.metronome.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSSelectorFromString
import platform.MediaPlayer.MPVolumeView
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UISwitch
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VolumeSlider() {
    val sliderTint = MaterialTheme.colorScheme.primary

    SettingsRow(title = "Volume") {
        UIKitView(
            factory = {
                MPVolumeView().apply {
                    setShowsRouteButton(false)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            update = { view ->
                view.tintColor = sliderTint.toUIColor()
            }
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val onTint = MaterialTheme.colorScheme.primary
    val latestOnChange = rememberUpdatedState(onCheckedChange)
    val switchTarget = remember { SwitchTarget { latestOnChange.value(it) } }

    UIKitView(
        factory = {
            UISwitch().apply {
                addTarget(
                    target = switchTarget,
                    action = NSSelectorFromString("onValueChanged:"),
                    forControlEvents = UIControlEventValueChanged
                )
            }
        },
        modifier = Modifier.size(width = 51.dp, height = 31.dp),
        update = { view ->
            view.setOn(checked, animated = false)
            view.onTintColor = onTint.toUIColor()
        }
    )
}

@Composable
actual fun BackgroundPlayPermissionCheck(backgroundPlayEnabled: Boolean) {
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class SwitchTarget(
    private val onChange: (Boolean) -> Unit
) : NSObject() {
    @ObjCAction
    fun onValueChanged(sender: UISwitch) {
        onChange(sender.on)
    }
}

private fun Color.toUIColor(): UIColor = UIColor(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble()
)
