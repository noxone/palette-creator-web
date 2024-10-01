package org.olafneumann.palette.colors

import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colorful.RGB
import kotlin.math.roundToInt

private fun RGB.relativeLuminance(): Double =
    0.2126 * r + 0.7152 * g + 0.0722 * b

fun Color.contrast(to: Color): Double {
    val c1 = LinearRgb().relativeLuminance()
    val c2 = to.LinearRgb().relativeLuminance()

    val contrast = if (c1 > c2) {
        (c1 + 0.05) / (c2 + 0.05)
    } else {
        (c2 + 0.05) / (c1 + 0.05)
    }
    return (contrast * 10 + 0.5).roundToInt() / 10.0
}
