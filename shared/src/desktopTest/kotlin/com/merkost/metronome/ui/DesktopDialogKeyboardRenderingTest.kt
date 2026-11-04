package com.merkost.metronome.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.AppTextField
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppPresentation
import com.merkost.metronome.navigation.LocalAppViewportSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class DesktopDialogKeyboardRenderingTest {
    @Test
    fun escapeDismissesAFreshDialogThroughItsAnimatedExit() = runDesktopComposeUiTest(width = 800, height = 700, testTimeout = 20.seconds) {
        var visible by mutableStateOf(true)
        var dismissals = 0
        setContent {
            CompositionLocalProvider(
                LocalAppPresentation provides AppPresentation.Desktop,
                LocalAppViewportSize provides DpSize(800.dp, 700.dp),
            ) {
                MaterialTheme {
                    if (visible) AppBottomSheet("Practice data", { dismissals++; visible = false }) {
                        Text("Your practice statistics")
                    }
                }
            }
        }
        waitForIdle()
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Practice data")).performKeyInput { pressKey(Key.Escape) }
        waitForIdle()
        assertEquals(1, dismissals, "Escape must dismiss an untouched desktop dialog")
    }

    @Test
    fun initialDialogFocusDoesNotTakeFocusFromItsTextEditor() = runDesktopComposeUiTest(width = 800, height = 700, testTimeout = 20.seconds) {
        var visible by mutableStateOf(true)
        var value by mutableStateOf("")
        var dismissals = 0
        setContent {
            CompositionLocalProvider(
                LocalAppPresentation provides AppPresentation.Desktop,
                LocalAppViewportSize provides DpSize(800.dp, 700.dp),
            ) {
                MaterialTheme {
                    if (visible) AppBottomSheet("Edit tempo", { dismissals++; visible = false }) {
                        val focus = remember { FocusRequester() }
                        AppTextField(value, { value = it }, modifier = Modifier.focusRequester(focus).testTag("dialogEditor"), label = "Name")
                        LaunchedEffect(Unit) { focus.requestFocus() }
                    }
                }
            }
        }
        waitForIdle()
        onNodeWithTag("dialogEditor").assertIsFocused().performKeyInput { pressKey(Key.Spacebar) }
        waitForIdle()
        onNodeWithTag("dialogEditor").assertIsFocused().performTextInput("Practice A")
        waitForIdle()
        assertEquals("Practice A", value)
        assertEquals(0, dismissals)
        onNodeWithTag("dialogEditor").performKeyInput { pressKey(Key.Escape) }
        waitForIdle()
        assertEquals(1, dismissals)
    }
}
