package org.olafneumann.palette.colors

import org.olafneumann.palette.colorful.Color

const val GOLDEN_ANGLE = 137.5
private const val MAX_ANGLE = 360.0
private const val LIGHT_FONT_THRESHOLD = 0.65

fun Color.rotate(degrees: Double): Color {
    val hsluv = hsluv()
    return Color.hsluv(h = (hsluv.h + degrees).rem(MAX_ANGLE), s = hsluv.s, l = hsluv.l)
}

fun Color.fittingFontColor(light: Color, dark: Color): Color =
    if (hsluv().l < LIGHT_FONT_THRESHOLD) light else dark
