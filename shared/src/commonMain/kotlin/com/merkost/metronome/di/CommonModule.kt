package com.merkost.metronome.di

import com.merkost.metronome.engine.LiveActivityObserver
import com.merkost.metronome.engine.MetronomeEngine
import com.merkost.metronome.logging.CedarSetup
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.platform.isDebug
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.viewModels.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonModule = module {
    single<AppDatastore> { AppDatastoreImpl(get()) }
    single { MetronomeViewModel(get(), get()) }
    single {
        CedarSetup.initialize(isDebug())
        MetronomeEngine(get(), get(), get(), get()).also { it.start() }
    }
    single { LiveActivityObserver(get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
}
