package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.Handler
import dev.fritz2.core.RenderContext
import dev.fritz2.core.id
import dev.fritz2.core.title
import kotlinx.coroutines.flow.map
import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.ShadeList
import org.olafneumann.palette.colors.fittingFontColor


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
                textColor = color.color.fittingFontColor(light = colors.last().color, dark = colors.first().color),
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

@Suppress("LongParameterList")
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

        val outerClasses = mutableListOf("on-title-font", "font-thin", "transition-all")
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
            outerClasses.add("first:ms-0")
            outerClasses.add("last:me-0")
            outerClasses.add("group")
            outerClasses.add("text-xs")
        }
        /*if (handler != null) {
            outerClasses.add("hover:scale-105")
        }*/
        classList(outerClasses)
        inlineStyle("${width.css("width", "rem")}${height.css("height", "rem")}")
        div {
            val innerClasses = mutableListOf("shadow-inner","w-full","h-full","flex","flex-wrap","justify-center","content-center")
            if (bigBox) {
                innerClasses.add("rounded-lg")
            } else {
                innerClasses.add("rounded")
                innerClasses.add("*:hidden")
            }
            classList(innerClasses)

            inlineStyle("background-color:$colorHex;${textHex.css("color")};")

            id?.let { id(it) }
            title(colorHex)

            textHex?.let {
                p("group-hover:block") {
                    +(textToRender?.replace("{{hex}}", colorHex) ?: colorHex)
                }
            }
        }

        handler?.let {
            clicks.map { color } handledBy it
        }
    }

private fun Any?.css(property: String, unit: String? = null) = this?.let { "$property:$it${unit ?: ""};" } ?: ""
