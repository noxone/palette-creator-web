package org.olafneumann.palette.app.ui

import dev.fritz2.core.Handler
import dev.fritz2.core.RenderContext
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
            val div = smallColorBox(
                width = width,
                height = height,
                color = color.color,
                //text = "${color.color.hex()} - ${color.shade.format(2)}"
            )
            handler?.let { div.clicks.map { color.color } handledBy it }
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
            smallColorBox(width = width, height = height, color = color)
        }
    }

private fun RenderContext.smallColorBox(
    width: Double,
    height: Double,
    color: Color
) =
    div {
        val hex = color.hex()

        className("flex-auto rounded border border-slate-200 shadow-inner mx-1")
        inlineStyle("background-color: ${hex};width: ${width}rem;height: ${height}rem;")
        title(hex)
    }

fun RenderContext.bigColorBox(
    color: Color,
    textColor: Color? = null,
    handler: Handler<Color>? = null
) =
    div {
        className("on-title-font rounded-lg shadow-xl font-thin h-full")
        div {
            className("rounded-lg shadow-inner w-full h-full flex flex-wrap justify-center content-center")
            inlineStyle("background-color: ${color.hex()};${textColor?.let { "color:${it.hex()};" } ?: ""}")
            textColor?.let {
                p {
                    +color.hex()
                }
            }

            handler?.let {
                clicks.map { color } handledBy it
            }
        }
    }
