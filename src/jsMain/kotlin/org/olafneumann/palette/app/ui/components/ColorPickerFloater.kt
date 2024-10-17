package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.Handler
import dev.fritz2.core.RenderContext
import dev.fritz2.core.id
import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.fittingFontColor
import org.olafneumann.palette.model.PaletteModel

fun RenderContext.createColorPickerFloater(id: String, proposedColors: List<PaletteModel.Companion.ProposedColor>, handler: Handler<Color>) =
    div {
        id(id)
        div {
            className("shadow-xl z-10 inline-block w-48 text-sm text-gray-500 transition-opacity duration-300 bg-white border border-gray-200 rounded-lg shadow-sm dark:text-gray-400 dark:border-gray-600 dark:bg-gray-800")

            div {
                proposedColors.forEach { color ->
                    div {
                        className("w-full h-12 p-1")
                        colorBox(
                            color = color.color,
                            textColor = color.color.fittingFontColor(
                                Color(1.0, 1.0, 1.0),
                                Color(0.0, 0.0, 0.0)
                            ),
                            textToRender = "${color.name.name}: {{hex}}",
                            handler = handler,
                        )
                    }
                }
            }
        }
    }