package org.olafneumann.palette.colors

import org.olafneumann.palette.colorful.Color
import kotlin.random.Random

object ColorGenerator {
     fun randomPrimary(): Color = Color.hsluv(
        h = Random.nextDouble() * 360,
        s = 0.5 + 0.5 * Random.nextDouble(),
        l = 0.5 + 0.35 * Random.nextDouble()
    )

    fun deriveNeutral(from: Color): Color {
        return Color.hsluv(h = from.hsluv().h, s = 0.05, l = 0.5)
    }

    fun randomNeutralCold(): Color = randomNeutral(ColorName.blue, ColorName.aqua, ColorName.green)

    fun randomNeutralWarm(): Color = randomNeutral(ColorName.red, ColorName.purple, ColorName.orange, ColorName.yellow)

    fun randomNeutral(vararg allowedColorNames: ColorName): Color {
        val nextH = { Random.nextDouble() * 360 }
        var h = nextH()
        if (allowedColorNames.isNotEmpty()) {
            while (allowedColorNames.none { ColorName.fromDegree(h) == it }) {
                h = nextH()
            }
        }
        return Color.hsluv(
            h = h,
            s = 0.001 + 0.1 * Random.nextDouble(),
            l = 0.5
        )
    }
}
