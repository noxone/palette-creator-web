package org.olafneumann.palette.colors

import org.olafneumann.palette.colorful.Color

const val GOLDEN_ANGLE = 137.5
private const val MAX_ANGLE = 360.0

fun Color.rotate(degrees: Double): Color {
    val hsluv = hsluv()
    return Color.hsluv(h = (hsluv.h + degrees).rem(MAX_ANGLE), s = hsluv.s, l = hsluv.l)
}