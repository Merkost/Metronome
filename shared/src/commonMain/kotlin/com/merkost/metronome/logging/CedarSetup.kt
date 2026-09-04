package com.merkost.metronome.logging

import org.kimplify.cedar.logging.Cedar
import org.kimplify.cedar.logging.LogTree
import org.kimplify.cedar.logging.trees.ConsoleTree

object CedarSetup {
    fun initialize(isDebug: Boolean, releaseTree: LogTree?) {
        Cedar.clearForest()
        if (isDebug) {
            Cedar.plant(ConsoleTree())
        } else {
            releaseTree?.let { Cedar.plant(it) }
        }
    }
}
