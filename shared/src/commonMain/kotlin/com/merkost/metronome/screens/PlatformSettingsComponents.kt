package com.merkost.metronome.screens

import androidx.compose.runtime.Composable

@Composable
expect fun AppInfoCard()

@Composable
expect fun VolumeSlider()

@Composable
expect fun BackgroundPlayPermissionCheck(backgroundPlayEnabled: Boolean)
