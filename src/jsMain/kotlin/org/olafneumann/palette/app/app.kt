package org.olafneumann.palette.app

import dev.fritz2.core.Handler
import dev.fritz2.core.HtmlTag
import dev.fritz2.core.Id
import dev.fritz2.core.RenderContext
import dev.fritz2.core.RootStore
import dev.fritz2.core.Window
import dev.fritz2.core.checked
import dev.fritz2.core.d
import dev.fritz2.core.fill
import dev.fritz2.core.`for`
import dev.fritz2.core.id
import dev.fritz2.core.max
import dev.fritz2.core.min
import dev.fritz2.core.render
import dev.fritz2.core.title
import dev.fritz2.core.type
import dev.fritz2.core.value
import dev.fritz2.core.values
import dev.fritz2.core.viewBox
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.ColorName
import org.olafneumann.palette.colors.ShadeList
import org.olafneumann.palette.colors.contrast
import org.olafneumann.palette.js.copyToClipboard
import org.olafneumann.palette.model.PaletteModel
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.url.URL
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

private const val COLOR_COUNT_DIV = 48
private const val HEADER_ID = "on_header"
private const val SHADES_MIN = 5
private const val SHADES_MAX = 15

private fun createInitialModel(): PaletteModel {
    val params = URL(document.URL).searchParams
    val primaryHex = params.get("primary")
    val neutralHex = params.get("neutral")
    val accentHexList = params.get("accents")?.split(',')

    return PaletteModel(
        shadeCount = 7, // TODO: read from params
        primaryColor = primaryHex?.let { Color.hex(it) } ?: Color.randomPrimary(),
        enforcePrimaryColorInShades = true, // TODO: read from params
        neutralColor = neutralHex?.let { Color.hex(it) } ?: Color.randomNeutral(),
        accentColors = accentHexList?.let { it.mapNotNull { hex -> Color.hex(hex) } } ?: emptyList(),
    )
}

private val URL_CURRENT = URL(window.location.toString())
private fun URL.toCurrentWindowLocation(): URL {
    val url = URL(this.toString())
    url.protocol = URL_CURRENT.protocol
    url.hostname = URL_CURRENT.hostname
    url.port = URL_CURRENT.port
    return url
}

fun main() {

    val colorCountStore = object : RootStore<Int>(min(1536, window.innerWidth) / COLOR_COUNT_DIV, job = Job()) {
        val setSize: Handler<Event> = handle { _: Int, _: Event ->
            val element = document.getElementById(HEADER_ID)
            val width = min(1536, element?.clientWidth ?: 1536)
            width / COLOR_COUNT_DIV
        }
    }
    val modelStore = object :
        RootStore<PaletteModel>(
            initialData = createInitialModel(),
            job = Job()
        ) {

        private val queryStringChanger = handle { model: PaletteModel, _: PaletteModel ->
            val map = mapOf(
                "primary" to model.primaryColor.hex().substring(1),
                "neutral" to model.neutralColor.hex().substring(1),
                "accents" to model.accentColors.joinToString(",") { it.hex().substring(1) }
            )
            val localhostUrl = map
                .map { "${it.key}=${it.value}" }
                .joinToString(prefix = "http://localhost/?", separator = "&")
            val url = URL(localhostUrl).toCurrentWindowLocation()
            window.history.replaceState(data = null, title = document.title, url = url.search)
            model
        }

        init {
            data handledBy queryStringChanger
        }

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
            Color.hex(action)
                ?.let { model.setPrimaryColor(primaryColor = it, resetAccentColors = checkAccentColorReset(model)) }
                ?: model
        }
        val setPrimaryEnsurance: Handler<Boolean> = handle { model: PaletteModel, action: Boolean ->
            model.copy(enforcePrimaryColorInShades = action)
        }
        val randomizePrimaryColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.setPrimaryColor(
                primaryColor = Color.randomPrimary(),
                resetAccentColors = checkAccentColorReset(model)
            )
        }
        val deriveNeutralColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.copy(neutralColor = model.primaryColor.deriveNeutral())
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
        val removeAccentColor: Handler<Color> = handle { model: PaletteModel, color: Color ->
            model.copy(accentColors = model.accentColors - color)
        }
        val updateShadeCount: Handler<Int> = handle { model: PaletteModel, count: Int -> model.copy(shadeCount = count) }

        val downloadStuff: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            // TODO: implement stuff
            model
        }

        val copyColorToClipboard: Handler<Color> = handle { model: PaletteModel, color: Color ->
            copyToClipboard(color.hex())
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
                            Color.hsluv(
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
                        +"In a few steps we will create a nice color palette for you."
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
                        className("col-span-8")
                        div {
                            className("inline-flex")
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
                                        value(modelStore.data.map { it.primaryColor.hex() })
                                        changes.values() handledBy modelStore.setPrimaryColor
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
                                clicks handledBy modelStore.randomizePrimaryColor
                            }
                        }

                        checkbox(modelStore.data.map { it.enforcePrimaryColorInShades }, handler = modelStore.setPrimaryEnsurance, label = "Make sure, the primary color is part of the generated shades.")

                        modelStore.data.map { it.isPrimaryColorSaturatedEnough }.renderFalse {
                            div {
                                className("flex items-center w-full max-w-xs p-4 text-gray-500 bg-white rounded-lg shadow dark:text-gray-400 dark:bg-gray-800")
                                div {
                                    className("inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-orange-500 bg-orange-100 rounded-lg dark:bg-orange-700 dark:text-orange-200")
                                    svg {
                                        className("w-5 h-5")
                                        xmlns("http://www.w3.org/2000/svg")
                                        viewBox("0 0 20 20")
                                        path {
                                            d("M10 .5a9.5 9.5 0 1 0 9.5 9.5A9.51 9.51 0 0 0 10 .5ZM10 15a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm1-4a1 1 0 0 1-2 0V6a1 1 0 0 1 2 0v5Z")
                                        }
                                        span {
                                            className("sr-only")
                                            +"Warning icon"
                                        }
                                    }
                                }
                                div {
                                    className("ms-3 text-sm font-normal")
                                    +"The saturation of the main color is quite low. This might not be a problem, but we propose to use a color with some more saturation as primary color."
                                }
                                button {
                                    className("ms-auto -mx-1.5 -my-1.5 bg-white text-gray-400 hover:text-gray-900 rounded-lg focus:ring-2 focus:ring-gray-300 p-1.5 hover:bg-gray-100 inline-flex items-center justify-center h-8 w-8 dark:text-gray-500 dark:hover:text-white dark:bg-gray-800 dark:hover:bg-gray-700")
                                    type("button")
                                    span {
                                        className("sr-only")
                                        +"Close"
                                    }
                                    svg {
                                        className("h-3 w-3")
                                        xmlns("http://www.w3.org/2000/svg")
                                        fill("none")
                                        viewBox("0 0 14 14")
                                        path {
                                            d("m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6")
                                            attr("stroke", "currentColor")
                                            attr("stroke-linecap", "round")
                                            attr("stroke-linejoin", "round")
                                            attr("stroke-width", "2")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    div {
                        className("col-span-4 w-full h-full")
                        modelStore.data.render(into = this) {
                            val useBrightTextColor = it.primaryColor.hsluv().l < 0.65
                            colorBox(
                                color = it.primaryColor,
                                textColor = if (useBrightTextColor) it.primaryColorShadeList.lightestColor else it.primaryColorShadeList.darkestColor
                            )
                        }
                    }

                    div {
                        className("border-t col-span-12 mt-3 pt-3")
                        p {
                            +"The currently selected color would bring the first set of nice shades for your palette:"
                        }
                        div {
                            modelStore.data.map { it.primaryColorShadeList.shadedColors }.render(into = this) { colors ->
                                className("border rounded-lg p-2 mt-2 shadow-inner")
                                inlineStyle("max-width:46rem;")

                                colorList(
                                    width = 2.5,
                                    height = 2.5,
                                    colors,
                                    handler = modelStore.copyColorToClipboard
                                )
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
                            clicks handledBy modelStore.deriveNeutralColor
                        }
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Random warm"
                            clicks handledBy modelStore.randomizeWarmNeutralColor
                        }
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Random cold"
                            clicks handledBy modelStore.randomizeColdNeutralColor
                        }
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Completely random"
                            clicks handledBy modelStore.randomizeNeutralColor
                        }
                    }

                    div {
                        className("col-span-4 w-full h-full")

                        modelStore.data.render(into = this) {
                            div {
                                colorBox(
                                    color = it.neutralColor,
                                    textColor = it.neutralColorShadeList.lightestColor
                                )
                            }

                            div {
                                +"Contrast to primary color: ${it.neutralColor.contrast(it.primaryColor)}"
                            }
                        }
                    }

                    div {
                        className("border-t col-span-12 mt-3 pt-3")
                        p {
                            +"The neutral shades would look like this:"
                        }
                        div {
                            modelStore.data.map { it.neutralColorShadeList.shadedColors }.render(into = this) { colors ->
                                className("border rounded-lg p-2 mt-2 shadow-inner")
                                inlineStyle("max-width:46rem;")

                                colorList(
                                    width = 2.5,
                                    height = 2.5,
                                    colors,
                                    handler = modelStore.copyColorToClipboard
                                )
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
                            clicks handledBy modelStore.addRandomAccentColor
                        }
                        button {
                            type("button")
                            className("px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white")
                            +"Pick custom accent color"
                            // clicks handledBy colorStore.randomizeWarmNeutralColor
                        }
                    }

                    div {
                        className(" col-span-12 mt-3 pt-3")
                        className(
                            modelStore.data.map { it.accentColorsShadeLists.isNotEmpty() },
                            false
                        ) { if (it) "border-t" else "hidden" }
                        p {
                            +"The accent shades would look like this:"
                        }
                        modelStore.data.map { it.accentColorsShadeLists }.renderEach(idProvider = { "${it.baseColor.hex().substring(1)}-${it.shadedColors.count()}" }) { shadeList ->
                            div {
                                className("flex flex-row")
                                div {
                                    className("border rounded-lg p-2 mt-2 shadow-inner")
                                    inlineStyle("max-width:46rem;")

                                    colorList(
                                        width = 2.5,
                                        height = 2.5,
                                        shadeList.shadedColors,
                                        handler = modelStore.copyColorToClipboard
                                    )
                                }
                                button {
                                    type("button")
                                    +"D"
                                    clicks.map { shadeList.baseColor } handledBy modelStore.removeAccentColor
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
                title = "Options",
            ) {
                h3 {
                    modelStore.data.map { it.shadeCount }.render(into = this) {
                        +"Shade count: $it"
                    }
                }
                div {
                    label {
                        className("block mb-2 text-sm font-medium text-gray-900 dark:text-white")
                        `for`("shade-count")
                        modelStore.data.map { it.shadeCount }.renderText(into = this)
                    }
                    input {
                        className("w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer dark:bg-gray-700")
                        id("shade-count")
                        type("range")
                        min(SHADES_MIN.toString())
                        max(SHADES_MAX.toString())
                        value(modelStore.data.map { it.shadeCount.toString() })
                        changes.map { it.target.unsafeCast<HTMLInputElement>().value.toInt() } handledBy modelStore.updateShadeCount
                    }
                }
            }

            section(
                number = 6,
                title = "Download",
            ) {
                button {
                    type("button")
                    clicks handledBy modelStore.downloadStuff
                    +"Download"
                }
            }
        }
    }
}

private fun RenderContext.checkbox(value: Flow<Boolean>, handler: Handler<Boolean>? = null, label: String) =
    checkbox(value = value, handler = handler) { +label }

private fun RenderContext.checkbox(value: Flow<Boolean>, handler: Handler<Boolean>? = null, label: HtmlTag<HTMLLabelElement>.() -> Unit) =
    div {
        val id = Id.next()
        className("flex items-start mb-6")
        div {
            className("flex-items-center h-5")
            input {
                className("w-4 h-4 border border-gray-300 rounded bg-gray-50 focus:ring-3 focus:ring-blue-300 dark:bg-gray-700 dark:border-gray-600 dark:focus:ring-blue-600 dark:ring-offset-gray-800")
                id(id)
                type("checkbox")
                checked(value)
                handler?.let { handler ->
                    changes.map { event -> event.target.unsafeCast<HTMLInputElement>().checked } handledBy handler
                }
            }
        }
        label {
            className("ms-2 text-sm font-medium text-gray-900 dark:text-gray-300")
            `for`(id)
            label()
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

private fun RenderContext.boxy(content: HtmlTag<HTMLDivElement>.() -> Unit) =
    div {
        className("mt-5 p-4 bg-slate-50 md:rounded-xl shadow-xl grid grid-cols-12")
        content()
    }

private fun RenderContext.colorList(
    width: Double,
    height: Double,
    colors: List<ShadeList.ShadedColor>,
    handler: Handler<Color>? = null
) =
    div {
        className("flex flex-row justify-around justify-items-center")
        colors.forEach { color ->
            val div = colorBox(width = width, height = height, hex = color.color.hex(), text = "${color.color.hex()} - ${color.shade.format(2)}")
            handler?.let { div.clicks.map { color.color } handledBy it }
        }
    }


private fun RenderContext.colorList(
    width: Double,
    height: Double,
    colors: List<Color>
) =
    div {
        className("flex flex-row justify-around justify-items-center")
        colors.forEach { color ->
            colorBox(width = width, height = height, hex = color.hex())
        }
    }

private fun RenderContext.colorBox(width: Double, height: Double, hex: String, text: String? = null) =
    div {
        className("flex-auto rounded border border-slate-200 shadow-inner mx-1")
        inlineStyle("background-color: ${hex};width: ${width}rem;height: ${height}rem;")
        title(hex)
        text?.let { +it }
    }

private fun RenderContext.colorBox(color: Color, textColor: Color? = null, handler: Handler<Color>? = null) =
    div {
        className("on-title-font rounded-lg shadow-xl font-thin h-full")
        div {
            className("rounded-lg shadow-inner w-full h-full flex flex-wrap justify-center content-center")
            inlineStyle("background-color: ${color.hex()};${textColor?.let { "color:${it.hex()};" } ?: ""}")
            textColor?.let {
                p {
                    +color.hex()
                }
                p {
                    className("ms-2")
                    val hsl = color.hsluv()
                    +"${hsl.h.format(2)},${hsl.s.format(2)},${hsl.l.format(2)}"
                }
            }

            handler?.let {
                clicks.map { color } handledBy it
            }
        }
    }

private fun Color.Companion.randomPrimary(): Color = hsluv(
    h = Random.nextDouble() * 360,
    s = 0.5 + 0.5 * Random.nextDouble(),
    l = 0.5 + 0.35 * Random.nextDouble()
)

private fun Color.deriveNeutral(): Color {
    val hsl = hsluv()
    return Color.hsluv(h = hsl.h, s = 0.05, l = 0.5)
}

private fun Color.Companion.randomNeutral(vararg allowedColorNames: ColorName): Color {
    val nextH = { Random.nextDouble() * 360 }
    var h = nextH()
    if (allowedColorNames.isNotEmpty()) {
        while (allowedColorNames.none { ColorName.fromDegree(h) == it }) {
            h = nextH()
        }
    }
    return hsluv(
        h = h,
        s = 0.001 + 0.1 * Random.nextDouble(),
        l = 0.5
    )
}

private fun Double.format(digits: Int) =
    ((10.0.pow(digits) * this).roundToInt().toDouble() / 10.0.pow(digits)).toString()
