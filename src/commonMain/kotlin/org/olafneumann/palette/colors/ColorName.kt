package org.olafneumann.palette.colors

import org.olafneumann.palette.colorful.Color

enum class ColorName(val from: Double, val to: Double) {
    Red(from = 306.0, to = 14.0),
    Orange(from = 14.0, to = 42.0),
    Yellow(from = 42.0, to = 70.0),
    Green(from = 70.0, to = 167.0),
    Aqua(from = 167.0, to = 188.0),
    Blue(from = 188.0, to = 252.0),
    Purple(from = 252.0, to = 306.0);

    companion object {
        private fun into360(value: Double): Double {
            var out = value
            while (out < 0.0) {
                out += 360.0
            }
            while (out > 360.0) {
                out -= 360.0
            }
            return out
        }

        fun fromDegree(degree: Double): ColorName {
            val realDegree = into360(degree)
            for (colorName in entries) {
                if (realDegree >= colorName.from && realDegree < colorName.to) {
                    return colorName
                }
            }
            return Red
        }

        fun Color.colorName(): ColorName = fromDegree(this.hsl().h)
    }
}
