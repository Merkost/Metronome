package com.merkost.metronome.di

import com.merkost.metronome.engine.DefaultSoundPreviewController
import com.merkost.metronome.engine.SoundPreviewController
import com.merkost.metronome.engine.LiveActivityObserver
import com.merkost.metronome.engine.MetronomeEngine
import com.merkost.metronome.logging.CedarSetup
import com.merkost.metronome.logging.ReleaseLogTreeProvider
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.platform.isDebug
import com.merkost.metronome.platform.currentTimeMillis
import com.merkost.metronome.practiceSets.DataStorePracticeSessionRepository
import com.merkost.metronome.practiceSets.DataStorePracticeSetRepository
import com.merkost.metronome.practiceSets.PracticeSessionController
import com.merkost.metronome.practiceSets.PracticeSessionRepository
import com.merkost.metronome.practiceSets.PracticeSetRepository
import com.merkost.metronome.presets.DataStorePracticePresetRepository
import com.merkost.metronome.presets.PracticePresetRepository
import com.merkost.metronome.review.DataStoreReviewPromptStore
import com.merkost.metronome.review.ReviewPromptCoordinator
import com.merkost.metronome.review.ReviewPromptStore
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.whatsnew.WhatsNewCoordinator
import com.merkost.metronome.viewModels.PracticePresetsViewModel
import com.merkost.metronome.viewModels.PracticeSetsViewModel
import com.merkost.metronome.viewModels.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.random.Random

val commonModule = module {
    single<AppDatastore> { AppDatastoreImpl(get()) }
    single { com.merkost.metronome.backup.PracticeBackupRepository(get()) }
    single<PracticePresetRepository> {
        DataStorePracticePresetRepository(
            dataStore = get(),
            nextId = {
                "preset-${currentTimeMillis()}-${Random.nextInt().toUInt().toString(16)}"
            },
            nowMillis = ::currentTimeMillis,
        )
    }
    single {
        com.merkost.metronome.practiceSets.PracticeStarterRepository(
            dataStore = get(),
            nextId = { "starter-${currentTimeMillis()}-${Random.nextInt().toUInt().toString(16)}" },
            nowMillis = ::currentTimeMillis,
        )
    }
    single<PracticeSetRepository> {
        DataStorePracticeSetRepository(
            dataStore = get(),
            nextId = {
                "set-${currentTimeMillis()}-${Random.nextInt().toUInt().toString(16)}"
            },
            nowMillis = ::currentTimeMillis,
        )
    }
    single<PracticeSessionRepository> { DataStorePracticeSessionRepository(get()) }
    single {
        PracticeSessionController(
            repository = get(),
            nextId = {
                "session-${currentTimeMillis()}-${Random.nextInt().toUInt().toString(16)}"
            },
            nowMillis = ::currentTimeMillis,
        )
    }
    single<ReviewPromptStore> { DataStoreReviewPromptStore(get()) }
    single { ReviewPromptCoordinator(get(), get(), get()) }
    single { WhatsNewCoordinator(get(), get()) }
    single { MetronomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    single {
        CedarSetup.initialize(isDebug(), get<ReleaseLogTreeProvider>().releaseTree())
        MetronomeEngine(get(), get(), get(), get(), get()).also { it.start() }
    }
    single<SoundPreviewController> {
        DefaultSoundPreviewController(
            player = get(named("soundPreview")),
            audioFocus = get(named("soundPreview")),
            isPlaybackActive = { get<MetronomeViewModel>().metronomeState.value.playing },
            onFailure = { error -> org.kimplify.cedar.logging.Cedar.tag("SoundPreview").e("Preview failed: ${error.message}") },
        )
    }
    single { LiveActivityObserver(get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel {
        val appDatastore = get<AppDatastore>()
        PracticePresetsViewModel(
            repository = get(),
            countInEnabled = appDatastore.countInEnabled,
            practiceSetRepository = get(),
        )
    }
    viewModel {
        PracticeSetsViewModel(
            repository = get(),
            presetRepository = get(),
            sessionController = get(),
            starterRepository = get(),
            nextStepId = {
                "step-${currentTimeMillis()}-${Random.nextInt().toUInt().toString(16)}"
            },
        )
    }
}
