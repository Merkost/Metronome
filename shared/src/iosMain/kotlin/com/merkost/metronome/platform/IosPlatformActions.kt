package com.merkost.metronome.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosPlatformActions : PlatformActions {
    override fun contactSupport() {
        val url = NSURL(string = "mailto:merkostdev+metronome@gmail.com?subject=Support%20Request")
        UIApplication.sharedApplication.openURL(url)
    }

    override fun rateApp() {
        val url = NSURL(string = "https://apps.apple.com/app/id6480380648")
        UIApplication.sharedApplication.openURL(url)
    }

    override fun isDynamicColorSupported(): Boolean = false
}
