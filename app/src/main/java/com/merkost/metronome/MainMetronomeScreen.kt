package com.merkost.metronome

import Ball
import MetronomeBalls
import OutlinedCircle
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.theme.MetronomeTheme
import kotlinx.coroutines.delay

val BallSize = 40.dp
val CircleSize = BallSize + 32.dp
val CircleWeight = 5.dp
val BallColor = Color.LightGray

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
                .padding(it)
                .padding(top = 128.dp)
        ) {

            val springSpec = SpringSpec<Float>(
                // Determined experimentally
                stiffness = 800f,
                dampingRatio = 0.8f
            )

            val tweenSpec = tween<Float>(100)

            var selectedIndex by remember {
                mutableStateOf(0)
            }
            val itemCount = 4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MetronomeBalls(
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
@Preview(showBackground = true)

fun MainMetronomeScreenPreview() {
    MetronomeTheme {
        MainMetronomeScreen()
    }
}