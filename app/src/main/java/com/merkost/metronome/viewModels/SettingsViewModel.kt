package com.merkost.metronome.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.ui.theme.AppColorScheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(val appDatastore: AppDatastore) : ViewModel() {

    val colorScheme = appDatastore.color
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppColorScheme.BLACKNWHITE)
    val colorFlash = appDatastore.colorFlash
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val backgroundPlay = appDatastore.backgroundPlay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val totalTime = appDatastore.totalTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0L)
    val currentStereo = appDatastore.stereoSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

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

    fun onColorSchemeChanged(it: AppColorScheme) {
        viewModelScope.launch {
            appDatastore.saveColor(it)
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