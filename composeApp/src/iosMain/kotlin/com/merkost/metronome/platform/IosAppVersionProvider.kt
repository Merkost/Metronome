package com.merkost.metronome.platform

import platform.Foundation.NSBundle

class IosAppVersionProvider : AppVersionProvider {
    override fun getAppVersion(): AppVersionInfo? {
        val info = NSBundle.mainBundle.infoDictionary ?: return null
        val name = info["CFBundleShortVersionString"] as? String ?: ""
        val build = (info["CFBundleVersion"] as? String)?.toLongOrNull() ?: 0L
        return AppVersionInfo(name, build)
    }
}
