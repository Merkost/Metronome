package com.merkost.metronome.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosPlatformActions : PlatformActions {
    override fun contactSupport() {
        val url = NSURL(string = "mailto:merkostdev+metronome@gmail.com?subject=Support%20Request") ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }

    override fun rateApp() {
        val url = NSURL(string = "https://apps.apple.com/app/id6480380648") ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }

    override fun isDynamicColorSupported(): Boolean = false
}
