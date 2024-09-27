package org.olafneumann.palette.app

import dev.fritz2.core.Handler
import dev.fritz2.core.HtmlTag
import dev.fritz2.core.RenderContext
import dev.fritz2.core.RootStore
import dev.fritz2.core.Window
import dev.fritz2.core.`for`
import dev.fritz2.core.id
import dev.fritz2.core.render
import dev.fritz2.core.title
import dev.fritz2.core.type
import dev.fritz2.core.value
import dev.fritz2.core.values
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colorful.ColorName
import org.olafneumann.palette.model.PaletteModel
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

private val COLOR_COUNT_DIV = 48
private val HEADER_ID = "on_header"

fun main() {

    val colorCountStore = object : RootStore<Int>(min(1536, window.innerWidth) / COLOR_COUNT_DIV, job = Job()) {
        val setSize: Handler<Event> = handle { _: Int, _: Event ->
            val element = document.getElementById(HEADER_ID)
            val width = min(1536, element?.clientWidth ?: 1536)
            width / COLOR_COUNT_DIV
        }
    }
    val colorStore = object :
        RootStore<PaletteModel>(
            PaletteModel(
                shadeCount = 7,
                primaryColor = Color.randomPrimary(),
                neutralColor = Color.randomNeutral(),
                accentColors = emptyList(),
            ),
            job = Job()
        ) {

        private fun checkAccentColorReset(model: PaletteModel): Boolean {
            val hasAccentColorsDefined = model.accentColors.isNotEmpty()
            var resetAccentColors = false
            if (hasAccentColorsDefined) {
                if (window.confirm("Changing the primary color could make the existing accept colors unusable. Should these be reset?")) {
                    resetAccentColors = true
                }
            }
            return resetAccentColors
        }

        val setPrimaryColor: Handler<String> = handle { model: PaletteModel, action: String ->
            Color.Hex(action)
                ?.let { model.setPrimaryColor(primaryColor = it, resetAccentColors = checkAccentColorReset(model)) }
                ?: model
        }
        val randomizePrimaryColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.setPrimaryColor(primaryColor = Color.randomPrimary(), resetAccentColors = checkAccentColorReset(model))
        }
        val randomizeWarmNeutralColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.copy(neutralColor = Color.randomNeutral(ColorName.red, ColorName.yellow, ColorName.orange))
        }
        val randomizeColdNeutralColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.copy(neutralColor = Color.randomNeutral(ColorName.blue, ColorName.aqua))
        }
        val randomizeNeutralColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.copy(neutralColor = Color.randomNeutral())
        }
        val addRandomAccentColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.addRandomAccentColor()
        }
        val removeAccentColor: Handler<Int> = handle { model: PaletteModel, index: Int ->
            model.copy(accentColors = model.accentColors - model.accentColors.get(index))
        }

        val downloadStuff: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            // TODO: implement stuff
            model
        }
    }

    render(selector = "#target") {
        Window.resizes handledBy colorCountStore.setSize

        div {
            className("md:container md:mx-auto text-slate-900")


            div {
                id(HEADER_ID)
                className("mt-5 px-4 py-7 bg-orange-400 rounded-xl shadow-xl relative text-center")
                div {
                    className("on-title-font text-5xl sm:text-6xl md:text-7xl py-4 sm:py-6")

                    +"Palette Creator"
                }
                colorCountStore.data.render { colorCount ->
                    colorList(
                        width = 2.5,
                        height = 2.8,
                        colors = (0..<colorCount).map {
                            Color.HSLuv(
                                h = 290.0 / colorCount * it,
                                s = 0.1 + 0.85 / colorCount * it,
                                l = 0.7
                            )
                        })
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

            section(
                number = 1,
                title = "Primary Color",
                instruction = "Please pick or enter the main color you want to use for your application.",
                explanation = """This is the main color for your app or website. It determines the color, people mostly see when interacting with your software.""".trimMargin(),
            ) {
                div {
                    className("grid grid-cols-12")

                    div {
                        className("inline-flex rounded-md shadow-sm col-span-8")
                        label {
                            className("z-20 cursor-pointer px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-s-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            inlineStyle("position:relative;")
                            `for`("on-primary-color-picker")
                            div {
                                className("flex flex-wrap content-center justify-center h-full")
                                +"Color Picker"
                            }
                            div {
                                className("flex flex-wrap content-center justify-center z-0")
                                inlineStyle("position:absolute;left:0;right:0;top:0;bottom:0;")
                                input {
                                    type("color")
                                    inlineStyle("opacity:0;")
                                    id("on-primary-color-picker")
                                    value(colorStore.data.map { it.primaryColor.Hex() })
                                    changes.values() handledBy colorStore.setPrimaryColor
                                }
                            }
                        }
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Enter hex RGB"
                        }
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Randomize Color"
                            clicks handledBy colorStore.randomizePrimaryColor
                        }
                    }

                    div {
                        className("col-span-4 w-full h-full")
                        colorStore.data.render(into = this) {
                            val useBrightTextColor = it.primaryColor.HSLuv().l < 0.65
                            colorBox(
                                color = it.primaryColor,
                                textColor = if (useBrightTextColor) it.shadedPrimaryColors.first().color else it.shadedPrimaryColors.last().color
                            )
                        }
                    }

                    div {
                        className("border-t col-span-12 mt-3 pt-3")
                        p {
                            +"The currently selected color would bring the first set of nice shades for your palette:"
                        }
                        div {
                            colorStore.data.render(into = this) {
                                className("border rounded-lg p-2 mt-2 shadow-inner")
                                inlineStyle("max-width:46rem;")

                                colorList(width = 2.5, height = 2.5, it.shadedPrimaryColors.map { sc -> sc.color })
                            }
                        }
                    }
                }
            }

            section(
                number = 2,
                title = "Neutral Color",
                instruction = "Choose a neutral color. Shades of this might be used for backgrounds, texts or borders.",
                explanation = """True black or white often looks strange to the eye, so we should go with some other very dark or light colors.
                    |There is no real science in choosing the neutral color. It should just fit to your primary color.
                """.trimMargin(),
            ) {
                div {
                    className("grid grid-cols-12")
                    div {
                        className("col-span-8 inline-flex rounded-md shadow-sm")
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-s-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Derived from primary"
                        }
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Random warm"
                            clicks handledBy colorStore.randomizeWarmNeutralColor
                        }
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Random cold"
                            clicks handledBy colorStore.randomizeColdNeutralColor
                        }
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Completely random"
                            clicks handledBy colorStore.randomizeNeutralColor
                        }
                    }

                    div {
                        className("col-span-4 w-full h-full")
                        colorStore.data.render(into = this) {
                            colorBox(
                                color = it.neutralColor,
                                textColor = it.shadedNeutralColors.first().color
                            )
                        }
                    }

                    div {
                        className("border-t col-span-12 mt-3 pt-3")
                        p {
                            +"The neutral shades would look like this:"
                        }
                        div {
                            colorStore.data.render(into = this) {
                                className("border rounded-lg p-2 mt-2 shadow-inner")
                                inlineStyle("max-width:46rem;")

                                colorList(width = 2.5, height = 2.5, it.shadedNeutralColors.map { sc -> sc.color })
                            }
                        }
                    }
                }
            }

            section(
                number = 3,
                title = "Accent Color",
                instruction = "If you need need to highlight something, select an accent color.",
                explanation = """In order to highlight something you probably don't want to use your primary color. So add one or more accent colors.
                    |Be aware that too many color will also not do the trick ;)""".trimMargin(),
            ) {
                div {
                    className("grid grid-cols-12")
                    div {
                        className("col-span-12 inline-flex rounded-md shadow-sm")
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-s-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Add fitting accent color"
                            clicks handledBy colorStore.addRandomAccentColor
                        }
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Pick custom accent color"
                            // clicks handledBy colorStore.randomizeWarmNeutralColor
                        }
                    }

                    div {
                        colorStore.data.renderIf(predicate = { it.shadedAccentColors.isNotEmpty() }, into = this) {
                            className(" col-span-12 mt-3 pt-3")
                            className(
                                colorStore.data.map { it.shadedAccentColors.isNotEmpty() },
                                false
                            ) { if (it) "border-t" else "" }
                            p {
                                +"The accent shades would look like this:"
                            }
                            colorStore.data.map { it.shadedAccentColors }.renderEach { shades ->
                                val index = it.shadedAccentColors.indexOf(shades)
                                div {
                                    className("flex flex-row")
                                    div {
                                        className("border rounded-lg p-2 mt-2 shadow-inner")
                                        inlineStyle("max-width:46rem;")

                                        colorList(width = 2.5, height = 2.5, shades.map { sc -> sc.color })
                                    }
                                    button {
                                        type("button")
                                        +"D"
                                        clicks.map { index } handledBy colorStore.removeAccentColor
                                    }
                                }
                            }
                        }
                    }
                }
            }

            section(
                number = 4,
                title = "Special Colors",
                explanation = """For special cases like danger, warnings or even positive information you might want to use different colors in your UI.
                        You should also use some icons for this, as e.g. not all countries see "red" as a color for "danger". In China this is the color for luck and wealth."""
            ) {
                +"bla"
            }

            section(
                number = 5,
                title = "Download",
            ) {
                button {
                    type("button")
                    clicks handledBy colorStore.downloadStuff
                    +"Download"
                }
            }
        }
    }
}

private fun RenderContext.section(
    number: Int,
    title: String,
    instruction: String? = null,
    explanation: String? = null,
    content: HtmlTag<HTMLDivElement>.() -> Unit
) =
    boxy {
        div {
            className("hidden lg:block")
            div {
                className("on-title-font text-7xl antialiased text-slate-500 w-full text-center lining-nums mt-5")
                +number.toString()
            }
        }
        div {
            className("col-span-12 lg:col-span-11")
            div {
                className("mb-3")
                h2 {
                    className("on-title-font font-semibold text-3xl antialiased")
                    +title
                }
                instruction?.let { p { +it } }
            }

            div {
                className("text-sm text-slate-500 mb-2")
                explanation?.split("\n")?.forEach {
                    p { +it }
                }
            }

            content()
        }
    }

private fun RenderContext.hStack(content: HtmlTag<HTMLDivElement>.() -> Unit) {
    div {
        className("on-hstack flex flex-row flex-nowrap z-10")
        content()
    }
}

private fun RenderContext.vStack(content: HtmlTag<HTMLDivElement>.() -> Unit) {
    div {
        className("on-vstack flex flex-col flex-nowrap")
        content()
    }
}

private fun RenderContext.boxy(content: HtmlTag<HTMLDivElement>.() -> Unit) =
    div {
        className("mt-5 p-4 bg-slate-50 md:rounded-xl shadow-xl grid grid-cols-12")
        content()
    }

private fun RenderContext.colorList(width: Double, height: Double, colors: List<Color>) =
    div {
        className("flex flex-row justify-around justify-items-center")
        colors.forEach {
            colorBox(width = width, height = height, color = it)
        }
    }

private fun RenderContext.colorBox(width: Double, height: Double, color: Color) =
    div {
        className("flex-auto rounded border border-slate-200 shadow-inner mx-1")
        inlineStyle("background-color: ${color.Hex()};width: ${width}rem;height: ${height}rem;")
        title(color.Hex())
    }

private fun RenderContext.colorBox(color: Color, textColor: Color? = null) =
    div {
        className("on-title-font rounded-lg shadow-xl font-thin h-full")
        div {
            className("rounded-lg shadow-inner w-full h-full flex flex-wrap justify-center content-center")
            inlineStyle("background-color: ${color.Hex()};${textColor?.let { "color:${it.Hex()};" } ?: ""}")
            textColor?.let {
                p {
                    +color.Hex()
                }
                p {
                    className("ms-2")
                    val hsl = color.Hsl()
                    +"${hsl.h.format(2)},${hsl.s.format(2)},${hsl.l.format(2)}"
                }
            }
        }
    }

private fun Color.Companion.randomPrimary(): Color = Color.HSLuv(
    h = Random.nextDouble() * 360,
    s = 0.5 + 0.5 * Random.nextDouble(),
    l = 0.5 + 0.35 * Random.nextDouble()
)

private fun Color.Companion.randomNeutral(vararg allowedColorNames: ColorName): Color {
    val nextH = { Random.nextDouble() * 360 }
    var h = nextH()
    if (allowedColorNames.isNotEmpty()) {
        while (allowedColorNames.none { ColorName.fromDegree(h) == it }) {
            h = nextH()
        }
    }
    return Color.HSLuv(
        h = h,
        s = 0.001 + 0.1 * Random.nextDouble(),
        l = 0.5
    )
}

private fun Double.format(digits: Int) =
    ((10.0.pow(digits) * this).roundToInt().toDouble() / 10.0.pow(digits)).toString()
