package com.merkost.metronome.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.MotionDurationScale
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

object AppAnimations {

    private const val PressStiffness = 1600f
    private const val QuickStiffness = 1100f
    private const val StandardStiffness = 700f
    private const val EmphasizedStiffness = 340f
    private const val CalmStiffness = 190f
    private const val ExpressiveStiffness = 520f
    private const val NavigationStiffness = 400f

    private const val Flat = 1f
    private const val NearlyFlat = 0.94f
    private const val Lively = 0.66f

    fun <T> press(): SpringSpec<T> =
        SpringSpec(dampingRatio = Flat, stiffness = PressStiffness)

    fun <T> quick(): SpringSpec<T> =
        SpringSpec(dampingRatio = Flat, stiffness = QuickStiffness)

    fun <T> standard(): SpringSpec<T> =
        SpringSpec(dampingRatio = Flat, stiffness = StandardStiffness)

    fun <T> emphasized(): SpringSpec<T> =
        SpringSpec(dampingRatio = NearlyFlat, stiffness = EmphasizedStiffness)

    fun <T> calm(): SpringSpec<T> =
        SpringSpec(dampingRatio = Flat, stiffness = CalmStiffness)

    fun <T> expressive(): SpringSpec<T> =
        SpringSpec(dampingRatio = Lively, stiffness = ExpressiveStiffness)

    val Press = press<Float>()
    val Quick = quick<Float>()
    val Standard = standard<Float>()
    val Emphasized = emphasized<Float>()
    val Calm = calm<Float>()
    val Expressive = expressive<Float>()

    private val StandardOffset = SpringSpec(
        dampingRatio = Flat,
        stiffness = StandardStiffness,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    )

    private val QuickOffset = SpringSpec(
        dampingRatio = Flat,
        stiffness = QuickStiffness,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    )

    private val EmphasizedOffset = SpringSpec(
        dampingRatio = NearlyFlat,
        stiffness = EmphasizedStiffness,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    )

    private val EmphasizedSize = SpringSpec(
        dampingRatio = NearlyFlat,
        stiffness = EmphasizedStiffness,
        visibilityThreshold = IntSize.VisibilityThreshold,
    )

    val expandEnter: EnterTransition =
        expandVertically(EmphasizedSize, expandFrom = Alignment.Top) + fadeIn(Standard)

    val shrinkExit: ExitTransition =
        shrinkVertically(EmphasizedSize, shrinkTowards = Alignment.Top) + fadeOut(Quick)

    val revealEnter: EnterTransition =
        expandHorizontally(EmphasizedSize) + fadeIn(Standard)

    val concealExit: ExitTransition =
        shrinkHorizontally(EmphasizedSize) + fadeOut(Quick)

    val fadeThrough: ContentTransform =
        fadeIn(Standard).togetherWith(fadeOut(Quick))

    val fadeThroughFixedSize: ContentTransform
        get() = ContentTransform(
            fadeThrough.targetContentEnter,
            fadeThrough.initialContentExit,
            fadeThrough.targetContentZIndex,
            sizeTransform = null,
        )

    val fadeScaleTransform: ContentTransform =
        (fadeIn(Standard) + scaleIn(initialScale = 0.96f, animationSpec = Emphasized))
            .togetherWith(fadeOut(Quick) + scaleOut(targetScale = 0.98f, animationSpec = Quick))

    private val NavigationOffset = SpringSpec(
        dampingRatio = Flat,
        stiffness = NavigationStiffness,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    )

    private val NavigationFloat = SpringSpec<Float>(
        dampingRatio = Flat,
        stiffness = NavigationStiffness,
    )

    private const val RestingScreenAlpha = 0.82f
    private const val ParallaxDivisor = 4

    fun forwardNavigation(sizeTransform: SizeTransform? = SizeTransform()): ContentTransform =
        ContentTransform(
            targetContentEnter = slideInHorizontally(NavigationOffset) { it },
            initialContentExit = slideOutHorizontally(NavigationOffset) { -it / ParallaxDivisor } +
                fadeOut(NavigationFloat, targetAlpha = RestingScreenAlpha),
            targetContentZIndex = 1f,
            sizeTransform = sizeTransform,
        )

    fun backwardNavigation(sizeTransform: SizeTransform? = SizeTransform()): ContentTransform =
        ContentTransform(
            targetContentEnter = slideInHorizontally(NavigationOffset) { -it / ParallaxDivisor } +
                fadeIn(NavigationFloat, initialAlpha = RestingScreenAlpha),
            initialContentExit = slideOutHorizontally(NavigationOffset) { it },
            targetContentZIndex = 0f,
            sizeTransform = sizeTransform,
        )

    private val Unclipped = SizeTransform(clip = false)

    val forwardRoute: ContentTransform get() = forwardNavigation(Unclipped)

    val backwardRoute: ContentTransform get() = backwardNavigation(Unclipped)

    val fadeThroughRoute: ContentTransform
        get() = ContentTransform(
            fadeThrough.targetContentEnter,
            fadeThrough.initialContentExit,
            fadeThrough.targetContentZIndex,
            Unclipped,
        )

    fun slideDigitTransform(towardsUp: Boolean): ContentTransform {
        val direction = if (towardsUp) -1 else 1
        return (slideInVertically(StandardOffset) { it * -direction / 2 } + fadeIn(Standard))
            .togetherWith(slideOutVertically(QuickOffset) { it * direction / 2 } + fadeOut(Quick))
    }

    fun slideStepTransform(towardsNext: Boolean): ContentTransform {
        val direction = if (towardsNext) 1 else -1
        return (slideInHorizontally(EmphasizedOffset) { it * direction / 4 } + fadeIn(Standard))
            .togetherWith(slideOutHorizontally(EmphasizedOffset) { it * -direction / 4 } + fadeOut(Quick))
    }

    fun slideLabelTransform(towardsUp: Boolean): ContentTransform {
        val direction = if (towardsUp) -1 else 1
        return (slideInVertically(StandardOffset) { it * -direction / 3 } + fadeIn(Standard))
            .togetherWith(slideOutVertically(QuickOffset) { it * direction / 3 } + fadeOut(Quick))
    }
}

const val PressedScaleSubtle = 0.985f
const val PressedScaleSurface = 0.975f
const val PressedScaleControl = 0.94f

@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = PressedScaleControl,
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = AppAnimations.Press,
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.pressableSurface(
    onClick: () -> Unit,
    enabled: Boolean = true,
    pressedScale: Float = PressedScaleSurface,
    role: Role = Role.Button,
    rippled: Boolean = true,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = AppAnimations.Press,
        label = "surfacePressScale"
    )
    val indication = if (rippled) LocalIndication.current else null
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = indication,
            enabled = enabled,
            role = role,
            onClick = onClick,
        )
}

fun Modifier.appearScale(
    initialScale: Float = 0.72f,
): Modifier = composed {
    val scale = remember { Animatable(initialScale) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, AppAnimations.standard())
    }
    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

fun Modifier.appearIn(
    delayMillis: Int = 0,
    riseDistance: Dp = 12.dp,
): Modifier = composed {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        progress.animateTo(1f, AppAnimations.emphasized())
    }
    this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * riseDistance.toPx()
    }
}

fun Modifier.pulseOnChange(
    trigger: Int,
    peakScale: Float = 1.04f,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val last = remember { intArrayOf(trigger) }
    LaunchedEffect(trigger) {
        if (trigger != last[0]) {
            last[0] = trigger
            scale.animateTo(peakScale, AppAnimations.quick())
            scale.animateTo(1f, AppAnimations.standard())
        }
    }
    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

fun Modifier.pulseOnAppear(
    peakScale: Float = 1.12f,
): Modifier = composed {
    val scale = remember { Animatable(0.9f) }
    LaunchedEffect(Unit) {
        scale.animateTo(peakScale, AppAnimations.quick())
        scale.animateTo(1f, AppAnimations.expressive())
    }
    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

@Composable
fun AnimatedNumberText(
    value: Int,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    autoSize: TextAutoSize? = null,
) {
    var previousValue by remember { mutableIntStateOf(value) }
    val towardsUp = remember(value) { value >= previousValue }
    SideEffect { previousValue = value }
    val text = value.toString()
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier.clearAndSetSemantics { contentDescription = text }, contentAlignment = Alignment.Center) {
        val measured = measurer.measure(AnnotatedString(text), style)
        val fit = if (autoSize != null && constraints.hasBoundedWidth && measured.size.width > 0) {
            (constraints.maxWidth.toFloat() / measured.size.width).coerceIn(0.5f, 1f)
        } else 1f
        val digitStyle = style.copy(fontSize = style.fontSize * fit, fontFeatureSettings = "tnum")
        val digitWidth = with(density) {
            measurer.measure(AnnotatedString("0"), digitStyle).size.width.toDp()
        }
        Row(Modifier.animateContentSize(AppAnimations.emphasized()), verticalAlignment = Alignment.CenterVertically) {
            text.indices.forEach { index ->
                val place = text.lastIndex - index
                key(place) {
                    AnimatedContent(
                        targetState = text[index],
                        transitionSpec = { AppAnimations.slideDigitTransform(towardsUp) },
                        contentKey = { it },
                        label = "digit-$place",
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(digitWidth),
                    ) { digit ->
                        Text(text = digit.toString(), style = digitStyle, color = color, textAlign = TextAlign.Center, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AnimatedNumberTextPreview() {
    MaterialTheme {
        Box(Modifier.width(220.dp)) {
            AnimatedNumberText(
                value = 120,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 62.sp,
                ),
                modifier = Modifier.fillMaxWidth(),
                autoSize = TextAutoSize.StepBased(30.sp, 62.sp),
            )
        }
    }
}

@Composable
fun isAppMotionReduced(): Boolean =
    (rememberCoroutineScope().coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f) == 0f
