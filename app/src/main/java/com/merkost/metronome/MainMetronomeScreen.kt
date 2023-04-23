package com.merkost.metronome

import Ball
import MetronomeBalls
import OutlinedCircle
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.viewModelFactory
import com.merkost.metronome.ui.theme.MetronomeTheme
import kotlinx.coroutines.delay

val BallSize = 40.dp
val CircleSize = BallSize + 32.dp
val CircleWeight = 5.dp
val BallColor = Color.LightGray
val MetronomeMinimum = 40
val MetronomeMaximum = 208

val defaultPlayButtonSize = 85.dp

val horizontalPadding = 18.dp

val defaultIconButtonSize = 70.dp
val defaultSecondaryIconButtonSize = defaultIconButtonSize


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMetronomeScreen() {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text(
                    text = "BeatMate",
                    style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                )
            },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Settings, Icons.Default.Settings.name)
                    }
                })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val springSpec = SpringSpec<Float>(
                // Determined experimentally
                stiffness = 800f,
                dampingRatio = 0.8f
            )

            var selectedIndex by remember {
                mutableStateOf(0)
            }

            var sliderValue by remember {
                mutableStateOf(168)
            }

            val itemCount = 4
            MetronomeBalls(
                modifier = Modifier.padding(top = 64.dp, bottom = 64.dp),
                selectedIndex = selectedIndex,
                itemCount = itemCount,
//                    arrangementSpacing = ,
                animSpec = springSpec,
                indicator = {
                    OutlinedCircle()
                },
            ) {
                (1..itemCount).forEach {
                    val color = if (it == 1) Color.Black else BallColor
                    Ball(color = color)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Allegro",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MyIconButton(Icons.Default.Remove) {}
                    Text(
                        modifier = Modifier.weight(1f),
                        text = sliderValue.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    )
                    MyIconButton(Icons.Default.Add) {}

                }
                Spacer(modifier = Modifier.size(32.dp))
                Slider(
                    modifier = Modifier.height(16.dp),
                    value = sliderValue.toFloat(), onValueChange = { sliderValue = it.toInt() },
                    valueRange = MetronomeMinimum.toFloat()..MetronomeMaximum.toFloat(),
                    steps = MetronomeMaximum - MetronomeMinimum,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Black, activeTrackColor = Color.Black,
                        inactiveTrackColor = Color.LightGray
                    )
                )
                Spacer(modifier = Modifier.size(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MySecondaryTextButton(text = "- 5", onClick = {})
                    MySecondaryTextButton(text = "÷ 2", onClick = {})
                    MySecondaryTextButton(text = "× 2", onClick = {})
                    MySecondaryTextButton(text = "+ 5", onClick = {})
                }
                Spacer(modifier = Modifier.size(64.dp))

                Divider(modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.size(32.dp))


                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    var secondaryButtonSize by remember {
                        mutableStateOf(IntSize.Zero)
                    }

                    val density = LocalDensity.current

                    MySecondaryButton(
                        onClick = {},
                        shape = RoundedCornerShape(30),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                                .heightIn(density.run { secondaryButtonSize.height.toDp() }, defaultPlayButtonSize)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Rounded.Alarm, Icons.Rounded.Alarm.name)
                            Text(
                                text = "0:00",
                                maxLines = 2,
                                lineHeight = 18.sp,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(horizontalPadding))
                    PlayButton(Modifier, state = MetronomeState.Stopped) {}
                    Spacer(modifier = Modifier.size(horizontalPadding))

                    MySecondaryButton(onClick = {}, shape = RoundedCornerShape(30),
                        modifier = Modifier
                            .onSizeChanged {
                                secondaryButtonSize = it
                            }
                            .weight(1f).fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp).heightIn(max = defaultPlayButtonSize)
                        ) {
                            Icon(Icons.Rounded.TouchApp, Icons.Rounded.TouchApp.name)
                            Text(
                                text = "Tap\nTempo",
                                maxLines = 2,
                                lineHeight = 18.sp,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                }

            }

            LaunchedEffect(key1 = Unit) {
                (0..999).forEach {
                    selectedIndex = 1
                    delay(1000)
                    selectedIndex = 2
                    delay(1000)
                    selectedIndex = 3
                    delay(1000)
                    selectedIndex = 0
                    delay(1000)
                }
            }

        }
    }
}

@Composable
fun MySecondaryTextButton(text: String, onClick: () -> Unit) {
    MySecondaryButton(onClick = onClick, modifier = Modifier.size(defaultSecondaryIconButtonSize)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySecondaryButton(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        colors = CardDefaults.outlinedCardColors(),
        modifier = modifier,
        onClick = onClick,
        shape = shape
    ) {
        content()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = defaultIconButtonSize,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(size)
            .then(modifier),
        onClick = onClick,
        shape = CircleShape
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(icon, icon.name, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    state: MetronomeState,
    size: Dp = defaultPlayButtonSize,
    onClick: () -> Unit
) {
    val icon = Icons.Rounded.PlayArrow
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black, contentColor = Color.White),
        modifier = Modifier
            .size(size)
            .then(modifier),
        onClick = onClick,
        shape = CircleShape
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                icon, icon.name, modifier = Modifier
                    .align(Alignment.Center)
                    .size(50.dp)
            )
        }
    }
}


@Composable
//@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(
    showBackground = true, wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE,
)
fun MainMetronomeScreenPreview() {
    MetronomeTheme {
        MainMetronomeScreen()
    }
}

sealed class MetronomeState {
    object Stopped: MetronomeState()
    class Playing(rhythm: Int): MetronomeState()
}