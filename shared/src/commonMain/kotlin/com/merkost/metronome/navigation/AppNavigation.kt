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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.merkost.metronome.components.MetronomeMark
import com.merkost.metronome.engine.SoundPreviewController
import com.merkost.metronome.screens.AnimatedSplash
import com.merkost.metronome.screens.WelcomeScreen
import com.merkost.metronome.viewModels.MetronomeViewModel
import org.koin.compose.koinInject
import com.merkost.metronome.screens.MainScreen
import com.merkost.metronome.screens.PracticePresetsScreen
import com.merkost.metronome.screens.PracticeSetsScreen
import com.merkost.metronome.screens.SettingsScreen
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.DesktopAppShell
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
    val startsOnContent = LocalAppPresentation.current == AppPresentation.Desktop
    var splashComplete by rememberSaveable { mutableStateOf(startsOnContent) }

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
    val state by viewModel.metronomeState.collectAsState()
    val selectedSound by viewModel.onboardingInitialSound.collectAsState()
    val isSaving by viewModel.onboardingSaving.collectAsState()
    val error by viewModel.onboardingError.collectAsState()
    val finish = { viewModel.completeOnboarding(state.rhythm, selectedSound, startPlaying = false) }
    WelcomeScreen(
        isSaving = isSaving,
        error = error,
        onComplete = finish,
        onSkip = finish,
    )
}

@Composable
private fun AppNavigationHost() {
    val backStack = rememberNavBackStack(
        mainNavigationSavedStateConfiguration,
        MainDestination.Main,
    )
    val navigator = remember(backStack) { AppNavigator(backStack) }
    val presentation = LocalAppPresentation.current
    val commands = LocalAppCommands.current
    val desktop = presentation == AppPresentation.Desktop
    val desktopNavigationGuard = remember { DesktopNavigationGuard() }
    val navigate: (MainDestination) -> Unit = { destination ->
        if (desktop) {
            desktopNavigationGuard.navigate(
                current = backStack.lastOrNull() as? MainDestination ?: MainDestination.Main,
                destination = destination,
            ) { navigateDesktop(backStack, destination) }
        } else {
            navigator.navigate(destination)
        }
    }
    val goBack: () -> Unit = {
        if (desktop) navigate(MainDestination.Main) else navigator.goBack()
        Unit
    }
    LaunchedEffect(commands, desktop) {
        if (desktop) {
            commands.collect { command ->
                when (command) {
                    is AppCommand.Navigate -> navigate(command.destination)
                    is AppCommand.RequestExit -> desktopNavigationGuard.leave(
                        backStack.lastOrNull() as? MainDestination ?: MainDestination.Main,
                        command.proceed,
                    )
                    AppCommand.Back -> goBack()
                }
            }
        }
    }
    val content: @Composable () -> Unit = {
        NavDisplay(
            backStack = backStack,
            onBack = goBack,
            transitionSpec = { if (desktop) AppAnimations.fadeThroughRoute else AppAnimations.forwardNavigation() },
            popTransitionSpec = { if (desktop) AppAnimations.fadeThroughRoute else AppAnimations.backwardNavigation() },
            predictivePopTransitionSpec = { if (desktop) AppAnimations.fadeThroughRoute else AppAnimations.backwardNavigation() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<MainDestination.Main> {
                    MainScreen(
                        onSettingsClicked = { navigate(MainDestination.Settings) },
                        onPresetsClicked = { navigate(MainDestination.PracticePresets) },
                        onPracticeSetsClicked = { navigate(MainDestination.PracticeSets) },
                    )
                }
                entry<MainDestination.PracticePresets> {
                    PracticePresetsScreen(
                        upPress = goBack,
                        onOpenPracticeSets = {
                            if (desktop) navigate(MainDestination.PracticeSets) else navigator.navigateBackToOrPush(MainDestination.PracticeSets)
                        },
                    )
                }
                entry<MainDestination.PracticeSets> {
                    PracticeSetsScreen(
                        upPress = goBack,
                        onStart = { navigate(MainDestination.Main) },
                        onOpenPresets = {
                            if (desktop) navigate(MainDestination.PracticePresets) else navigator.navigateBackToOrPush(MainDestination.PracticePresets)
                        },
                    )
                }
                entry<MainDestination.Settings> {
                    SettingsScreen(upPress = goBack)
                }
            },
        )
    }
    if (desktop) {
        CompositionLocalProvider(LocalDesktopNavigationGuard provides desktopNavigationGuard) {
            DesktopAppShell(
                selectedDestination = backStack.lastOrNull() as? MainDestination ?: MainDestination.Main,
                onNavigate = navigate,
                playing = koinInject<MetronomeViewModel>().isPlaying.collectAsState().value,
                content = content,
            )
        }
    } else {
        content()
    }
}
