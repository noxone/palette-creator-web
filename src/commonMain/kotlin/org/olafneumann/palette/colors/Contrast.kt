package org.olafneumann.palette.colors

import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colorful.RGB
import kotlin.math.roundToInt

// https://medium.muz.li/the-science-of-color-contrast-an-expert-designers-guide-33e84c41d156

private const val ROUND = 0.5
private const val TEN = 10

private const val FACTOR_RED = 0.2126
private const val FACTOR_GREEN = 0.7152
private const val FACTOR_BLUE = 0.0722

private const val SUMMAND_CONTRAST = 0.05

private fun RGB.relativeLuminance(): Double =
    FACTOR_RED * r + FACTOR_GREEN * g + FACTOR_BLUE * b

fun Color.contrast(to: Color): Double {
    val c1 = linearRgb().relativeLuminance()
    val c2 = to.linearRgb().relativeLuminance()

    val contrast = if (c1 > c2) {
        (c1 + SUMMAND_CONTRAST) / (c2 + SUMMAND_CONTRAST)
    } else {
        (c2 + SUMMAND_CONTRAST) / (c1 + SUMMAND_CONTRAST)
    }
    return (contrast * TEN + ROUND).roundToInt() / TEN.toDouble()
}
