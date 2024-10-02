package org.olafneumann.palette.colors

import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colorful.RGB
import kotlin.math.roundToInt

// https://medium.muz.li/the-science-of-color-contrast-an-expert-designers-guide-33e84c41d156

private fun RGB.relativeLuminance(): Double =
    0.2126 * r + 0.7152 * g + 0.0722 * b

fun Color.contrast(to: Color): Double {
    val c1 = linearRgb().relativeLuminance()
    val c2 = to.linearRgb().relativeLuminance()

    val contrast = if (c1 > c2) {
        (c1 + 0.05) / (c2 + 0.05)
    } else {
        (c2 + 0.05) / (c1 + 0.05)
    }
    return (contrast * 10 + 0.5).roundToInt() / 10.0
}
