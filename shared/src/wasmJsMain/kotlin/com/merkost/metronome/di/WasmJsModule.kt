package com.merkost.metronome.di

import com.merkost.metronome.engine.MetronomePlayer
import com.merkost.metronome.engine.MetronomePlayerWasm
import com.merkost.metronome.logging.ReleaseLogTreeProvider
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.AudioFocusController
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.platform.HapticProviderWasm
import com.merkost.metronome.platform.LiveActivityController
import com.merkost.metronome.platform.NoopAudioFocusController
import com.merkost.metronome.platform.WasmLiveActivityController
import com.merkost.metronome.platform.PlatformActions
import com.merkost.metronome.platform.WasmAppVersionProvider
import com.merkost.metronome.platform.WasmPlatformActions
import com.merkost.metronome.review.InAppReviewRequester
import com.merkost.metronome.review.WasmInAppReviewRequester
import com.merkost.metronome.platform.createDataStore
import org.koin.dsl.module

val wasmJsModule = module {
    single<ReleaseLogTreeProvider> { ReleaseLogTreeProvider { null } }
    single { createDataStore() }
    single<MetronomePlayer> { MetronomePlayerWasm() }
    single<PlatformActions> { WasmPlatformActions() }
    single<AppVersionProvider> { WasmAppVersionProvider() }
    single<HapticProvider> { HapticProviderWasm() }
    single<AudioFocusController> { NoopAudioFocusController() }
    single<LiveActivityController> { WasmLiveActivityController() }
    single<InAppReviewRequester> { WasmInAppReviewRequester() }
}
