package org.olafneumann.palette.app.ui

import dev.fritz2.core.Handler
import dev.fritz2.core.RenderContext
import dev.fritz2.core.id
import dev.fritz2.core.title
import kotlinx.coroutines.flow.map
import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.ShadeList


fun RenderContext.colorList(
    width: Double,
    height: Double,
    colors: List<ShadeList.ShadedColor>,
    handler: Handler<Color>? = null
) =
    div {
        className("flex flex-row justify-around justify-items-center")
        colors.forEach { color ->
            colorBox(
                width = width,
                height = height,
                color = color.color,
                handler = handler
            )
        }
    }


fun RenderContext.colorList(
    width: Double,
    height: Double,
    colors: List<Color>
) =
    div {
        className("flex flex-row justify-around justify-items-center")
        colors.forEach { color ->
            colorBox(width = width, height = height, color = color)
        }
    }

fun RenderContext.colorBox(
    id: String? = null,
    color: Color,
    textColor: Color? = null,
    textToRender: String? = null,
    width: Double? = null,
    height: Double? = null,
    handler: Handler<Color>? = null,
) =
    div {
        val bigBox = width == null && height == null
        val colorHex = color.hex()
        val textHex = textColor?.hex()

        val outerClasses = mutableListOf("on-title-font", "font-thin")
        if (bigBox) {
            outerClasses.add("rounded-lg")
            outerClasses.add("shadow-xl")
            outerClasses.add("h-full")
        } else {
            outerClasses.add("flex-auto")
            outerClasses.add("rounded")
            outerClasses.add("border")
            outerClasses.add("border-slate-200")
            outerClasses.add("shadow-inner")
            outerClasses.add("mx-1")
        }
        classList(outerClasses)
        inlineStyle("${width.css("width", "rem")}${height.css("height", "rem")}")
        div {
            val innerClasses = mutableListOf("shadow-inner","w-full","h-full","flex","flex-wrap","justify-center","content-center")
            if (bigBox) {
                innerClasses.add("rounded-lg")
            } else {
                innerClasses.add("rounded")
            }
            classList(innerClasses)

            inlineStyle("background-color:$colorHex;${textHex.css("color")};")

            id?.let { id(it) }
            title(colorHex)

            textHex?.let {
                p {
                    +(textToRender?.replace("{{hex}}", colorHex) ?: colorHex)
                }
            }
        }

        handler?.let {
            clicks.map { color } handledBy it
        }
    }

private fun Any?.css(property: String, unit: String? = null) = this?.let { "$property:$it${unit ?: ""};" } ?: ""
