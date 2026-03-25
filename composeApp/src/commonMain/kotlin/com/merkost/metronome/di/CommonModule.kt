package com.merkost.metronome.di

import com.merkost.metronome.engine.MetronomeEngine
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.viewModels.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonModule = module {
    single<AppDatastore> { AppDatastoreImpl(get()) }
    single { MetronomeViewModel(get()) }
    single { MetronomeEngine(get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
}
