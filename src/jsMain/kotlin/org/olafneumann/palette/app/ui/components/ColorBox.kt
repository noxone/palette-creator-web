package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.Handler
import dev.fritz2.core.RenderContext
import dev.fritz2.core.id
import kotlinx.coroutines.flow.map
import org.olafneumann.palette.app.npm.flip
import org.olafneumann.palette.app.npm.offset
import org.olafneumann.palette.app.npm.shift
import org.olafneumann.palette.app.utils.IdGenerator
import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.ShadeList
import org.olafneumann.palette.colors.fittingFontColor

fun RenderContext.colorDisplay(shadeList: ShadeList, vertical: Boolean = false, handler: Handler<Color>? = null) =
    div("grid grid-cols-5 gap-3") {
        val classListFirst = listOf(if (vertical) "col-span-full sm:col-span-2 h-12" else "col-span-full h-16 lg:h-32")
        val classListSecond = listOf(if (vertical) "col-span-full sm:col-span-3" else "col-span-full")
        div("w-full") {
            classList(classListFirst)
            colorBox(
                type = ColorBoxType.Big,
                color = shadeList.baseColor,
                textColor = shadeList.baseColor.fittingFontColor(
                    light = shadeList.lightestColor,
                    dark = shadeList.darkestColor
                )
            )
        }

        div {
            classList(classListSecond)
            colorList(
                width = "w-3",
                height = if (vertical) "h-12" else "h-10",
                colors = shadeList.shadedColors,
                handler = handler
            )
        }
    }

fun RenderContext.colorList(
    width: String,
    height: String,
    colors: List<ShadeList.ShadedColor>,
    handler: Handler<Color>? = null,
) =
    div {
        className("flex flex-row justify-around justify-items-center")
        colors.forEach { color ->
            colorBox(
                type = ColorBoxType.Small,
                width = width,
                height = height,
                color = color.color,
                textColor = color.color.fittingFontColor(light = colors.last().color, dark = colors.first().color),
                handler = handler
            )
        }
    }


fun RenderContext.colorList(
    width: String,
    height: String,
    colors: List<Color>,
) =
    div {
        className("flex flex-row justify-around justify-items-center")
        colors.forEach { color ->
            colorBox(type = ColorBoxType.Title, width = width, height = height, color = color)
        }
    }

private fun <T> MutableList<T>.addAll(vararg items: T) =
    addAll(items)

enum class ColorBoxType {
    Big, Small, Title, Button
}

@Suppress("LongParameterList", "LongMethod", "CyclomaticComplexMethod")
fun RenderContext.colorBox(
    type: ColorBoxType,
    color: Color,
    textColor: Color? = null,
    textToRender: String? = null,
    width: String? = null,
    height: String? = null,
    handler: Handler<Color>? = null,
) =
    div {
        val colorHex = color.hex()
        val textHex = textColor?.hex()

        val outerClasses = mutableListOf("on-title-font", "font-thin", "transition-all")
        when (type) {
            ColorBoxType.Big -> {
                outerClasses.addAll(
                    "rounded-lg",
                    "shadow-xl",
                    "h-full"
                )
            }
            ColorBoxType.Button -> {
                outerClasses.addAll(
                    "rounded-lg",
                    "shadow",
                    "h-full"
                )
            }
            else -> {
                outerClasses.addAll(
                    "flex-auto",
                    "rounded",
                    "border",
                    "border-slate-200",
                    "shadow-inner",
                    "mx-1",
                    "first:ms-0",
                    "last:me-0"
                )
            }
        }
        width?.let { outerClasses.add(it) }
        height?.let { outerClasses.add(it) }
        classList(outerClasses)
        div {
            val id = IdGenerator.next
            id(id)
            val innerClasses = mutableListOf(
                "w-full",
                "h-full",
                "flex",
                "flex-wrap",
                "justify-center",
                "content-center"
            )
            when (type) {
                ColorBoxType.Big -> {
                    innerClasses.addAll("shadow-inner", "rounded-lg")
                }
                ColorBoxType.Button -> {
                    innerClasses.addAll("shadow", "rounded")
                }
                else -> {
                    innerClasses.addAll("shadow-inner", "rounded")
                }
            }
            classList(innerClasses)

            inlineStyle("background-color:$colorHex;${textHex.css("color")};")

            //title(colorHex)

            if (type == ColorBoxType.Big || textToRender != null) {
                textHex?.let {
                    +(textToRender?.replace("{{hex}}", colorHex) ?: colorHex)
                }
            } else if (type == ColorBoxType.Small) {
                val floaterId = IdGenerator.next
                div("hidden text-slate-900 bg-slate-100 border-slate-400 px-4 py-2 border rounded-xl") {
                    id(floaterId)
                    +colorHex
                }
                val floater = Floater(
                    referenceElementId = id,
                    floatingElementId = floaterId,
                    options = Options(
                        middleware = arrayOf(offset(1), flip(), shift()),
                    )
                )
                floater.install(inHtmlTag = this, forType = FloaterEventType.MouseOver)
            }
        }

        handler?.let {
            clicks.map { color } handledBy it
        }
    }

private fun Any?.css(property: String, unit: String? = null) = this?.let { "$property:$it${unit ?: ""};" } ?: ""
