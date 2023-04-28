package com.merkost.metronome.di

import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.viewModels.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<AppDatastore> { AppDatastoreImpl(androidContext()) }

    single { MetronomeViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}