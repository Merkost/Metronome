package com.merkost.metronome.platform

class WasmPlatformActions : PlatformActions {
    override fun contactSupport() {
        openUrl("mailto:merkostdev+metronome@gmail.com?subject=Support%20Request%20from%20Metronome%20Web")
    }

    override fun rateApp() {
        openUrl("https://play.google.com/store/apps/details?id=com.merkost.metronome")
    }

    override fun isDynamicColorSupported(): Boolean = false
}

class WasmAppVersionProvider : AppVersionProvider {
    override fun getAppVersion(): AppVersionInfo? = AppVersionInfo("1.3.0 (web)", 9)
}

private fun openUrl(url: String): Unit = js("window.open(url, '_blank')")
