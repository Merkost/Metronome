package com.merkost.metronome.di

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.viewModels.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<AppDatastore> { AppDatastoreImpl(androidContext()) }

    single { MetronomeViewModel(get()) }
    factory {
        SoundPool.Builder()
            .setMaxStreams(4) // to prevent delaying the next tick under any circumstances
            .setAudioAttributes(
                AudioAttributes.Builder().setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            ).build()
    }
    viewModel { SettingsViewModel(get()) }
}