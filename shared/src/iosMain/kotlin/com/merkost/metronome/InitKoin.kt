package com.merkost.metronome

import com.merkost.metronome.di.commonModule
import com.merkost.metronome.di.iosModule
import com.merkost.metronome.engine.LiveActivityObserver
import com.merkost.metronome.platform.LiveActivityController
import com.merkost.metronome.review.InAppReviewRequester
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

fun initKoin(
    liveActivityController: LiveActivityController,
    inAppReviewRequester: InAppReviewRequester,
) {
    startKoin {
        modules(
            commonModule,
            iosModule,
            module {
                single { liveActivityController }
                single { inAppReviewRequester }
            }
        )
    }
    KoinPlatform.getKoin().get<LiveActivityObserver>().start()
}
