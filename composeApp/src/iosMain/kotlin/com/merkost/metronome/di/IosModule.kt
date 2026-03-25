package com.merkost.metronome.di

import com.merkost.metronome.engine.MetronomePlayer
import com.merkost.metronome.engine.MetronomePlayerIos
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.IosAppVersionProvider
import com.merkost.metronome.platform.IosPlatformActions
import com.merkost.metronome.platform.PlatformActions
import com.merkost.metronome.platform.createDataStore
import org.koin.dsl.module

val iosModule = module {
    single { createDataStore() }
    single<MetronomePlayer> { MetronomePlayerIos() }
    single<PlatformActions> { IosPlatformActions() }
    single<AppVersionProvider> { IosAppVersionProvider() }
}
