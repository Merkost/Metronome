package com.merkost.metronome.logging

import org.kimplify.cedar.logging.Cedar
import org.kimplify.cedar.logging.trees.ConsoleTree

object CedarSetup {
    fun initialize(isDebug: Boolean) {
        Cedar.clearForest()
        if (isDebug) {
            Cedar.plant(ConsoleTree())
        } else {
            Cedar.plant(CrashlyticsTree())
        }
    }
}
