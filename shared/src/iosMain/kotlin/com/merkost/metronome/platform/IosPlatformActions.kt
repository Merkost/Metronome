package com.merkost.metronome.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

private const val SupportEmail = "merkostdev+metronome@gmail.com"

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
        val encodedSubject = subject.urlEncoded()
        val encodedBody = body.urlEncoded()
        val mailUrl = "mailto:$SupportEmail?subject=$encodedSubject&body=$encodedBody"
        val browserUrl = "https://mail.google.com/mail/?view=cm&fs=1&to=${SupportEmail.urlEncoded()}&su=$encodedSubject&body=$encodedBody"
        openUrl(mailUrl, browserUrl)
    }

    override fun rateApp() {
        openUrl(
            urlString = "itms-apps://itunes.apple.com/app/id6761737690?action=write-review",
            fallbackUrlString = "https://apps.apple.com/app/id6761737690?action=write-review"
        )
    }

    override fun isDynamicColorSupported(): Boolean = false
}

private fun String.urlEncoded(): String {
    val hex = "0123456789ABCDEF"
    return buildString {
        encodeToByteArray().forEach { byte ->
            val value = byte.toInt() and 0xFF
            val char = value.toChar()
            if (char.isUrlSafe()) {
                append(char)
            } else {
                append('%')
                append(hex[value shr 4])
                append(hex[value and 0x0F])
            }
        }
    }
}

private fun Char.isUrlSafe(): Boolean =
    this in 'A'..'Z' ||
        this in 'a'..'z' ||
        this in '0'..'9' ||
        this == '-' ||
        this == '_' ||
        this == '.' ||
        this == '~'

private fun openUrl(urlString: String, fallbackUrlString: String? = null) {
    val url = NSURL.URLWithString(urlString) ?: return
    UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>()) { success ->
        if (!success && fallbackUrlString != null) {
            val fallbackUrl = NSURL.URLWithString(fallbackUrlString) ?: return@openURL
            UIApplication.sharedApplication.openURL(fallbackUrl, emptyMap<Any?, Any>(), null)
        }
    }
}
