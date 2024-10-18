package org.olafneumann.palette.app

import dev.fritz2.core.Handler
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
import org.olafneumann.palette.app.npm.JSZip
import org.olafneumann.palette.app.npm.Options
import org.olafneumann.palette.app.npm.Placement
import org.olafneumann.palette.app.npm.saveAs
import org.olafneumann.palette.app.ui.components.Button
import org.olafneumann.palette.app.ui.components.ButtonType
import org.olafneumann.palette.app.ui.components.button
import org.olafneumann.palette.app.ui.components.buttonGroup
import org.olafneumann.palette.app.ui.components.checkbox
import org.olafneumann.palette.app.ui.components.colorBox
import org.olafneumann.palette.app.ui.components.colorList
import org.olafneumann.palette.app.ui.components.iconDownload
import org.olafneumann.palette.app.ui.components.iconEdit
import org.olafneumann.palette.app.ui.components.iconTrash
import org.olafneumann.palette.app.ui.components.section
import org.olafneumann.palette.app.ui.components.warningToast
import org.olafneumann.palette.app.utils.copyToClipboard
import org.olafneumann.palette.app.utils.toCurrentWindowLocation
import org.olafneumann.palette.app.utils.toJson
import org.olafneumann.palette.app.utils.toMap
import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.ColorGenerator
import org.olafneumann.palette.colors.fittingFontColor
import org.olafneumann.palette.model.PaletteModel
import org.olafneumann.palette.model.generateCss
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.url.URL
import kotlin.math.min

private const val COLOR_COUNT_DIV = 48
private const val HEADER_ID = "on_header"
private const val SHADES_MIN = 5
private const val SHADES_MAX = 15

fun PaletteModel.Companion.fromCurrentLocation(): PaletteModel =
    parse(URL(document.URL).searchParams.toMap())

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
            initialData = PaletteModel.fromCurrentLocation(),
            job = Job()
        ) {

        private val queryStringChanger = handle { model: PaletteModel, _: PaletteModel ->
            window.history.replaceState(
                data = null,
                title = document.title,
                url = URL(model.createUrl()).toCurrentWindowLocation().search
            )
            model
        }

        init {
            data handledBy queryStringChanger
        }

        private fun checkAccentColorReset(model: PaletteModel): Boolean {
            val hasAccentColorsDefined = model.namedAccentColors.isNotEmpty()
            val resetAccentColors = hasAccentColorsDefined && window.confirm("Changing the primary color could make the existing accept colors unusable. Should these be reset?")
            console.log(hasAccentColorsDefined, resetAccentColors)
            return resetAccentColors
        }

        val setPrimaryColor: Handler<String> = handle { model: PaletteModel, hex: String ->
            Color.hex(hex)
                ?.let { color -> model.setPrimaryColor(primaryColor = color, resetAccentColors = checkAccentColorReset(model)) }
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
        val addAccentColorHex: Handler<String> = handle { model: PaletteModel, hex: String ->
            Color.hex(hex)
                ?.let { color -> model.addAccentColor(color) }
                ?: model
        }
        val addAccentColor: Handler<Color> = handle { model: PaletteModel, color: Color ->
            model.addAccentColor(color)
        }
        val addRandomAccentColor: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            model.addRandomAccentColor()
        }
        val removeAccentColor: Handler<Color> = handle { model: PaletteModel, color: Color ->
            model.removeAccentColor(color)
        }
        val updateShadeCount: Handler<Int> =
            handle { model: PaletteModel, count: Int -> model.copy(shadeCount = count) }

        val downloadCSS: Handler<MouseEvent> = handle { model: PaletteModel, _: MouseEvent ->
            downloadFile(filename = "shades.css", content = model.generateCss(), zipFilename = "shades.zip")
            model
        }

        val copyColorToClipboard: Handler<Color> = handle { model: PaletteModel, color: Color ->
            copyToClipboard(color.hex())
            model
        }

        private fun downloadFile(filename: String, content: String, zipFilename: String) {
            val zip = JSZip()
            zip.file(filename, content)
            zip.generateAsync(mapOf("type" to "blob").toJson()).then {
                saveAs(it, zipFilename)
            }
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
                            Button(
                                type = ButtonType.ColorPicker,
                                text = "Color Picker",
                                value = modelStore.data.map { it.primaryColor.hex() },
                                textHandler = modelStore.setPrimaryColor
                            ),
                            // TODO: Button(text = "Enter hex RGB"),
                            Button(text = "Randomize Color", clickHandler = modelStore.randomizePrimaryColor),
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
                                        colors = colors,
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
                            Button(text = "Derived from primary", clickHandler = modelStore.deriveNeutralColor),
                            Button(text = "Random warm", clickHandler = modelStore.randomizeWarmNeutralColor),
                            Button(text = "Random cold", clickHandler = modelStore.randomizeColdNeutralColor),
                            Button(text = "Completely random", clickHandler = modelStore.randomizeNeutralColor),
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
                                        colors = colors,
                                        handler = modelStore.copyColorToClipboard
                                    )
                                }
                        }
                    }
                }
            }

            section(
                number = 3,
                title = "Accent Colors",
                instruction = "If you need need to highlight something, select an accent color.",
                explanation = """In order to highlight something you probably don't want to use your primary color. So add one or more accent colors.
                    |Be aware that too many color will also not do the trick ;)""".trimMargin(),
            ) {
                div {
                    className("grid grid-cols-12")

                    div {
                        className("col-span-12")

                        buttonGroup(
                            Button(
                                text = "Derived from primary color",
                                floaterElement = {
                                    modelStore.data.map { it.proposedAccentColors }
                                        .renderEach(idProvider = { "proposedAccentColor_${it.color.hex()}" }) { color ->
                                            div {
                                                className("w-full h-12 p-1")
                                                colorBox(
                                                    color = color.color,
                                                    textColor = color.color.fittingFontColor(
                                                        Color(1.0, 1.0, 1.0), // TODO: replace by better colors
                                                        Color(0.0, 0.0, 0.0)
                                                    ),
                                                    textToRender = "${color.name}: {{hex}}",
                                                    handler = modelStore.addAccentColor,
                                                )
                                            }
                                        }
                                },
                                floaterOptions = Options(placement = Placement.bottomStart),
                                floaterEvents = listOf(FloaterEventType.Click),
                                floaterBlurOnOutsideClick = true,
                            ),
                            Button(
                                text = "Add random accent color",
                                clickHandler = modelStore.addRandomAccentColor,
                            ),
                            Button(
                                type = ButtonType.ColorPicker,
                                value = modelStore.data.map { it.proposedAccentColor.hex() },
                                text = "Pick custom accent color",
                                textHandler = modelStore.addAccentColorHex
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
                        modelStore.data.map { it.accentColorsShadeLists }
                            .renderEach(into = this, idProvider = {
                                "accent_color_${it.name}"
                            }) { shadeList ->
                                div {
                                    className("grid grid-cols-12")
                                    div {
                                        className("col-span-2")
                                        +shadeList.name

                                        button(Button(
                                            icon = { iconEdit() },
                                            //customCode = { clicks.map { shadeList.baseColor } handledBy modelStore.removeAccentColor }
                                        ))
                                    }
                                    div {
                                        className("col-span-9 border rounded-lg p-2 shadow-inner")
                                        inlineStyle("max-width:46rem;")

                                        colorList(
                                            width = 2.5,
                                            height = 2.5,
                                            shadeList.shadedColors,
                                            handler = modelStore.copyColorToClipboard
                                        )
                                    }
                                    div {
                                        className("col-span-1")
                                        button(Button(
                                            icon = { iconTrash() },
                                            customCode = { clicks.map { shadeList.baseColor } handledBy modelStore.removeAccentColor }
                                        ))
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
                button(
                    Button(
                        icon = { iconDownload() },
                        text = "CSS",
                        clickHandler = modelStore.downloadCSS
                    )
                )
            }
        }
    }
}

