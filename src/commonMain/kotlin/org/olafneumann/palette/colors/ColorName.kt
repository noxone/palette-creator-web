package org.olafneumann.palette.colors

import org.olafneumann.palette.colorful.Color

enum class ColorName(val from: Double, val to: Double) {
    red(from = 310.0, to = 20.0),
    orange(from = 20.0, to = 45.0),
    yellow(from = 45.0, to = 70.0),
    green(from = 70.0, to = 155.0),
    aqua(from = 155.0, to = 185.0),
    blue(from = 185.0, to = 245.0),
    purple(from = 245.0, to = 310.0);

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
            return red
        }

        fun Color.colorName(): ColorName = fromDegree(this.Hsl().h)
    }
}