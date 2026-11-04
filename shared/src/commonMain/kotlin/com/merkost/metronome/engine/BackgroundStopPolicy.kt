package com.merkost.metronome.engine

fun shouldStopWhenHidden(backgroundPlay: Boolean, screenInteractive: Boolean): Boolean =
    !backgroundPlay && screenInteractive
