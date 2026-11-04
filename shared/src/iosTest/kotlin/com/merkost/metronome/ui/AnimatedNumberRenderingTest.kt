package com.merkost.metronome.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.ui.theme.Typography
import kotlinx.coroutines.test.TestResult
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class AnimatedNumberRenderingTest {
    @Test
    fun rtlKeepsTheActualNumberRasterAndWholeAccessibleValueInLtrOrder(): TestResult = numberTest { model ->
        listOf(80, 128).forEach { value ->
            runOnUiThread { model.value.intValue = value; model.direction.value = LayoutDirection.Ltr }
            settle()
            val ltr = raster()
            val geometry = numberBounds()
            assertWholeValue(value)
            runOnUiThread { model.direction.value = LayoutDirection.Rtl }
            settle()
            assertWholeValue(value)
            assertEquals(geometry, numberBounds(), "Changing direction must preserve the counter frame")
            assertRasterEquals(ltr, raster(), "RTL must paint$value in the same left-to-right order")
            val before = onNodeWithTag("beforeNumber").fetchSemanticsNode().boundsInRoot
            val after = onNodeWithTag("afterNumber").fetchSemanticsNode().boundsInRoot
            assertTrue(before.left > after.right, "The surrounding row must retain RTL placement")
            onNode(hasText("Context Rtl")).assertIsDisplayed()
        }
    }

    @Test
    fun changingOnePlaceKeepsUnchangedDigitPixelsAndGeometryStillThroughTheTransition(): TestResult = numberTest(initialValue = 128, initialDirection = LayoutDirection.Rtl) { model ->
        val initial = raster()
        val geometry = numberBounds()
        val groups = inkGroups(initial)
        assertEquals(3, groups.size, "The baseline must paint three separate digits")
        val unchangedEnd = groups[1].last + 1
        val changingStart = groups[2].first
        assertWholeValue(128)
        runOnUiThread { model.value.intValue = 129 }
        val sampledFrames = linkedMapOf<Long, ImageBitmap>()
        var elapsed = 0L
        listOf(32L, 64L, 96L, 128L).forEach { time ->
            advance(time - elapsed)
            assertWholeValue(129)
            assertEquals(geometry, numberBounds(), "Changing one digit must preserve the counter frame at$time ms")
            val sample = raster()
            assertRegionEquals(initial, sample, 0, unchangedEnd, "Unchanged prefix at$time ms")
            sampledFrames[time] = sample
            elapsed = time
        }
        settle()
        assertWholeValue(129)
        assertRegionEquals(initial, raster(), 0, unchangedEnd, "Unchanged prefix after the transition")
        val rtlFinal = raster()
        listOf(64L, 96L, 128L).forEach { time ->
            val sample = sampledFrames.getValue(time)
            val fromInitial = regionDifference(initial, sample, changingStart, initial.width)
            val fromSettled = regionDifference(rtlFinal, sample, changingStart, initial.width)
            assertTrue(fromInitial > 0.005f, "The changed place must differ from its initial raster at$time ms. difference=$fromInitial")
            assertTrue(fromSettled > 0.005f, "The changed place must paint an intermediate frame rather than an instant replacement at$time ms. difference=$fromSettled")
        }
        runOnUiThread { model.direction.value = LayoutDirection.Ltr }
        settle()
        assertRasterEquals(rtlFinal, raster(), "Settled129 raster must agree across layout directions")
    }

    private fun numberTest(
        initialValue: Int = 80,
        initialDirection: LayoutDirection = LayoutDirection.Ltr,
        block: suspend ComposeUiTest.(NumberFixtureModel) -> Unit,
    ): TestResult = runSkikoComposeUiTest(
        size = Size(320f, 160f),
        effectContext = object : MotionDurationScale { override val scaleFactor = 1f },
        testTimeout = 30.seconds,
    ) {
        mainClock.autoAdvance = false
        val model = NumberFixtureModel(initialValue, initialDirection)
        setContent { NumberFixture(model) }
        settle()
        block(model)
    }

    private fun ComposeUiTest.assertWholeValue(value: Int) {
        onAllNodes(hasContentDescription(value.toString())).assertCountEquals(1)
        onNode(hasContentDescription(value.toString())).assertIsDisplayed()
        value.toString().forEach { digit -> onAllNodes(hasText(digit.toString())).assertCountEquals(0) }
    }

    private fun ComposeUiTest.numberBounds(): Rect = onNodeWithTag("numberFrame").fetchSemanticsNode().boundsInRoot
    private fun ComposeUiTest.raster(): ImageBitmap = onNodeWithTag("numberFrame").captureToImage()

    private fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }

    private fun ComposeUiTest.settle() = advance(2_000)

    private fun assertRasterEquals(expected: ImageBitmap, actual: ImageBitmap, stage: String) {
        assertEquals(expected.width, actual.width, stage)
        assertEquals(expected.height, actual.height, stage)
        assertRegionEquals(expected, actual, 0, expected.width, stage)
    }

    private fun assertRegionEquals(expected: ImageBitmap, actual: ImageBitmap, start: Int, end: Int, stage: String) {
        assertTrue(regionDifference(expected, actual, start, end) < 0.00001f, "$stage must preserve every painted pixel")
    }

    private fun regionDifference(expected: ImageBitmap, actual: ImageBitmap, start: Int, end: Int): Float {
        val first = expected.toPixelMap()
        val second = actual.toPixelMap()
        var difference = 0f
        for (y in 0 until expected.height) for (x in start until end) {
            difference += abs(first[x, y].red - second[x, y].red) + abs(first[x, y].green - second[x, y].green) + abs(first[x, y].blue - second[x, y].blue) + abs(first[x, y].alpha - second[x, y].alpha)
        }
        return difference / ((end - start) * expected.height * 4f)
    }

    private fun inkGroups(image: ImageBitmap): List<IntRange> {
        val pixels = image.toPixelMap()
        val backdrop = pixels[0, 0]
        val groups = mutableListOf<IntRange>()
        var start = -1
        for (x in 0 until image.width) {
            val ink = (0 until image.height).any { y ->
                val pixel = pixels[x, y]
                maxOf(abs(pixel.red - backdrop.red), abs(pixel.green - backdrop.green), abs(pixel.blue - backdrop.blue)) > 0.15f
            }
            if (ink && start < 0) start = x
            if (!ink && start >= 0) { groups += start until x; start = -1 }
        }
        if (start >= 0) groups += start until image.width
        return groups
    }
}

private class NumberFixtureModel(initialValue: Int, initialDirection: LayoutDirection) {
    val value = mutableIntStateOf(initialValue)
    val direction = mutableStateOf(initialDirection)
}

@Composable
private fun NumberFixture(model: NumberFixtureModel) {
    CompositionLocalProvider(LocalDensity provides Density(1f), LocalLayoutDirection provides model.direction.value) {
        MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor, typography = Typography) {
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.size(24.dp).testTag("beforeNumber"))
                    Box(Modifier.weight(1f).height(96.dp).testTag("numberFrame")) {
                        AnimatedNumberText(
                            value = model.value.intValue,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 62.sp, fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Box(Modifier.size(24.dp).testTag("afterNumber"))
                }
                Text("Context ${LocalLayoutDirection.current}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
