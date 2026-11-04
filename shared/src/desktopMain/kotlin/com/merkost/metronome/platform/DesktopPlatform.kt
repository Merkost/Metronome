package com.merkost.metronome.platform

import com.merkost.metronome.model.Beat
import com.merkost.metronome.review.InAppReviewRequester
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DesktopPlatformActions : PlatformActions {
    override fun contactSupport() {
        val subject = URLEncoder.encode("Support request from Metronome for Mac", StandardCharsets.UTF_8).replace("+", "%20")
        val mail = URI("mailto:merkostdev+metronome@gmail.com?subject=$subject")
        val desktop = runCatching { Desktop.getDesktop() }.getOrNull() ?: return
        val mailed = desktop.isSupported(Desktop.Action.MAIL) && runCatching { desktop.mail(mail) }.isSuccess
        if (!mailed && desktop.isSupported(Desktop.Action.BROWSE)) {
            runCatching { desktop.browse(URI("https://mail.google.com/mail/?view=cm&fs=1&to=merkostdev%2Bmetronome%40gmail.com&su=$subject")) }
        }
    }

    override fun rateApp() {
        val desktop = runCatching { Desktop.getDesktop() }.getOrNull() ?: return
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            runCatching { desktop.browse(URI("https://apps.apple.com/app/id6761737690?action=write-review")) }
        }
    }

    override fun isDynamicColorSupported(): Boolean = false
}

class DesktopAppVersionProvider : AppVersionProvider {
    override fun getAppVersion(): AppVersionInfo = AppVersionInfo(
        System.getProperty("metronome.version", "1.5.0"),
        System.getProperty("metronome.build", "1").toLongOrNull() ?: 1L,
    )
}

class DesktopHapticProvider : HapticProvider {
    override fun playBeatHaptic(beat: Beat) = Unit
    override fun playConfirmHaptic() = Unit
}

class DesktopLiveActivityController : LiveActivityController {
    override fun start(snapshot: LiveActivitySnapshot) = Unit
    override fun update(snapshot: LiveActivitySnapshot) = Unit
    override fun end() = Unit
}

class DesktopInAppReviewRequester : InAppReviewRequester {
    override fun requestReview(): Boolean = false
}
