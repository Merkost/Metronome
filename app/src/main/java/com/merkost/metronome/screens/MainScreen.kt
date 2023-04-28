package com.merkost.metronome.screens

import Ball
import MetronomeBalls
import OutlinedCircle
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.merkost.metronome.MainActivity
import com.merkost.metronome.R
import com.merkost.metronome.components.MainButtonsRow
import com.merkost.metronome.components.MyIconButton
import com.merkost.metronome.components.MySecondaryTextButton
import com.merkost.metronome.components.checkNotificationPolicyAccess
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.StopWatchState
import com.merkost.metronome.ui.theme.MetronomeTheme
import com.merkost.metronome.viewModels.MetronomeViewModel
import org.koin.androidx.compose.get

val BallSize = 40.dp
val CircleSize = BallSize + 32.dp
val CircleWeight = 5.dp
val BallColor = Color.LightGray

val BallsCount = 4

val defaultPlayButtonSize = 85.dp
val horizontalPadding = 18.dp

val defaultIconButtonSize = 70.dp
val defaultSecondaryIconButtonSize = defaultIconButtonSize


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onSettingsClicked: () -> Unit) {
    val context = LocalContext.current
    val viewModel: MetronomeViewModel = get()

    val service = (context as MainActivity).metronomeService
    val colorFlash by viewModel.colorFlash.collectAsState()
    val colorScheme by viewModel.colorScheme.collectAsState()
    val metronomeState = remember(service?.metronomeState) {
       service?.metronomeState ?: MetronomeState()
    }
    val stopWatchState = remember(service?.stopWatch) { service?.stopWatch ?: StopWatchState() }
    val isPlaying = service?.isPlaying?.collectAsState()?.value ?: false
    val selectedIndex = remember(service?.index) { service?.index ?: 0 }

    val springSpec = SpringSpec<Float>(stiffness = 800f, dampingRatio = 0.8f)

    val boxColorAnimation = remember { Animatable(0f) }

    if (colorFlash) {
        LaunchedEffect(selectedIndex) {
            if (metronomeState.playing) {
                boxColorAnimation.animateTo(0.5f, springSpec)
                boxColorAnimation.animateTo(0f, springSpec)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        colorScheme.color,
                        alpha = boxColorAnimation.value.coerceIn(0f, 1f)
                    )
                }
                .padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "BeatMate",
                        style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClicked) {
                        Icon(Icons.Default.Settings, Icons.Default.Settings.name)
                    }
                }
            )

            MetronomeBalls(
                modifier = Modifier.padding(bottom = 64.dp),
                selectedIndex = selectedIndex.coerceIn(0..3),
                itemCount = BallsCount,
                animSpec = springSpec,
                indicator = { OutlinedCircle(selectedIndex == -1) },
            ) {
                (1..BallsCount).forEach {
                    val color by
                    animateColorAsState(
                        targetValue = if (it == 1 || selectedIndex.coerceIn(0..3) == it - 1) Color.Black else BallColor,
                        label = "ballsColor"
                    )
                    Ball(color = color)
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Allegro",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MyIconButton(
                        Icons.Default.Remove,
                        onClick = viewModel::onSliderValueDecreased
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = metronomeState.rhythm.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    )
                    MyIconButton(
                        Icons.Default.Add,
                        onClick = viewModel::onSliderValueIncreased
                    )
                }
            }
            Slider(
                modifier = Modifier.height(16.dp),
                value = metronomeState.rhythm.toFloat(),
                onValueChange = viewModel::onSliderValueChanged,
                valueRange = viewModel.metronomeRange,
                steps = viewModel.steps,
                colors = SliderDefaults.colors(
                    thumbColor = Color.Black, activeTrackColor = Color.Black,
                    inactiveTrackColor = Color.LightGray
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MySecondaryTextButton(text = "- 5", onClick = viewModel::onMinusFive)
                MySecondaryTextButton(text = "÷ 2", onClick = viewModel::divideByTwo)
                MySecondaryTextButton(text = "× 2", onClick = viewModel::multiplyByTwo)
                MySecondaryTextButton(text = "+ 5", onClick = viewModel::onPlusFive)
            }
            Spacer(modifier = Modifier.size(1.dp))
            Divider(modifier = Modifier.fillMaxWidth())
        }
        MainButtonsRow(
            Modifier.padding(horizontal = horizontalPadding),
            isPlaying = isPlaying,
            stopWatchState,
            {
                service?.let {
                    service.onPlayPauseClicked()
                }
            },
            onTempoTap = viewModel::onTempoTap
        )
    }


}


@Composable
//@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(
    showBackground = true, wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE,
)
fun MainMetronomeScreenPreview() {
    MetronomeTheme {
        MainScreen() {}
    }
}