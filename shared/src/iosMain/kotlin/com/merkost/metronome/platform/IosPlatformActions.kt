package com.merkost.metronome.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

class IosPlatformActions : PlatformActions {
    override fun contactSupport() {
        val device = UIDevice.currentDevice
        val appVersion = IosAppVersionProvider().getAppVersion()
        val body = buildString {
            append("Please describe the issue you're experiencing or your question below:\n")
            append("\n\n\n\n\n")
            append("Device Information:\n")
            append("OS Version: ${device.systemName} ${device.systemVersion}\n")
            append("Device Model: ${device.model}\n")
            append("App version: ${appVersion?.versionName ?: "unknown"} (${appVersion?.versionNumber ?: "unknown"})\n")
        }
        val encodedBody = body.replace(" ", "%20")
            .replace("\n", "%0A")
            .replace(":", "%3A")
            .replace("(", "%28")
            .replace(")", "%29")
        val url = NSURL(string = "mailto:merkostdev+metronome@gmail.com?subject=Support%20Request%20from%20Metronome%20App&body=$encodedBody") ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }

    override fun rateApp() {
        val url = NSURL(string = "https://apps.apple.com/app/id6480380648") ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }

    override fun isDynamicColorSupported(): Boolean = false
}
