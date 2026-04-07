package com.merkost.metronome.logging

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import org.kimplify.cedar.logging.LogPriority
import org.kimplify.cedar.logging.LogTree

class CrashlyticsTree : LogTree {
    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?) {
        if (!priority.isAtLeast(LogPriority.WARNING)) return
        val crashlytics = Firebase.crashlytics
        crashlytics.log("${priority.name.first()}/$tag: $message")
        if (throwable != null) {
            crashlytics.recordException(throwable)
        }
    }
}
