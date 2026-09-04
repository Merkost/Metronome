package com.merkost.metronome.logging

import org.kimplify.cedar.logging.LogTree

fun interface ReleaseLogTreeProvider {
    fun releaseTree(): LogTree?
}
