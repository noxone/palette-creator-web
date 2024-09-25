package org.olafneumann.palette.model

import dev.fritz2.core.Lenses
import org.olafneumann.palette.colorful.Color

private const val GOLDEN_ANGLE = 137.5
private const val MAX_ANGLE = 360.0

@Lenses
data class PaletteModel(
    val shadeCount: Int,
    val primaryColor: Color,
    val accentColorSeed: Int = ACCENT_COLOR_SEED_INIT,
) {
    val proposedAccentColor = primaryColor.rotate(GOLDEN_ANGLE)
    val shadedPrimaryColors: List<ShadedColor> = primaryColor.createShades(shadeCount)

    init {
        // TODO: Create nice pri
    }

    fun setPrimaryColor(primaryColor: Color) = copy(primaryColor = primaryColor, accentColorSeed = 1)

    companion object {
        private const val ACCENT_COLOR_SEED_INIT = 1

        private fun Color.createShades(amount: Int): List<ShadedColor> =
            createShades((1..amount).map { index -> 0.1 + 0.8 / amount * index }.reversed())

        private fun Color.createShades(values: List<Double>): List<ShadedColor> {
            val hsluv = HSLuv()
            return values.map { ShadedColor(color = Color.HSLuv(h = hsluv.h, s = hsluv.s, l = it), shade = it) }
        }
    }
}

data class ShadedColor(
    val color: Color,
    val shade: Double,
)

private fun Color.rotate(degrees: Double): Color {
    val hsluv = HSLuv()
    return Color.HSLuv(h = (hsluv.h + degrees).rem(MAX_ANGLE), s = hsluv.s, l = hsluv.l)
}
