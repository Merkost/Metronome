package com.merkost.metronome.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.merkost.metronome.components.EmptyState
import com.merkost.metronome.components.EmptyStateArt
import com.merkost.metronome.model.resolve
import com.merkost.metronome.model.timerFinishedHeadline
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppPresentation
import com.merkost.metronome.navigation.MainDestination
import com.merkost.metronome.screens.ActiveTimerContent
import com.merkost.metronome.screens.SettingsActions
import com.merkost.metronome.screens.SettingsScreenContent
import com.merkost.metronome.screens.SettingsUiState
import com.merkost.metronome.screens.WelcomeScreen
import com.merkost.metronome.ui.theme.AppColorScheme
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.coroutines.runBlocking
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.preset_empty_body
import metronome.shared.generated.resources.preset_empty_title
import metronome.shared.generated.resources.settings_playback
import metronome.shared.generated.resources.timer_stop_when_up
import metronome.shared.generated.resources.welcome_hear_bar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class LocalizedRenderingTest {
    private val originalLocale = Locale.getDefault()

    private val locales = listOf("es", "fr", "de", "pt-BR", "it", "ja", "ko", "zh-CN", "ru", "tr")

    private val literals = mapOf(
        "de" to mapOf(
            "welcome_hear_bar" to "Einen Takt anhören",
            "settings_playback" to "Wiedergabe",
        ),
        "ja" to mapOf(
            "welcome_hear_bar" to "1小節を聴く",
            "settings_playback" to "再生",
        ),
    )

    @AfterTest
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    private fun <T> underLocale(tag: String, block: () -> T): T {
        val previous = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag(tag))
        try {
            return block()
        } finally {
            Locale.setDefault(previous)
        }
    }

    private fun translated(tag: String, resource: StringResource): String = underLocale(tag) {
        runBlocking { org.jetbrains.compose.resources.getString(resource) }
    }

    private fun english(resource: StringResource): String = translated("en-US", resource)

    @Test
    fun everyShippedLocaleTranslatesTheKeyScreenStrings() {
        locales.forEach { tag ->
            listOf(
                Res.string.welcome_hear_bar,
                Res.string.settings_playback,
                Res.string.preset_empty_title,
                Res.string.preset_empty_body,
                Res.string.timer_stop_when_up,
            ).forEach { resource ->
                assertNotEquals(english(resource), translated(tag, resource), "$tag left ${resource.key} in English")
            }
        }
    }

    @Test
    fun germanAndJapaneseMatchTheReviewedWording() {
        literals.forEach { (tag, expected) ->
            assertEquals(expected.getValue("welcome_hear_bar"), translated(tag, Res.string.welcome_hear_bar))
            assertEquals(expected.getValue("settings_playback"), translated(tag, Res.string.settings_playback))
        }
    }

    @Test
    fun welcomeScreenRendersInEveryLocale() {
        locales.forEach { tag ->
            underLocale(tag) {
                runDesktopComposeUiTest(testTimeout = 30.seconds) {
                    setContent {
                        MaterialTheme(colorScheme = AppColorScheme.MELROSE.lightColor) {
                            WelcomeScreen(false, null, {}, {}, hearing = false, onHearBar = {})
                        }
                    }
                    waitForIdle()
                    onNode(hasText(translated(tag, Res.string.welcome_hear_bar))).assertExists()
                    onNode(hasText(english(Res.string.welcome_hear_bar))).assertDoesNotExist()
                }
            }
        }
    }

    @Test
    fun settingsContentRendersInEveryLocale() {
        locales.forEach { tag ->
            underLocale(tag) {
                runDesktopComposeUiTest(width = 1000, height = 900, testTimeout = 30.seconds) {
                    setContent {
                        CompositionLocalProvider(
                            LocalAppPresentation provides AppPresentation.Desktop,
                            LocalDensity provides Density(1f, 1.5f),
                        ) {
                            MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                                DesktopAppShell(MainDestination.Settings, {}) {
                                    SettingsScreenContent(
                                        state = SettingsUiState(supportsHaptics = false, version = "1.5.0"),
                                        actions = SettingsActions(),
                                        upPress = {},
                                    )
                                }
                            }
                        }
                    }
                    mainClock.advanceTimeBy(2_000)
                    waitForIdle()
                    onNode(hasText(translated(tag, Res.string.settings_playback))).assertExists()
                    onNode(hasText(english(Res.string.settings_playback))).assertDoesNotExist()
                }
            }
        }
    }

    @Test
    fun emptyStateRendersInEveryLocale() {
        locales.forEach { tag ->
            underLocale(tag) {
                runDesktopComposeUiTest(testTimeout = 30.seconds) {
                    setContent {
                        MaterialTheme(colorScheme = AppColorScheme.MELROSE.lightColor) {
                            EmptyState(
                                EmptyStateArt.Presets,
                                stringResource(Res.string.preset_empty_title),
                                stringResource(Res.string.preset_empty_body),
                            )
                        }
                    }
                    waitForIdle()
                    onNode(hasText(translated(tag, Res.string.preset_empty_title))).assertExists()
                    onNode(hasText(translated(tag, Res.string.preset_empty_body))).assertExists()
                }
            }
        }
    }

    @Test
    fun finishedTimerSpeaksTheLocaleThroughUiText() {
        locales.forEach { tag ->
            underLocale(tag) {
                runDesktopComposeUiTest(testTimeout = 30.seconds) {
                    setContent {
                        MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                            ActiveTimerContent(
                                isPlaying = false,
                                timerGoal = 300_000L,
                                timerRemaining = 0L,
                                onExtend = {},
                                onRestart = {},
                                onStop = {},
                                practiceStreak = 3,
                            )
                        }
                    }
                    mainClock.advanceTimeBy(1_000)
                    waitForIdle()
                    val headline = runBlocking { timerFinishedHeadline(5).resolve() }
                    val englishHeadline = underLocale("en-US") { runBlocking { timerFinishedHeadline(5).resolve() } }
                    assertNotEquals(englishHeadline, headline, "$tag left the finished headline in English")
                    onNode(hasText(headline)).assertExists()
                    onNode(hasText(translated(tag, Res.string.timer_stop_when_up))).assertDoesNotExist()
                }
            }
        }
    }
}
