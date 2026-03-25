package com.merkost.metronome.app

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.merkost.metronome.BuildConfig
import com.merkost.metronome.di.androidModule
import com.merkost.metronome.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class MetronomeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@MetronomeApp)
            modules(commonModule, androidModule)
        }

    }

    /** A tree which logs important information for crash reporting.  */
    private inner class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return
            }
            if (priority == Log.ERROR || priority == Log.WARN) {
                val crashlytics = Firebase.crashlytics
                if (t != null) {
                    crashlytics.recordException(t)
                } else {
                    val category = when (priority) {
                        Log.ERROR -> "E"
                        Log.WARN -> "W"
                        else -> throw IllegalStateException()
                    }
                    // https://firebase.google.com/docs/crashlytics/upgrade-sdk?platform=android
                    // To log a message to a crash report, use the following syntax:
                    // crashlytics.log("E/TAG: my message")
                    crashlytics.log("$category/$tag: $message")
                }
            }
        }
    }
}
