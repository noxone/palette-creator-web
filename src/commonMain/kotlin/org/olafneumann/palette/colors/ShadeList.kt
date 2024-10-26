package org.olafneumann.palette.colors

import org.olafneumann.palette.colorful.Color
import kotlin.math.abs

data class ShadeList(
    val name: String,
    val baseColor: Color,
    val shadedColors: List<ShadedColor>,
) {
    constructor(
        name: String,
        baseColor: Color,
        shadeCount: Int,
        min: Double = 0.1,
        max: Double = 0.9,
        enforceColorInShades: Boolean,
    )
            : this(
        name = name,
        baseColor = baseColor,
        shadedColors = createShades(
            forColor = baseColor,
            shadeCount = shadeCount,
            min = min,
            max = max,
            ensureColorIsIncluded = enforceColorInShades
        )
    )

    val colors: List<Color> get() = shadedColors.map { it.color }

    val lightestColor: Color get() = shadedColors.last().color

    val darkestColor: Color get() = shadedColors.first().color

    companion object {
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
            return values.map { ShadedColor(color = Color.hsluv(h = hsluv.h, s = hsluv.s, l = it), shade = it) }
        }
    }

    data class ShadedColor(
        val color: Color,
        val shade: Double,
    )
}



