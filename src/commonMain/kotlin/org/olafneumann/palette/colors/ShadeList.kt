package org.olafneumann.palette.colors

import org.olafneumann.palette.colorful.Color
import kotlin.math.abs
import kotlin.math.roundToInt

@Suppress("MagicNumber")
private val predefinedShades = mapOf(
    5 to listOf(0.1, 0.3, 0.5, 0.7, 0.9),
    6 to listOf(0.1, 0.3, 0.4, 0.6, 0.7, 0.9),
    7 to listOf(0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8),
    8 to listOf(0.1, 0.2, 0.3, 0.4, 0.6, 0.7, 0.8, 0.9),
    9 to listOf(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9),
    10 to listOf(0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9),
    11 to listOf(0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95),
    12 to listOf(0.05, 0.1, 0.15, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95),
    13 to listOf(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95),
    14 to listOf(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95),
    15 to listOf(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.40, 0.5, 0.6, 0.7, 0.8, 0.85, 0.9, 0.95),
)

enum class ShadeType(
    val title: String,
    val description: String,
) {
    Even(
        title = "Evenly spread",
        description = "The values for the shades will be evenly spread."
    ),
    Enforced(
        title = "Enforced colors",
        description = "Quite similar to \"evenly spread\", but it will make sure that your chosen base colors are " +
                "part of the generated shades. All shades around these base colors are again evenly spread. " +
                "The color that is closest to your selected color will be replaced."
    ),
    Predefined(
        title = "Predefined Values",
        description = "The values for the shades are predefined by the page author. The for those predefined values " +
                "was to create those shades that are most commonly used."
    );

    companion object {
        private fun of(value: String?): ShadeType? =
            value?.let { name -> return entries.find { it.name.lowercase() == name.lowercase() } }

        fun String?.toShadeType(default: ShadeType = Even): ShadeType =
            of(this) ?: default
    }
}

data class ShadeList(
    val name: String,
    val baseColor: Color,
    val shadedColors: List<ShadedColor>,
    val shadeType: ShadeType,
) {
    constructor(
        name: String,
        baseColor: Color,
        shadeCount: Int,
        shadeType: ShadeType,
    )
            : this(
        name = name,
        baseColor = baseColor,
        shadedColors = when (shadeType) {
            ShadeType.Even -> createShades(
                forColor = baseColor,
                shadeCount = shadeCount,
                ensureColorIsIncluded = false,
                min = 1.0 / (shadeCount + 1),
                max = 1.0 - 1.0 / (shadeCount + 1),
            )

            ShadeType.Enforced -> createShades(
                forColor = baseColor,
                shadeCount = shadeCount,
                ensureColorIsIncluded = true,
                min = 1.0 / (shadeCount + 1),
                max = 1.0 - 1.0 / (shadeCount + 1),
            )

            ShadeType.Predefined -> getPredefinedShades(
                forColor = baseColor,
                count = shadeCount
            )
        },
        shadeType = shadeType,
    )

    val colors: List<Color> get() = shadedColors.map { it.color }

    val lightestColor: Color get() = shadedColors.last().color

    val darkestColor: Color get() = shadedColors.first().color

    companion object {
        fun getPredefinedShades(
            forColor: Color,
            count: Int,
        ) = createShades(
            forColor = forColor,
            values = predefinedShades[count] ?: emptyList(),
        )

        fun createShades(
            forColor: Color,
            shadeCount: Int,
            min: Double = 0.1,
            max: Double = 0.9,
            ensureColorIsIncluded: Boolean,
        ): List<ShadedColor> {
            if (shadeCount <= 0) {
                return emptyList()
            } else if (shadeCount == 1) {
                return createShades(forColor = forColor, values = listOf((min + max) * 0.5))
            }

            val span = max - min
            val step = span / (shadeCount - 1)
            val computedShades = (0..<shadeCount).map { index -> min + step * index }

            if (!ensureColorIsIncluded) {
                return createShades(
                    forColor = forColor,
                    values = computedShades
                )
            }

            val lightness = forColor.hsluv().l
            val closestIndex = computedShades.mapIndexed { index, d -> index to abs(lightness - d) }
                .minBy { it.second }
                .first
            val lowerShades = createShades(
                forColor = forColor,
                shadeCount = closestIndex + 1,
                min = min,
                max = lightness,
                ensureColorIsIncluded = false,
            )
            val upperShades = createShades(
                forColor = forColor,
                shadeCount = shadeCount - closestIndex,
                min = lightness,
                max = max,
                ensureColorIsIncluded = false,
            ).drop(1)
            return lowerShades + upperShades
        }

        private fun createShades(forColor: Color, values: List<Double>): List<ShadedColor> {
            val hsluv = forColor.hsluv()
            return values.map { ShadedColor(color = Color.hsluv(h = hsluv.h, s = hsluv.s, l = it), shade = 1.0 - it) }
        }
    }

    data class ShadedColor(
        val color: Color,
        val shade: Double,
    ) {
        val intShade = (shade * 1000).roundToInt()
    }
}



