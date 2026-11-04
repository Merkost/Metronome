package com.merkost.metronome.di

import com.merkost.metronome.engine.MetronomePlayer
import com.merkost.metronome.engine.MetronomePlayerDesktop
import com.merkost.metronome.logging.ReleaseLogTreeProvider
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.AudioFocusController
import com.merkost.metronome.platform.DesktopAppVersionProvider
import com.merkost.metronome.platform.DesktopDataStorePersistence
import com.merkost.metronome.platform.DesktopHapticProvider
import com.merkost.metronome.platform.DesktopInAppReviewRequester
import com.merkost.metronome.platform.DesktopLiveActivityController
import com.merkost.metronome.platform.DesktopPlatformActions
import com.merkost.metronome.platform.DesktopPersistence
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.platform.LiveActivityController
import com.merkost.metronome.platform.NoopAudioFocusController
import com.merkost.metronome.platform.PlatformActions
import com.merkost.metronome.platform.createDataStore
import com.merkost.metronome.review.InAppReviewRequester
import org.koin.core.qualifier.named
import org.koin.dsl.module

val desktopModule = module {
    single<ReleaseLogTreeProvider> { ReleaseLogTreeProvider { null } }
    single { createDataStore() }
    single<DesktopPersistence> { DesktopDataStorePersistence(get()) }
    single<MetronomePlayer> { MetronomePlayerDesktop() }
    single<MetronomePlayer>(named("soundPreview")) { MetronomePlayerDesktop() }
    single<PlatformActions> { DesktopPlatformActions() }
    single<AppVersionProvider> { DesktopAppVersionProvider() }
    single<HapticProvider> { DesktopHapticProvider() }
    single<AudioFocusController> { NoopAudioFocusController() }
    single<AudioFocusController>(named("soundPreview")) { NoopAudioFocusController() }
    single<LiveActivityController> { DesktopLiveActivityController() }
    single<InAppReviewRequester> { DesktopInAppReviewRequester() }
}
