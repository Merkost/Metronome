package com.merkost.metronome.di

import com.merkost.metronome.engine.MetronomePlayer
import com.merkost.metronome.engine.MetronomePlayerAndroid
import com.merkost.metronome.platform.AndroidAppVersionProvider
import com.merkost.metronome.platform.AndroidAudioFocusController
import com.merkost.metronome.platform.AndroidPlatformActions
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.AudioFocusController
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.platform.HapticProviderAndroid
import com.merkost.metronome.platform.LiveActivityController
import com.merkost.metronome.platform.NoopLiveActivityController
import com.merkost.metronome.platform.PlatformActions
import com.merkost.metronome.platform.createDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single { createDataStore(androidContext()) }
    single<MetronomePlayer> { MetronomePlayerAndroid(androidContext()) }
    single<PlatformActions> { AndroidPlatformActions(androidContext()) }
    single<AppVersionProvider> { AndroidAppVersionProvider(androidContext()) }
    single<HapticProvider> { HapticProviderAndroid(androidContext()) }
    single<LiveActivityController> { NoopLiveActivityController() }
    single<AudioFocusController> { AndroidAudioFocusController(androidContext()) }
}
