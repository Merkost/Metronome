package com.merkost.metronome.platform

import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

class IosPlatformActions : PlatformActions {
    override fun contactSupport() {
        val device = UIDevice.currentDevice
        val appVersion = IosAppVersionProvider().getAppVersion()
        val subject = "Support Request from Metronome App"
        val body = buildString {
            append("Please describe the issue you're experiencing or your question below:\n")
            append("\n\n\n\n\n")
            append("Device Information:\n")
            append("OS Version: ${device.systemName} ${device.systemVersion}\n")
            append("Device Model: ${device.model}\n")
            append("App version: ${appVersion?.versionName ?: "unknown"} (${appVersion?.versionNumber ?: "unknown"})\n")
        }
        val encodedSubject = (subject as NSString)
            .stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet) ?: return
        val encodedBody = (body as NSString)
            .stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet) ?: return
        val url = NSURL(string = "mailto:merkostdev+metronome@gmail.com?subject=$encodedSubject&body=$encodedBody") ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }

    override fun rateApp() {
        val url = NSURL(string = "https://apps.apple.com/app/id6480380648") ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }

    override fun isDynamicColorSupported(): Boolean = false
}
