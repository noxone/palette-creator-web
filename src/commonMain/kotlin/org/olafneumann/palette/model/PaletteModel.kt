package org.olafneumann.palette.model

import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.ColorName
import org.olafneumann.palette.colors.ColorName.Companion.colorName
import org.olafneumann.palette.colors.GOLDEN_ANGLE
import org.olafneumann.palette.colors.ShadeList
import org.olafneumann.palette.colors.rotate

// @Lenses
data class PaletteModel(
    val shadeCount: Int,
    val primaryColor: Color,
    val enforcePrimaryColorInShades: Boolean,
    val accentColorSeed: Int = ACCENT_COLOR_SEED_INIT,
    val neutralColor: Color,
    val accentColors: List<Color>,
) {
    // TODO: Improve way to generate accent colors!
    private val proposedAccentColor: Color get() = (accentColors.lastOrNull() ?: primaryColor).rotate(GOLDEN_ANGLE * accentColorSeed)

    val primaryColorShadeList = ShadeList(baseColor = primaryColor, shadeCount = shadeCount, enforceColorInShades = enforcePrimaryColorInShades)
    val neutralColorShadeList = ShadeList(baseColor = neutralColor, shadeCount = shadeCount, min = 0.05, max = 0.95, enforceColorInShades = false)
    val accentColorsShadeLists = accentColors.map { ShadeList(it, shadeCount, enforceColorInShades = enforcePrimaryColorInShades) }

    val isPrimaryColorSaturationHighEnough = primaryColor.hsluv().s >= PRIMARY_MIN_SATURATION
    val isNeutralColorSaturationLowEnough = neutralColor.hsluv().s < NEUTRAL_MAX_SATURATION

    val proposedAccentColors = ColorName.entries.map { primaryColor.rotateUntil(it) }

    private fun Color.rotateUntil(colorName: ColorName): ProposedColor {
        var color = rotate(GOLDEN_ANGLE)
        while (color.colorName() != colorName) {
            color = color.rotate(GOLDEN_ANGLE)
        }
        return ProposedColor(color = color, name = colorName)
    }

    fun setPrimaryColor(primaryColor: Color, resetAccentColors: Boolean) =
        copy(
            primaryColor = primaryColor,
            accentColors = if (resetAccentColors) emptyList() else accentColors,
            accentColorSeed = if (resetAccentColors) 1 else accentColorSeed,
        )
    fun addRandomAccentColor(): PaletteModel =
        copy(accentColors = accentColors + proposedAccentColor, accentColorSeed = accentColorSeed + 1)

    // TODO: Add check that neutral color is really neutral
    // TODO: Add check that accent colors are different enough

    companion object {
        private const val ACCENT_COLOR_SEED_INIT = 1
        private const val PRIMARY_MIN_SATURATION = 0.3
        private const val NEUTRAL_MAX_SATURATION = 0.15

        data class ProposedColor(
            val color: Color,
            val name: ColorName,
        )
    }
}
