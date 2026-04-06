package com.merkost.metronome.platform

import kotlin.time.Clock

fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

expect fun isDebug(): Boolean
