package com.merkost.metronome.app

import android.app.Application
import com.merkost.metronome.BuildConfig
import com.merkost.metronome.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MetronomeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@MetronomeApp)
            modules(
                listOf(appModule)
            )
        }

    }
}