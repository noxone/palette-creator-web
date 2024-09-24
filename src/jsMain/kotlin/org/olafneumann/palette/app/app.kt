package org.olafneumann.palette.app

import dev.fritz2.core.Handler
import dev.fritz2.core.HtmlTag
import dev.fritz2.core.RenderContext
import dev.fritz2.core.RootStore
import dev.fritz2.core.Window
import dev.fritz2.core.id
import dev.fritz2.core.render
import dev.fritz2.core.type
import dev.fritz2.core.value
import dev.fritz2.core.values
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.model.PaletteModel
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import kotlin.math.min

private val COLOR_COUNT_DIV = 48
private val HEADER_ID = "on_header"
private val TEXT_COLOR = "text-slate-"
private val DARK = 900
private val LIGHT = 100
private val MIDDLE = 500
private fun textColor(shade: Int) = "${TEXT_COLOR}${shade}"

fun main() {

    val colorCountStore = object : RootStore<Int>(min(1536, window.innerWidth) / COLOR_COUNT_DIV, job = Job()) {
        val setSize: Handler<Event> = handle { _: Int, _: Event ->
            val element = document.getElementById(HEADER_ID)
            val width = min(1536, element?.clientWidth ?: 1536)
            width / COLOR_COUNT_DIV
        }
    }
    val colorStore = object : RootStore<PaletteModel>(PaletteModel(color = Color.Hex("#5e30eb")), job = Job()) {
        val setPrimaryColor: Handler<String> = handle { currentState: PaletteModel, action: String ->
            currentState.copy(color = Color.Hex(action))
        }
    }

    render(selector = "#target") {
        Window.resizes handledBy colorCountStore.setSize

        div {
            className("md:container md:mx-auto ${textColor(DARK)}")


            div {
                id(HEADER_ID)
                className("mt-5 px-4 py-7 bg-orange-400 rounded-xl shadow-xl relative text-center")
                div {
                    className("on-title-font text-5xl sm:text-6xl md:text-7xl py-4 sm:py-6")

                    +"Palette Creator"
                }
                colorCountStore.data.render { colorCount ->
                    // todo: change number of color blocks depending on screen width
                    colorList(width = 2, height = 3, colors = (0..<colorCount).map { Color.HSLuv(h = 360.0 / colorCount * it, s = 0.3 + 0.65 / colorCount * it, l = 0.7) })
                }
            }
            div {
                className("mx-7 p-5 bg-orange-300 rounded-b-xl shadow-xl")
                div {
                    p {
                        +"You want to create a color palette for your app or website. Then this might be a good starting point for you. "
                    }
                    p {
                        +"In a very steps we will create a nice color palette for you."
                    }
                }
            }

            Section(
                number = 1,
                title = "Primary Color",
                explanation = """This is the main color for your app or website.
                        This one determines the color, people mostly see when interacting with your software.""",
                /*postContent = {div {
                    className("z-0")
                    inlineStyle(colorStore.data.map { "position: absolute;right: 0;top: 0;bottom: 0;width: 50%;border-radius: 0 0.75em 0 0;background-color:${it.color};background: linear-gradient(90deg, rgba(255, 255, 255, 0) 0%, ${it.color} 80%);" })
                }}*/
                ) {
                // direkt in boxy:
                // inlineStyle(colorStore.data.map { "transition: all 0.4s ease-in-out;background-color:${it.color};background: linear-gradient(90deg, #f8fafc 50%, ${it.color} 80%);" })

                VStack {
                    div {
                        className("grid grid-cols-4")
                        HStack {
                            className("col-span-3")
                            VStack {
                                div { +"Color picker" }
                                input {
                                    type("color")
                                    value(colorStore.data.map { it.color.Hex() })
                                    changes.values() handledBy colorStore.setPrimaryColor
                                }
                            }
                            VStack {
                                div { +"Hex:" }
                                div { colorStore.data.render(this) { +it.color.Hex() } }
                            }
                        }
                        colorStore.data.render {
                            colorBox(color = it.color)
                        }
                    }

                    br {  }
                }
            }

            Section(
                number = 2,
                title = "Neutral Color",
                explanation = """Next, we will select a quite neutral color for the background and foreground.
                        True black or white often looks strange to the eye, so we should go with some other very dark or light colors."""
            ) {
                +"bla"
            }

            Section(
                number = 3,
                title = "Accent Color",
                explanation = "You might need to highlight something. Then use an accent color, that fits to the primary color."
            ) {
                +"bla"
            }

            Section(
                number = 4,
                title = "Special Colors",
                explanation = """For special cases like danger, warnings or even positive information you might want to use different colors in your UI.
                        You should also use some icons for this, as e.g. not all countries see "red" as a color for "danger". In China this is the color for luck and wealth."""
            ) {
                +"bla"
            }

            Section(
                title = "Preview Color Palette",
                explanation = """See your color palette in full beauty here."""
            ) {
                +"Palette"
            }

            Section(
                number = 5,
                title = "Download",
                backgroundShade = 200,
            ) {
                +"bla"
            }
        }
    }
}

private fun RenderContext.Section(
    number: Int? = null,
    title: String,
    backgroundShade: Int = 50,
    explanation: String? = null,
    postContent: (HtmlTag<HTMLDivElement>.() -> Unit)? = null,
    content: HtmlTag<HTMLDivElement>.() -> Unit) =
    boxy(backgroundShade = backgroundShade) {
        number?.let {
            div {
                className("hidden lg:block")
                SectionNumber(number)
            }
        }
        div {
            className("col-span-11")
            VStack {
                div {
                    className("mb-3")
                    SectionTitle(title)
                    explanation?.split("\n")?.forEach {
                        Explanation(it)
                    }
                }

                content()
            }
        }
        postContent?.let { it() }
    }

private fun RenderContext.SectionNumber(number: Int) {
    div {
        className("on-title-font font-semibold text-7xl antialiased ${textColor(MIDDLE)} w-full text-center lining-nums")
        +number.toString()
    }
}

private fun RenderContext.HStack(content: HtmlTag<HTMLDivElement>.() -> Unit) {
    div {
        className("on-hstack flex flex-row flex-nowrap z-10")
        content()
    }
}

private fun RenderContext.VStack(content: HtmlTag<HTMLDivElement>.() -> Unit) {
    div {
        className("on-vstack flex flex-col flex-nowrap")
        content()
    }
}

private fun RenderContext.SectionTitle(text: String) {
    h2 {
        className("on-title-font font-semibold text-3xl antialiased")
        +text
    }
}

private fun RenderContext.Explanation(text: String) {
    p {
        className("text-sm ${textColor(MIDDLE)}")
        +text
    }
}

private fun RenderContext.boxy(backgroundShade: Int = 50, content: HtmlTag<HTMLDivElement>.() -> Unit) =
    div {
        //border border-slate-200
        className("mt-5 p-4 bg-slate-$backgroundShade md:rounded-xl shadow-xl relative grid grid-cols-12")
        content()
    }

private fun RenderContext.colorList(width: Int, height: Int, colors: List<Color>) =
    div {
        className("flex flex-row flex-nowrap justify-around justify-items-center")
        colors.forEach {
            colorBox(width = width, height = height, color = it)
        }
    }

private fun RenderContext.colorBox(width: Int, height: Int, color: Color) =
    div {
        className("flex-auto bg-$color rounded border border-slate-200 shadow-inner w-$width h-$height mx-1")
        inlineStyle("background-color: ${color.Hex()};width: ${width}em;height: ${height}em;")
    }

private fun RenderContext.colorBox(color: Color, withText: Boolean = true) =
    div {
        className("on-title-font rounded-lg shadow-xl font-thin")
        div {
            val useBrightTextColor = color.HSLuv().l < 0.5
            className("rounded-lg shadow-inner w-full h-full flex flex-wrap justify-center content-center ${if(useBrightTextColor) textColor(LIGHT) else textColor(DARK)}")
            inlineStyle("background-color: ${color.Hex()};")
            if (withText) {
                +color.Hex()
            }
        }
    }
