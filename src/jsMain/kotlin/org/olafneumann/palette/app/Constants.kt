package org.olafneumann.palette.app

import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.atLightness

// Header presentation
const val MAX_SCREEN_WIDTH = 1536
const val HEADER_ID = "on_header"
const val HEADER_TARGET_HUE = 290.0
const val HEADER_BASE_SATURATION = 0.1
const val HEADER_ADD_SATURATION = 0.85
const val HEADER_LIGHTNESS = 0.7

// Configuration
const val SHADES_MIN = 5
const val SHADES_MAX = 15

// Optics
const val MAX_LIGHTNESS = 0.95
const val MIN_LIGHTNESS = 0.05
//const val PRIMARY_COLOR_HEX = "#57A0CC"
const val NEUTRAL_COLOR_HEX = "#75777A"
//val PRIMARY_COLOR = Color.hex(PRIMARY_COLOR_HEX)!!
val NEUTRAL_COLOR = Color.hex(NEUTRAL_COLOR_HEX)!!
val LIGHT_TEXT_COLOR = NEUTRAL_COLOR.atLightness(MAX_LIGHTNESS)
val DARK_TEXT_COLOR = NEUTRAL_COLOR.atLightness(MIN_LIGHTNESS)

