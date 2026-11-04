package com.merkost.metronome.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.merkost.metronome.navigation.DesktopNavigationGuard
import com.merkost.metronome.navigation.LocalDesktopNavigationGuard
import com.merkost.metronome.navigation.MainDestination
import com.merkost.metronome.screens.PracticeSetNavigationGuard
import com.merkost.metronome.viewModels.PracticeSetEditorState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class DesktopNavigationGuardRenderingTest {
    @Test
    fun sidebarKeepsDirtyEditorUntilDiscardAndMenuNavigationUsesTheSameGuard() = runDesktopComposeUiTest(
        width = 1000, height = 720, testTimeout = 20.seconds,
    ) {
        val guard = DesktopNavigationGuard()
        var selected by mutableStateOf<MainDestination>(MainDestination.PracticeSets)
        var editor by mutableStateOf<PracticeSetEditorState?>(dirtyEditor())
        var discards = 0
        val navigate: (MainDestination) -> Unit = { destination ->
            guard.navigate(selected, destination) { selected = destination }
        }
        setContent {
            CompositionLocalProvider(LocalDesktopNavigationGuard provides guard, LocalDensity provides Density(1f)) {
                MaterialTheme {
                    DesktopAppShell(selected, navigate) {
                        Box(Modifier.fillMaxSize()) {
                            if (selected == MainDestination.PracticeSets) {
                                PracticeSetNavigationGuard({ editor }) { discards++; editor = null }
                                Text(editor?.name.orEmpty())
                            }
                        }
                    }
                }
            }
        }
        onNode(hasContentDescription("Settings")).performClick()
        onNode(hasText("Discard unsaved changes?")).assertIsDisplayed()
        assertEquals(MainDestination.PracticeSets, selected)
        onNode(hasText("Cancel")).performClick()
        assertEquals("Warm up draft", editor?.name)
        assertEquals(0, discards)
        runOnIdle { navigate(MainDestination.PracticePresets) }
        onNode(hasText("Discard unsaved changes?")).assertIsDisplayed()
        onNode(hasText("Discard", substring = false)).performClick()
        waitForIdle()
        assertEquals(MainDestination.PracticePresets, selected)
        assertNull(editor)
        assertEquals(1, discards)
        runOnIdle { navigate(MainDestination.Main) }
        assertEquals(MainDestination.Main, selected)
    }

    @Test
    fun savingEditorRejectsNavigationAndSuccessfulSaveReleasesIt() = runDesktopComposeUiTest(
        width = 1000, height = 720, testTimeout = 20.seconds,
    ) {
        val guard = DesktopNavigationGuard()
        var selected by mutableStateOf<MainDestination>(MainDestination.PracticeSets)
        var editor by mutableStateOf<PracticeSetEditorState?>(dirtyEditor().copy(isSaving = true))
        var discards = 0
        val navigate: (MainDestination) -> Unit = { destination ->
            guard.navigate(selected, destination) { selected = destination }
        }
        setContent {
            CompositionLocalProvider(LocalDesktopNavigationGuard provides guard, LocalDensity provides Density(1f)) {
                MaterialTheme {
                    DesktopAppShell(selected, navigate) {
                        PracticeSetNavigationGuard({ editor }) { discards++; editor = null }
                    }
                }
            }
        }
        onNode(hasContentDescription("Settings")).performClick()
        runOnIdle { navigate(MainDestination.Main) }
        assertEquals(MainDestination.PracticeSets, selected)
        assertEquals(true, editor?.isSaving)
        assertEquals(0, discards)
        runOnIdle { editor = null }
        runOnIdle { navigate(MainDestination.Settings) }
        assertEquals(MainDestination.Settings, selected)
        assertEquals(0, discards)
    }

    @Test
    fun applicationExitWaitsForSaveAndExplicitDraftDiscard() = runDesktopComposeUiTest(
        width = 1000, height = 720, testTimeout = 20.seconds,
    ) {
        val guard = DesktopNavigationGuard()
        var editor by mutableStateOf<PracticeSetEditorState?>(dirtyEditor().copy(isSaving = true))
        var exits = 0
        setContent {
            CompositionLocalProvider(LocalDesktopNavigationGuard provides guard, LocalDensity provides Density(1f)) {
                MaterialTheme {
                    Box(Modifier.fillMaxSize()) {
                        PracticeSetNavigationGuard({ editor }) { editor = null }
                        Text(editor?.name.orEmpty())
                    }
                }
            }
        }
        runOnIdle { guard.leave(MainDestination.PracticeSets) { exits++ } }
        assertEquals(0, exits)
        runOnIdle {
            editor = dirtyEditor()
            guard.leave(MainDestination.PracticeSets) { exits++ }
        }
        onNode(hasText("Discard unsaved changes?")).assertIsDisplayed()
        onNode(hasText("Cancel")).performClick()
        assertEquals(0, exits)
        assertEquals("Warm up draft", editor?.name)
        runOnIdle { guard.leave(MainDestination.PracticeSets) { exits++ } }
        onNode(hasText("Discard", substring = false)).performClick()
        waitForIdle()
        assertEquals(1, exits)
        assertNull(editor)
    }

    private fun dirtyEditor() = PracticeSetEditorState(
        sourceId = null,
        expectedUpdatedAtEpochMillis = null,
        name = "Warm up draft",
        steps = emptyList(),
        hasUnsavedChanges = true,
    )
}
