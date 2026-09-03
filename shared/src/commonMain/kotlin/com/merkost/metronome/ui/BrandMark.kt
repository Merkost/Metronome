package com.merkost.metronome.ui

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser

private const val BRAND_MARK_VIEWPORT = 7654f
private const val BRAND_MARK_GROUP_SCALE = 0.58f
private const val BRAND_MARK_GROUP_TRANSLATE = 1607.34f

private val BRAND_MARK_SUBPATHS = listOf(
    "M5305.1,2767.8L6117,1414.7l-468.4,-281.6l-662.4,1104.4l-304.3,-507.5c-266.7,-443.9 -842.8,-587.4 -1286.6,-320.7c-131.5,79 -241.7,189.2 -320.7,320.7l-1770.4,2955c-317.5,529 -146.1,1215.2 382.8,1532.8c173.6,104.2 372.3,159.3 574.8,159.3h3232.9c616.6,-1 1116.1,-500.6 1117.1,-1117.1v-10.9c-0.2,-203 -55.6,-402 -160.4,-575.9L5305.1,2767.8z",
    "M3543.8,2014.2c111.4,-184.9 351.6,-244.6 536.5,-133.2c54.6,32.9 100.3,78.6 133.2,133.2l455.6,754.5L3595.7,4554.6H2019.4L3543.8,2014.2z",
    "M4987.1,3299l755.4,1255.6H4233.6L4987.1,3299z",
    "M6065.1,5259.9c0,315 -255.4,570.4 -570.4,570.4l0,0H2261.7c-314.5,0 -569.5,-255 -569.5,-569.5c0,-54 7.7,-107.7 22.8,-159.5h4330c13,47.8 19.8,97.1 20,146.7V5259.9z",
)

/**
 * The brand mark, as separate subpaths so they can be stroked on in sequence.
 * Coordinates are normalised into a 0..1 square; scale by the drawing size.
 */
fun brandMarkSubpaths(size: Float): List<Path> = BRAND_MARK_SUBPATHS.map { data ->
    val path = PathParser().parsePathString(data).toPath()
    val matrix = androidx.compose.ui.graphics.Matrix().apply {
        val unit = size / BRAND_MARK_VIEWPORT
        translate(BRAND_MARK_GROUP_TRANSLATE * unit, BRAND_MARK_GROUP_TRANSLATE * unit)
        scale(unit * BRAND_MARK_GROUP_SCALE, unit * BRAND_MARK_GROUP_SCALE)
    }
    Path().apply { addPath(path) }.also { it.transform(matrix) }
}

fun brandMarkFilled(size: Float): Path {
    val combined = Path()
    brandMarkSubpaths(size).forEach { combined.addPath(it) }
    return combined
}
