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

    val primaryColorShadeList = ShadeList(baseColor = primaryColor, shadeCount = shadeCount)// primaryColor.createShades(shadeCount)
    val neutralColorShadeList = ShadeList(neutralColor, shadeCount = shadeCount, min = 0.05, max = 0.9) //: List<ShadedColor> = neutralColor.createShades(shadeCount, min = 0.05, max = 0.95)
    val accentColorsShadeLists = accentColors.map { ShadeList(it, shadeCount) } //:List<List<ShadedColor>> = accentColors.map { it.createShades(shadeCount) }

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
    }
}

data class ShadeList(
    val baseColor: Color,
    val shades: List<ShadedColor>
) {
    constructor(baseColor: Color, shadeCount: Int, min: Double = 0.1, max: Double = 0.9)
            : this(baseColor = baseColor, shades = baseColor.createShades(amount = shadeCount, min = min, max = max))

    val colors: List<Color> get() = shades.map { it.color }

    val lightestColor: Color get() = shades.first().color

    val darkestColor: Color get() = shades.last().color

    val isEmpty: Boolean get() = shades.isEmpty()
    val isNotEmpty: Boolean get() = !isEmpty
}

data class ShadedColor(
    val color: Color,
    val shade: Double,
)

private fun Color.rotate(degrees: Double): Color {
    val hsluv = hsluv()
    return Color.hsluv(h = (hsluv.h + degrees).rem(MAX_ANGLE), s = hsluv.s, l = hsluv.l)
}

private fun Color.createShades(amount: Int, min: Double = 0.1, max: Double = 0.9): List<ShadedColor> =
    createShades((1..amount).map { index -> min + (max - min) / amount * index }.reversed())

private fun Color.createShades(values: List<Double>): List<ShadedColor> {
    val hsluv = hsluv()
    return values.map { ShadedColor(color = Color.hsluv(h = hsluv.h, s = hsluv.s, l = it), shade = it) }
}

private object IdGenerator {
    private var current = 0
    fun next(): Int = current++
}
