package com.merkost.metronome.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.BeatDisplayStyle
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.ui.theme.AppColorScheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val appDatastore: AppDatastore
) : ViewModel() {

    val colorScheme = appDatastore.colorScheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppColorScheme.BLACKNWHITE)
    val colorFlash = appDatastore.colorFlash
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val backgroundPlay = appDatastore.backgroundPlay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val totalTime = appDatastore.totalTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0L)
    val currentStereo = appDatastore.stereoSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val selectedSound = appDatastore.selectedSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ClickSound.WOOD)
    val hapticEnabled = appDatastore.hapticEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val keepScreenAwake = appDatastore.keepScreenAwake
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    val countInEnabled = appDatastore.countInEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val practiceStreak = appDatastore.practiceStreak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val beatDisplayStyle = appDatastore.beatDisplayStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BeatDisplayStyle.DOTS)
    val liveActivityEnabled = appDatastore.liveActivityEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    fun onColorFlashChanged(b: Boolean) {
        viewModelScope.launch {
            appDatastore.saveColorFlash(b)
        }
    }

    fun onBackgroundPlayChanged(b: Boolean) {
        viewModelScope.launch {
            appDatastore.saveBackgroundPlay(b)
        }
    }

    fun onHapticChanged(enabled: Boolean) {
        viewModelScope.launch {
            appDatastore.saveHapticEnabled(enabled)
        }
    }

    fun onKeepScreenAwakeChanged(enabled: Boolean) {
        viewModelScope.launch {
            appDatastore.saveKeepScreenAwake(enabled)
        }
    }

    fun onCountInChanged(enabled: Boolean) {
        viewModelScope.launch {
            appDatastore.saveCountInEnabled(enabled)
        }
    }

    fun onLiveActivityChanged(enabled: Boolean) {
        viewModelScope.launch {
            appDatastore.saveLiveActivityEnabled(enabled)
        }
    }

    fun onBeatDisplayStyleChanged(style: BeatDisplayStyle) {
        viewModelScope.launch {
            appDatastore.saveBeatDisplayStyle(style)
        }
    }

    fun onColorSchemeChanged(it: AppColorScheme) {
        viewModelScope.launch {
            appDatastore.saveColorScheme(it)
        }
    }

    fun onSoundChanged(sound: ClickSound) {
        viewModelScope.launch {
            appDatastore.saveSelectedSound(sound)
        }
    }

    fun onStereoChanged(stereo: Float) {
        viewModelScope.launch {
            appDatastore.saveStereo(stereo.toInt().coerceIn(-5, 5))
        }
    }

    fun resetTotalTime() {
        viewModelScope.launch {
            appDatastore.resetTime()
        }
    }
}