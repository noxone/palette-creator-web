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
    val neutralColor: Color,
    val accentColors: List<Color>,
) {
    // TODO: Improve way to generate accent colors!
    private val proposedAccentColor = (accentColors.lastOrNull() ?: primaryColor).rotate(GOLDEN_ANGLE * accentColorSeed)

    val shadedPrimaryColors: List<ShadedColor> = primaryColor.createShades(shadeCount)
    val shadedNeutralColors: List<ShadedColor> = neutralColor.createShades(shadeCount, min = 0.05, max = 0.95)
    val shadedAccentColors:List<List<ShadedColor>> = accentColors.map { it.createShades(shadeCount) }

    fun setPrimaryColor(primaryColor: Color, resetAccentColors: Boolean) =
        copy(
            primaryColor = primaryColor,
            accentColors = if (resetAccentColors) emptyList() else accentColors,
            accentColorSeed = if (resetAccentColors) 1 else accentColorSeed,
        )
    fun addRandomAccentColor(): PaletteModel =
        copy(accentColors = accentColors + proposedAccentColor, accentColorSeed = accentColorSeed + 1)

    companion object {
        private const val ACCENT_COLOR_SEED_INIT = 1

        private fun Color.createShades(amount: Int, min: Double = 0.1, max: Double = 0.9): List<ShadedColor> =
            createShades((1..amount).map { index -> min + (max - min) / amount * index }.reversed())

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
