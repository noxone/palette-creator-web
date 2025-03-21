package org.olafneumann.palette.model

import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.ColorGenerator
import org.olafneumann.palette.colors.ColorName
import org.olafneumann.palette.colors.ColorName.Companion.colorName
import org.olafneumann.palette.colors.GOLDEN_ANGLE
import org.olafneumann.palette.colors.ShadeList
import org.olafneumann.palette.colors.ShadeType
import org.olafneumann.palette.colors.ShadeType.Companion.toShadeType
import org.olafneumann.palette.colors.rotate

// @Lenses
data class PaletteModel(
    val shadeCount: Int,
    val shadeType: ShadeType,
    val primaryColor: Color,
    val accentColorSeed: Int = ACCENT_COLOR_SEED_INIT,
    val neutralColor: Color,
    private val accentColors: List<Color>,
    private var accentNames: List<String>,
) {
    init {
        if (accentColors.size != accentNames.size) {
            accentNames = (1..accentColors.size).map { generateAccentColorName(it) }
        }
    }

    // TODO: Improve way to generate accent colors!
    val proposedAccentColor: Color
        get() = (accentColors.lastOrNull() ?: primaryColor).rotate(GOLDEN_ANGLE * accentColorSeed)

    val namedAccentColors = List(accentColors.size) { NamedColor(accentColors[it], accentNames[it]) }

    val primaryColorShadeList = ShadeList(
        name = "primary",
        baseColor = primaryColor,
        shadeCount = shadeCount,
        shadeType = shadeType
    )
    val neutralColorShadeList = ShadeList(
        name = "neutral",
        baseColor = neutralColor,
        shadeCount = shadeCount,
        shadeType = shadeType
    )
    val accentColorsShadeLists = namedAccentColors.map {
        ShadeList(
            name = it.name,
            baseColor = it.color,
            shadeCount = shadeCount,
            shadeType = shadeType
        )
    }

    val isPrimaryColorSaturationHighEnough = primaryColor.hsluv().s >= PRIMARY_MIN_SATURATION
    val isNeutralColorSaturationLowEnough = neutralColor.hsluv().s < NEUTRAL_MAX_SATURATION

    val proposedAccentColors = ColorName.entries
            .mapNotNull { primaryColor.rotateUntil(it) }
            .filter { pac -> !accentColors.contains(pac.color) }

    private val proposedAccentColorName by lazy {
        var counter = accentColors.size
        var name: String
        do {
            name = generateAccentColorName(counter++)
        } while (accentNames.contains(name))
        name
    }

    private fun generateAccentColorName(index: Int) = "accent-$index"

    private fun Color.rotateUntil(colorName: ColorName): ProposedColor? {
        var rotationCounter = MAX_COUNTER // prevent endless looping
        var color = rotate(GOLDEN_ANGLE)
        while (color.colorName() != colorName && rotationCounter-- > 0) {
            color = color.rotate(GOLDEN_ANGLE)
        }
        if (rotationCounter > 0)
            return ProposedColor(color = color, colorName = colorName)
        return null
    }

    fun setPrimaryColor(primaryColor: Color, resetAccentColors: Boolean) =
        copy(
            primaryColor = primaryColor,
            accentColors = if (resetAccentColors) emptyList() else accentColors,
            accentNames = if (resetAccentColors) emptyList() else accentNames,
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

    private fun removeAccentColor(color: Color) =
        copy(accentColors = accentColors - color, accentNames = accentNames - accentNames[accentColors.indexOf(color)])

    fun removeAccentColor(name: String) =
        removeAccentColor(accentColors[accentNames.indexOf(name)])

    fun renameAccentColor(oldName: String, newName: String): PaletteModel {
        val names = accentNames.toMutableList()
        val index = names.indexOf(oldName)
        names[index] = newName
        return copy(accentNames = names)
    }

    // TODO: Add check that neutral color is really neutral
    // TODO: Add check that accent colors are different enough

    companion object {
        private const val MAX_COUNTER = 300
        const val ACCENT_COLOR_SEED_INIT = 1
        private const val PRIMARY_MIN_SATURATION = 0.3
        private const val NEUTRAL_MAX_SATURATION = 0.15
        private const val DEFAULT_SHADE_COUNT = 7
        private val DEFAULT_SHADE_TYPE = ShadeType.Even

        data class ProposedColor(
            val color: Color,
            val colorName: ColorName,
        ) {
            val name = colorName.name
        }

        data class NamedColor(
            val color: Color,
            val name: String,
        )

        private const val PARAM_PRIMARY = "primary"
        private const val PARAM_NEUTRAL = "neutral"
        private const val PARAM_ACCENTS = "accents"
        private const val PARAM_ACCENT_NAMES = "accent_names"
        private const val PARAM_ACCENT_SEED = "accent_seed"
        private const val PARAM_SHADE_TYPE = "type"
        private const val PARAM_SHADE_COUNT = "count"

        private fun String.emptyToNull(): String? =
            if (isNullOrEmpty())
                null
            else
                this

        fun parse(params: Map<String, String?>): PaletteModel {
            val primaryHex = params[PARAM_PRIMARY]
            val neutralHex = params[PARAM_NEUTRAL]
            val accentHexList = params[PARAM_ACCENTS]?.split(',') ?: emptyList()
            var accentNames = params[PARAM_ACCENT_NAMES]?.split(',') ?: emptyList()
            val accentSeed = params[PARAM_ACCENT_SEED]?.toIntOrNull() ?: ACCENT_COLOR_SEED_INIT
            val shadeType = params[PARAM_SHADE_TYPE]?.emptyToNull().toShadeType(default = DEFAULT_SHADE_TYPE)
            val shadeCount = params[PARAM_SHADE_COUNT]?.toIntOrNull() ?: DEFAULT_SHADE_COUNT

            if (accentHexList.size != accentNames.size) {
                accentNames = List(accentHexList.size) { index -> "accent-$index" }
            }

            return PaletteModel(
                shadeCount = shadeCount,
                shadeType = shadeType,
                primaryColor = primaryHex?.let { Color.hex(it) } ?: ColorGenerator.randomPrimary(),
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
            PARAM_SHADE_COUNT to this.shadeCount,
            PARAM_SHADE_TYPE to this.shadeType.name.lowercase(),
        )
        return map
            .map { "${it.key}=${it.value}" }
            .joinToString(prefix = "http://localhost/?", separator = "&")
    }
}



