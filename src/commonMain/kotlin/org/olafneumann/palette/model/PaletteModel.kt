package org.olafneumann.palette.model

import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.ColorGenerator
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
    private val accentColors: List<Color>,
    private val accentNames: List<String>,
) {
    init {
        if (accentColors.size != accentNames.size) {
            throw IllegalArgumentException("accentColors size != accentNames size")
        }
    }

    // TODO: Improve way to generate accent colors!
    private val proposedAccentColor: Color get() = (accentColors.lastOrNull() ?: primaryColor).rotate(GOLDEN_ANGLE * accentColorSeed)

    val namedAccentColors = List(accentColors.size) { NamedColor(accentColors[it], accentNames[it]) }

    val primaryColorShadeList = ShadeList(baseColor = primaryColor, shadeCount = shadeCount, enforceColorInShades = enforcePrimaryColorInShades)
    val neutralColorShadeList = ShadeList(baseColor = neutralColor, shadeCount = shadeCount, min = 0.05, max = 0.95, enforceColorInShades = false)
    val accentColorsShadeLists = namedAccentColors.map { ShadeList(name = it.name, baseColor = it.color, shadeCount = shadeCount, enforceColorInShades = enforcePrimaryColorInShades) }

    val isPrimaryColorSaturationHighEnough = primaryColor.hsluv().s >= PRIMARY_MIN_SATURATION
    val isNeutralColorSaturationLowEnough = neutralColor.hsluv().s < NEUTRAL_MAX_SATURATION

    val proposedAccentColors = ColorName.entries
        .map { primaryColor.rotateUntil(it) }
        .filter { pac -> !accentColors.contains(pac.color) }
    private val proposedAccentColorName by lazy {
        var counter = accentColors.size
        var name: String
        do {
            name = "accent-${counter++}"
        } while (accentNames.contains(name))
        name
    }

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
        addAccentColor(accentColor = proposedAccentColor, increaseAccentColorSeed = true)
    fun addAccentColor(accentColor: Color, increaseAccentColorSeed: Boolean = false): PaletteModel =
        copy(
            accentColors = accentColors + accentColor,
            accentNames = accentNames + proposedAccentColorName,
            accentColorSeed = if (increaseAccentColorSeed) accentColorSeed + 1 else accentColorSeed
        )
    fun removeAccentColor(color: Color) =
        copy(accentColors = accentColors - color, accentNames = accentNames - accentNames[accentColors.indexOf(color)])

    // TODO: Add check that neutral color is really neutral
    // TODO: Add check that accent colors are different enough

    companion object {
        const val ACCENT_COLOR_SEED_INIT = 1
        private const val PRIMARY_MIN_SATURATION = 0.3
        private const val NEUTRAL_MAX_SATURATION = 0.15
        const val DEFAULT_SHADE_COUNT = 7
        const val DEFAULT_ENFORCE_PRIMARY_COLOR = true

        data class ProposedColor(
            val color: Color,
            val name: ColorName,
        )

        data class NamedColor(
            val color: Color,
            val name: String,
        )

        private const val PARAM_PRIMARY = "primary"
        private const val PARAM_NEUTRAL = "neutral"
        private const val PARAM_ACCENTS = "accents"
        private const val PARAM_ACCENT_NAMES = "accentNames"
        private const val PARAM_ACCENT_SEED = "accent_seed"
        private const val PARAM_ENFORCE_COLOR_IN_SHADE = "enforce_color"
        private const val PARAM_SHADE_COUNT = "count"

        fun parse(params: Map<String, String?>): PaletteModel {
            val primaryHex = params[PARAM_PRIMARY]
            val neutralHex = params[PARAM_NEUTRAL]
            val accentHexList = params[PARAM_ACCENTS]?.split(',') ?: emptyList()
            var accentNames = params[PARAM_ACCENT_NAMES]?.split(',') ?: emptyList()
            val accentSeed = params[PARAM_ACCENT_SEED]?.toIntOrNull() ?: ACCENT_COLOR_SEED_INIT
            val enforcePrimaryColorInShades = params[PARAM_ENFORCE_COLOR_IN_SHADE]?.toBoolean() ?: DEFAULT_ENFORCE_PRIMARY_COLOR
            val shadeCount = params[PARAM_SHADE_COUNT]?.toIntOrNull() ?: DEFAULT_SHADE_COUNT

            if (accentHexList.size != accentNames.size) {
                accentNames = List(accentHexList.size) { index -> "accent-$index" }
            }

            return PaletteModel(
                shadeCount = shadeCount,
                primaryColor = primaryHex?.let { Color.hex(it) } ?: ColorGenerator.randomPrimary(),
                enforcePrimaryColorInShades = enforcePrimaryColorInShades,
                neutralColor = neutralHex?.let { Color.hex(it) } ?: ColorGenerator.randomNeutral(),
                accentColors = accentHexList.let { it.mapNotNull { hex -> Color.hex(hex) } },
                accentNames = accentNames,
                accentColorSeed = accentSeed,
            )
        }
    }

    fun createUrl(): String {
        val map = mapOf(
            PARAM_PRIMARY to this.primaryColor.hex().substring(1),
            PARAM_NEUTRAL to this.neutralColor.hex().substring(1),
            PARAM_ACCENTS to this.accentColors.joinToString(",") { it.hex().substring(1) },
            PARAM_ACCENT_NAMES to this.accentNames.joinToString(","),
            PARAM_ACCENT_SEED to this.accentColorSeed,
        )
        return map
            .map { "${it.key}=${it.value}" }
            .joinToString(prefix = "http://localhost/?", separator = "&")
    }
}



