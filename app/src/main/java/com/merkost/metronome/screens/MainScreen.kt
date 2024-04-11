package com.merkost.metronome.screens

import Ball
import MetronomeBalls
import OutlinedCircle
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.merkost.metronome.components.MainButtonsRow
import com.merkost.metronome.components.MyIconButton
import com.merkost.metronome.components.MySecondaryTextButton
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.ui.theme.MetronomeTheme
import com.merkost.metronome.viewModels.MetronomeViewModel
import org.koin.androidx.compose.koinViewModel

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
    val viewModel: MetronomeViewModel = koinViewModel()

//    val service = (context as MainActivity).metronomeService
    val colorFlash by viewModel.colorFlash.collectAsState()
    val metronomeState: MetronomeState by viewModel.metronomeState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedIndex by viewModel.index.collectAsState()

    val springSpec = SpringSpec<Float>(stiffness = 600f, dampingRatio = 0.8f)

    val boxColorAnimation = remember { Animatable(0f) }

    if (colorFlash) {
        LaunchedEffect(selectedIndex) {
            if (metronomeState.playing) {
                boxColorAnimation.animateTo(0.35f, springSpec)
                boxColorAnimation.animateTo(0f, springSpec)
            } else {
                boxColorAnimation.animateTo(0f, springSpec)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            primaryColor,
                            alpha = boxColorAnimation.value.coerceIn(0f, 0.5f)
                        )
                    }
                    .padding(horizontal = horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Feel the Beat",
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
                    indicator = {
                        OutlinedCircle(MaterialTheme.colorScheme.primary)
                    },
                ) {
                    (1..BallsCount).forEach {
                        val color by animateColorAsState(
                            targetValue =
                            if (it == 1 || selectedIndex.coerceIn(0..3) == it - 1) {
                                MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.primaryContainer,
                            label = "ballsColor"
                        )
                        Ball(color = color)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedContent(targetState = metronomeState.tempoName) { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    }

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
                    modifier = Modifier.height(20.dp),
                    value = metronomeState.rhythm.toFloat(),
                    onValueChange = viewModel::onSliderValueChanged,
                    valueRange = viewModel.metronomeRange,
                    steps = viewModel.steps,
                    colors = SliderDefaults.colors(
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
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
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
            MainButtonsRow(
                Modifier.padding(horizontal = horizontalPadding),
                isPlaying = isPlaying,
                metronomeState.stopWatchState,
                onPlayPause = viewModel::onPlayPauseClicked,
                onTempoTap = viewModel::onTempoTap
            )
        }
    }


}


@Composable
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(
    showBackground = true, wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE,
)
fun MainMetronomeScreenPreview() {
    MetronomeTheme {
        MainScreen() {}
    }
}