package org.olafneumann.palette.app

import dev.fritz2.core.Handler
import dev.fritz2.core.HtmlTag
import dev.fritz2.core.RenderContext
import dev.fritz2.core.RootStore
import dev.fritz2.core.Window
import dev.fritz2.core.`for`
import dev.fritz2.core.id
import dev.fritz2.core.max
import dev.fritz2.core.min
import dev.fritz2.core.render
import dev.fritz2.core.type
import dev.fritz2.core.value
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.olafneumann.palette.app.npm.FloaterEventType
import org.olafneumann.palette.app.npm.Options
import org.olafneumann.palette.app.npm.Placement
import org.olafneumann.palette.app.ui.Button
import org.olafneumann.palette.app.ui.ButtonType
import org.olafneumann.palette.app.ui.buttonGroup
import org.olafneumann.palette.app.ui.checkbox
import org.olafneumann.palette.app.ui.colorBox
import org.olafneumann.palette.app.ui.colorList
import org.olafneumann.palette.app.ui.warningToast
import org.olafneumann.palette.app.utils.IdGenerator
import org.olafneumann.palette.app.utils.copyToClipboard
import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.ColorGenerator
import org.olafneumann.palette.colors.fittingFontColor
import org.olafneumann.palette.model.PaletteModel
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.url.URL
import kotlin.math.min

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
        primaryColor = primaryHex?.let { Color.hex(it) } ?: ColorGenerator.randomPrimary(),
        enforcePrimaryColorInShades = true, // TODO: read from params
        neutralColor = neutralHex?.let { Color.hex(it) } ?: ColorGenerator.randomNeutral(),
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
        val setPrimaryColorEnforcedInShades: Handler<Boolean> = handle { model: PaletteModel, action: Boolean ->
            model.copy(enforcePrimaryColorInShades = action)
        }
        val randomizePrimaryColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.setPrimaryColor(
                primaryColor = ColorGenerator.randomPrimary(),
                resetAccentColors = checkAccentColorReset(model)
            )
        }
        val deriveNeutralColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.copy(neutralColor = ColorGenerator.deriveNeutral(from = model.primaryColor))
        }
        val randomizeWarmNeutralColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.copy(neutralColor = ColorGenerator.randomNeutralWarm())
        }
        val randomizeColdNeutralColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.copy(neutralColor = ColorGenerator.randomNeutralCold())
        }
        val randomizeNeutralColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.copy(neutralColor = ColorGenerator.randomNeutral())
        }
        val addRandomAccentColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.addRandomAccentColor()
        }
        val removeAccentColor: Handler<Color> = handle { model: PaletteModel, color: Color ->
            model.copy(accentColors = model.accentColors - color)
        }
        val updateShadeCount: Handler<Int> =
            handle { model: PaletteModel, count: Int -> model.copy(shadeCount = count) }

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

                        buttonGroup(
                            listOf(
                                Button(
                                    type = ButtonType.colorPicker,
                                    text = "Color Picker",
                                    value = modelStore.data.map { it.primaryColor.hex() },
                                    textHandler = modelStore.setPrimaryColor
                                ),
                                // TODO: Button(text = "Enter hex RGB"),
                                Button(text = "Randomize Color", mouseHandler = modelStore.randomizePrimaryColor),
                            )
                        )

                        checkbox(
                            value = modelStore.data.map { it.enforcePrimaryColorInShades },
                            handler = modelStore.setPrimaryColorEnforcedInShades,
                            label = "Make sure, the primary color is part of the generated shades."
                        )

                        modelStore.data.map { it.isPrimaryColorSaturationHighEnough }.renderFalse {
                            warningToast("The saturation of the main color is quite low. This might not be a problem, but we propose to use a color with some more saturation as primary color.")
                        }
                    }

                    div {
                        className("col-span-4 w-full h-full")
                        modelStore.data.render(into = this) {
                            colorBox(
                                color = it.primaryColor,
                                textColor = it.primaryColor.fittingFontColor(
                                    light = it.primaryColorShadeList.lightestColor,
                                    dark = it.primaryColorShadeList.darkestColor
                                )
                            )
                        }
                    }

                    div {
                        className("border-t col-span-12 mt-3 pt-3")
                        p {
                            +"The currently selected color would bring the first set of nice shades for your palette:"
                        }
                        div {
                            modelStore.data.map { it.primaryColorShadeList.shadedColors }
                                .render(into = this) { colors ->
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
                        className("col-span-8")

                        buttonGroup(
                            listOf(
                                Button(text = "Derived from primary", mouseHandler = modelStore.deriveNeutralColor),
                                Button(text = "Random warm", mouseHandler = modelStore.randomizeWarmNeutralColor),
                                Button(text = "Random cold", mouseHandler = modelStore.randomizeColdNeutralColor),
                                Button(text = "Completely random", mouseHandler = modelStore.randomizeNeutralColor),
                            )
                        )

                        modelStore.data.map { it.isNeutralColorSaturationLowEnough }.renderFalse {
                            warningToast("The neutral color has a quite high saturation. We would suggest to choose a color with a lower saturation.")
                        }
                    }

                    div {
                        className("col-span-4 w-full h-full")

                        modelStore.data.render(into = this) {
                            colorBox(
                                color = it.neutralColor,
                                textColor = it.neutralColorShadeList.lightestColor
                            )
                        }
                    }

                    div {
                        className("border-t col-span-12 mt-3 pt-3")
                        p {
                            +"The neutral shades would look like this:"
                        }
                        div {
                            modelStore.data.map { it.neutralColorShadeList.shadedColors }
                                .render(into = this) { colors ->
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

                    val tooltipId = IdGenerator.next

                    div {
                        className("col-span-12")

                        fun RenderContext.createColorPickerFloater(id: String) =
                            div {
                                id(id)
                                div {
                                    id(tooltipId)
                                    classList(
                                        listOf(
                                            "shadow-xl",
                                            "z-10 inline-block w-64 text-sm text-gray-500 transition-opacity duration-300 bg-white border border-gray-200 rounded-lg shadow-sm dark:text-gray-400 dark:border-gray-600 dark:bg-gray-800",
                                        )
                                    )

                                    div {
                                        className("px-3 py-2 bg-gray-100 border-b border-gray-200 rounded-t-lg dark:border-gray-600 dark:bg-gray-700")
                                        h3 {
                                            className("font-semibold text-gray-900 dark:text-white")
                                            +"Choose accent color"
                                        }
                                    }
                                    div {
                                        modelStore.data.map { it.proposedAccentColors }.renderEach(into = this) { color ->
                                            div {
                                                className("w-64 h-12 p-2")
                                                colorBox(
                                                    color = color.color,
                                                    textColor = color.color.fittingFontColor(
                                                        Color(1.0, 1.0, 1.0),
                                                        Color(0.0, 0.0, 0.0)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                        buttonGroup(
                            listOf(
                                Button(
                                    text = "Choose accent color",
                                    floaterElement = { id -> createColorPickerFloater(id) },
                                    floaterOptions = Options(placement = Placement.bottomStart),
                                    floaterEvents = listOf(FloaterEventType.Click)
                                ),
                                Button(
                                    text = "Add random accent color",
                                    mouseHandler = modelStore.addRandomAccentColor,
                                ),
                                Button(text = "Pick custom accent color")
                            )
                        )
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
                        modelStore.data.map { it.accentColorsShadeLists }.renderEach(idProvider = {
                            "${
                                it.baseColor.hex().substring(1)
                            }-${it.shadedColors.count()}"
                        }) { shadeList ->
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
                number = 5,
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


private fun RenderContext.section(
    number: Int,
    title: String,
    instruction: String? = null,
    explanation: String? = null,
    content: HtmlTag<HTMLDivElement>.() -> Unit,
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
        className("my-4 p-4 bg-slate-50 md:rounded-xl shadow-xl grid grid-cols-12")
        content()
    }
