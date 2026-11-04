package com.merkost.metronome.platform

actual fun isDebug(): Boolean = System.getProperty("metronome.debug", "false").toBoolean()
