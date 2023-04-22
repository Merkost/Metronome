import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.merkost.metronome.BallColor
import com.merkost.metronome.BallSize
import com.merkost.metronome.CircleSize
import com.merkost.metronome.CircleWeight

@Composable
fun MetronomeBalls(
    selectedIndex: Int,
    itemCount: Int,
    animSpec: AnimationSpec<Float>,
    arrangementSpacing: Dp = 32.dp,
    indicator: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Track how "selected" each item is [0, 1]
    val selectionFractions = remember(itemCount) {
        List(itemCount) { i ->
            Animatable(if (i == selectedIndex) 1f else 0f)
        }
    }

    selectionFractions.forEachIndexed { index, selectionFraction ->
        val target = if (index == selectedIndex) 1f else 0f
        LaunchedEffect(target, animSpec) {
            selectionFraction.animateTo(target, animSpec)
        }
    }

    // Animate the position of the indicator
    val indicatorIndex = remember { Animatable(0f) }
    val targetIndicatorIndex = selectedIndex.toFloat()
    LaunchedEffect(targetIndicatorIndex) {
        indicatorIndex.animateTo(targetIndicatorIndex, animSpec)
    }

    Layout(
        modifier = modifier,
        content = {
            content()
            Box(Modifier.layoutId("indicator"), content = indicator)
        }
    ) { measurables, constraints ->
        check(itemCount == (measurables.size - 1)) // account for indicator

        val arrangementSpacingPx = with(this) {
            arrangementSpacing.roundToPx()
        }

        // Divide the width into n+1 slots and give the selected item 2 slots
//        val width = constraints.maxWidth / (itemCount+1)
//        val selectedWidth = 2 * unselectedWidth
        val indicatorMeasurable = measurables.first { it.layoutId == "indicator" }

        val itemPlaceables = measurables
            .filterNot { it == indicatorMeasurable }
            .mapIndexed { index, measurable ->
                measurable.measure(constraints)
            }

        val width = itemPlaceables.first().width
        val indicatorPlaceable = indicatorMeasurable.measure(constraints)

        val heightRaz = (indicatorPlaceable.height - itemPlaceables.first().height) / 2
        val widthRaz = (indicatorPlaceable.width - itemPlaceables.first().width) / 2

        Log.e("minWidth", width.toString())
        Log.e("maxWidth", constraints.maxWidth.toString())

        layout(
            width = width * itemCount + arrangementSpacingPx * (itemCount),
            height = itemPlaceables.maxByOrNull { it.height }?.height ?: 0
        ) {
            Log.e("PX", arrangementSpacingPx.toString())
            val indicatorLeft =
                width * indicatorIndex.value + indicatorIndex.value * (arrangementSpacingPx)
            indicatorPlaceable.placeRelative(x = indicatorLeft.toInt(), y = 0)
            var x = 0
            itemPlaceables.forEachIndexed { index, placeable ->
                placeable.placeRelative(x = x + widthRaz, y = 0 + heightRaz)
                if (index != itemPlaceables.size - 1) {
                    x += arrangementSpacingPx
                }
                x += placeable.width
            }
        }
    }
}

@Composable
fun Ball(color: Color = BallColor) {
    Box(
        modifier = Modifier
            .size(BallSize)
            .padding(2.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun OutlinedCircle() {
    Spacer(
        modifier = Modifier
            .size(CircleSize)
            .border(CircleWeight, Color.Black, CircleShape)
    )
}