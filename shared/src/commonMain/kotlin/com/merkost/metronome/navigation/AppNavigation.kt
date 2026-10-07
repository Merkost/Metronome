package com.merkost.metronome.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.merkost.metronome.components.MetronomeMark
import com.merkost.metronome.engine.SoundPreviewController
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.screens.AnimatedSplash
import com.merkost.metronome.screens.SoundPickerSheet
import com.merkost.metronome.screens.WelcomeScreen
import com.merkost.metronome.viewModels.MetronomeViewModel
import org.koin.compose.koinInject
import com.merkost.metronome.screens.MainScreen
import com.merkost.metronome.screens.PracticePresetsScreen
import com.merkost.metronome.screens.PracticeSetsScreen
import com.merkost.metronome.screens.SettingsScreen
import com.merkost.metronome.ui.AppAnimations
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal val mainNavigationSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(baseClass = androidx.navigation3.runtime.NavKey::class) {
            subclass(serializer = MainDestination.Main.serializer())
            subclass(serializer = MainDestination.Settings.serializer())
            subclass(serializer = MainDestination.PracticePresets.serializer())
            subclass(serializer = MainDestination.PracticeSets.serializer())
        }
    }
}

@Composable
fun AppNavigation() {
    var splashComplete by rememberSaveable { mutableStateOf(false) }

    val viewModel: MetronomeViewModel = koinInject()
    val previewController: SoundPreviewController = koinInject()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        previewController.stop()
        viewModel.onOnboardingBackgrounded()
    }
    val onboardingLoaded by viewModel.onboardingLoaded.collectAsState()
    val onboardingVisible by viewModel.onboardingVisible.collectAsState()
    val onboardingLoadError by viewModel.onboardingLoadError.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = onboardingLoaded to onboardingVisible,
            transitionSpec = { AppAnimations.fadeScaleTransform },
            label = "firstUseNavigation",
        ) { (loaded, welcome) ->
            when {
                !loaded -> Box(
                    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        MetronomeMark(Modifier.size(80.dp), MaterialTheme.colorScheme.primary)
                        onboardingLoadError?.let { error ->
                            Text(error, style = MaterialTheme.typography.bodyMedium)
                            Button(onClick = viewModel::retryOnboardingLoad) { Text("Try again") }
                        }
                    }
                }
                welcome -> WelcomeRoute(viewModel)
                else -> AppNavigationHost()
            }
        }
        if (!splashComplete) {
            AnimatedSplash(onFinished = { splashComplete = true })
        }
    }
}

@Composable
private fun WelcomeRoute(viewModel: MetronomeViewModel) {
    val previewController: SoundPreviewController = koinInject()
    val activePreview by previewController.activeSound.collectAsState()
    val previewError by previewController.errorMessage.collectAsState()
    val selectedSound by viewModel.onboardingInitialSound.collectAsState()
    val volume by viewModel.clickVolume.collectAsState()
    val stereoPan by viewModel.stereoPan.collectAsState()
    val isSaving by viewModel.onboardingSaving.collectAsState()
    val error by viewModel.onboardingError.collectAsState()
    var bpm by rememberSaveable { mutableStateOf(80) }
    var soundName by rememberSaveable { mutableStateOf(selectedSound.name) }
    var showSoundPicker by rememberSaveable { mutableStateOf(false) }
    val sound = ClickSound.entries.firstOrNull { it.name == soundName } ?: ClickSound.WOOD
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(previewController, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                previewController.stop()
                viewModel.onOnboardingBackgrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            previewController.stop()
        }
    }

    WelcomeScreen(
        bpm = bpm,
        selectedSound = sound,
        isListening = activePreview != null && !showSoundPicker,
        isSaving = isSaving,
        error = error ?: previewError,
        onListen = {
            if (activePreview != null) previewController.stop()
            else previewController.preview(sound, bpm, volume, stereoPan)
        },
        onPaceSelected = { selectedBpm ->
            bpm = selectedBpm
            previewController.preview(sound, selectedBpm, volume, stereoPan)
        },
        onChooseSound = {
            previewController.stop()
            showSoundPicker = true
        },
        onStart = {
            previewController.stop()
            viewModel.completeOnboarding(bpm, sound, startPlaying = true)
        },
        onSkip = {
            previewController.stop()
            viewModel.completeOnboarding(bpm, sound, startPlaying = false)
        },
    )

    if (showSoundPicker) {
        SoundPickerSheet(
            selectedSound = sound,
            previewSound = activePreview,
            onSelect = {
                previewController.stop()
                soundName = it.name
            },
            onPreview = {
                if (activePreview == it) previewController.stop()
                else previewController.preview(it, 100, volume, stereoPan)
            },
            onDismiss = {
                previewController.stop()
                showSoundPicker = false
            },
            errorMessage = previewError,
        )
    }
}

@Composable
private fun AppNavigationHost() {
    val backStack = rememberNavBackStack(
        mainNavigationSavedStateConfiguration,
        MainDestination.Main,
    )
    val navigator = remember(backStack) { AppNavigator(backStack) }

    NavDisplay(
        backStack = backStack,
        onBack = { navigator.goBack() },
        transitionSpec = { AppAnimations.forwardNavigation() },
        popTransitionSpec = { AppAnimations.backwardNavigation() },
        predictivePopTransitionSpec = { AppAnimations.backwardNavigation() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<MainDestination.Main> {
                MainScreen(
                    onSettingsClicked = { navigator.navigate(MainDestination.Settings) },
                    onPresetsClicked = { navigator.navigate(MainDestination.PracticePresets) },
                    onPracticeSetsClicked = { navigator.navigate(MainDestination.PracticeSets) },
                )
            }
            entry<MainDestination.PracticePresets> {
                PracticePresetsScreen(
                    upPress = { navigator.goBack() },
                    onOpenPracticeSets = {
                        navigator.navigateBackToOrPush(MainDestination.PracticeSets)
                    },
                )
            }
            entry<MainDestination.PracticeSets> {
                PracticeSetsScreen(
                    upPress = { navigator.goBack() },
                    onStart = { navigator.returnToMain() },
                    onOpenPresets = { navigator.navigate(MainDestination.PracticePresets) },
                )
            }
            entry<MainDestination.Settings> {
                SettingsScreen(upPress = { navigator.goBack() })
            }
        },
    )
}
