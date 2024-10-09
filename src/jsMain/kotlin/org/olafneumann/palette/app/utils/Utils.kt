package org.olafneumann.palette.app.utils

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.format(digits: Int) =
    ((10.0.pow(digits) * this).roundToInt().toDouble() / 10.0.pow(digits)).toString()
