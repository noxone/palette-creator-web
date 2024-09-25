package org.olafneumann.palette.model

import dev.fritz2.core.Lenses
import org.olafneumann.palette.colorful.Color

// Put your model data classes in here to use it on js and jvm side

@Lenses
data class PaletteModel(val shadeCount: Int, val color: Color) {
    companion object
}
