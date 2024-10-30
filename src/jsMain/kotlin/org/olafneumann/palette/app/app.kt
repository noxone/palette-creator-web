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
import org.olafneumann.palette.app.npm.Options
import org.olafneumann.palette.app.npm.Placement
import org.olafneumann.palette.app.ui.components.Button
import org.olafneumann.palette.app.ui.components.ButtonType
import org.olafneumann.palette.app.ui.components.ToastConfig
import org.olafneumann.palette.app.ui.components.button
import org.olafneumann.palette.app.ui.components.buttonGroup
import org.olafneumann.palette.app.ui.components.checkbox
import org.olafneumann.palette.app.ui.components.colorBox
import org.olafneumann.palette.app.ui.components.colorDisplay
import org.olafneumann.palette.app.ui.components.colorList
import org.olafneumann.palette.app.ui.components.iconDownload
import org.olafneumann.palette.app.ui.components.iconEdit
import org.olafneumann.palette.app.ui.components.iconTrash
import org.olafneumann.palette.app.ui.components.section
import org.olafneumann.palette.app.ui.components.warningToast
import org.olafneumann.palette.app.utils.copyToClipboard
import org.olafneumann.palette.app.utils.startDownload
import org.olafneumann.palette.app.utils.toCurrentWindowLocation
import org.olafneumann.palette.app.utils.toMap
import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.ColorGenerator
import org.olafneumann.palette.colors.fittingFontColor
import org.olafneumann.palette.model.OutputGenerator
import org.olafneumann.palette.model.PaletteModel
import org.w3c.dom.HTMLElement
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

@Suppress("LongMethod")
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
            val resetAccentColors =
                hasAccentColorsDefined && window.confirm("Changing the primary color could make the existing accept colors unusable. Should these be reset?")
            console.log(hasAccentColorsDefined, resetAccentColors)
            return resetAccentColors
        }

        val setPrimaryColor: Handler<String> = handle { model: PaletteModel, hex: String ->
            Color.hex(hex)
                ?.let { color ->
                    model.setPrimaryColor(
                        primaryColor = color,
                        resetAccentColors = checkAccentColorReset(model)
                    )
                }
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
        val removeAccentColor: Handler<String> = handle { model: PaletteModel, name ->
            if (window.confirm("Do you really want to delete accent color '$name'?"))
                model.removeAccentColor(name)
            else
                model
        }
        val renameAccentColor: Handler<String> = handle { model: PaletteModel, oldName ->
            val newName = window.prompt(message = "Please choose a new name for the accent color:", default = oldName)
            newName?.let { model.renameAccentColor(oldName, it) } ?: model
        }
        val updateShadeCount: Handler<Int> =
            handle { model: PaletteModel, count: Int -> model.copy(shadeCount = count) }

        val downloadOutput: Handler<OutputGenerator> = handle { model, generator ->
            generator.generateOutput(model).startDownload()
            model
        }

        val copyColorToClipboard: Handler<Color> = handle { model: PaletteModel, color: Color ->
            copyToClipboard(color.hex())
            model
        }
    }

    render(selector = "#on_main") {
        Window.resizes handledBy colorCountStore.setSize

        div("md:mt-4 px-4 py-7 bg-orange-400 md:rounded-xl shadow-xl relative text-center") {
            // TODO: use color 57A0CC
            id(HEADER_ID)
            div("py-4 sm:py-6") {
                div("on-title-font text-5xl sm:text-6xl md:text-7xl") {
                    +"Shade Generator"
                }
                p {
                    +"Generate shades and color palettes for your coding projects."
                }
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

        section(
            number = 1,
            title = "Primary Color",
            instruction = "Please pick or enter the main color you want to use for your application.",
            explanation = """This is the main color for your app or website. It determines the color, people mostly see when interacting with your software.""".trimMargin(),
            actions = listOf(
                Button(
                    type = ButtonType.ColorPicker,
                    text = "Use Color Picker",
                    value = modelStore.data.map { it.primaryColor.hex() },
                    textHandler = modelStore.setPrimaryColor
                ),
                // TODO: Button(text = "Enter hex RGB"),
                Button(text = "Randomize Color", clickHandler = modelStore.randomizePrimaryColor),
            ),
            toastConfig = ToastConfig(
                flow = modelStore.data.map { !it.isPrimaryColorSaturationHighEnough },
                message = "The saturation of the main color is quite low. This might not be a problem, but we propose to use a color with some more saturation as primary color."
            )
        ) {
            modelStore.data.map { it.primaryColorShadeList }.render { shadeList ->
                colorDisplay(shadeList = shadeList, handler = modelStore.copyColorToClipboard)
            }
        }

        section(
            number = 2,
            title = "Neutral Color",
            instruction = "Choose a neutral color. Shades of this might be used for backgrounds, texts or borders.",
            explanation = """True black or white often looks strange to the eye, so we should go with some other very dark or light colors.
                    |There is no real science in choosing the neutral color. It should just fit to your primary color.
                """.trimMargin(),
            actions = listOf(
                Button(text = "Derive from primary", clickHandler = modelStore.deriveNeutralColor),
                Button(text = "Random warm", clickHandler = modelStore.randomizeWarmNeutralColor),
                Button(text = "Random cold", clickHandler = modelStore.randomizeColdNeutralColor),
                Button(text = "Completely random", clickHandler = modelStore.randomizeNeutralColor),
            ),
            toastConfig = ToastConfig(
                flow = modelStore.data.map { !it.isNeutralColorSaturationLowEnough },
                message = "The neutral color has a quite high saturation. We would suggest to choose a color with a lower saturation."
            )
        ) {
            modelStore.data.map { it.neutralColorShadeList }.render { shadeList ->
                colorDisplay(shadeList = shadeList, handler = modelStore.copyColorToClipboard)
            }
        }

        section(
            number = 3,
            title = "Accent Colors",
            instruction = "If you need need to highlight something, select an accent color.",
            explanation = """In order to highlight something you probably don't want to use your primary color. So add one or more accent colors.
                    |Be aware that too many color will also not do the trick ;)""".trimMargin(),
            actions = listOf(
                Button(
                    text = "Derived from primary color",
                    floaterElement = {
                        modelStore.data.map { it.proposedAccentColors }
                            .renderEach(idProvider = { "proposedAccentColor_${it.color.hex()}" }) { color ->
                                div("w-full h-12 p-1") {
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
                    floaterOptions = Options(placement = Placement.BottomStart),
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
            ),
            contentBelow = true
        ) {
            div {
                modelStore.data.map { it.namedAccentColors.isEmpty() }.render { isEmpty ->
                    p {
                        if (isEmpty) {
                            +"No accent colors added yet."
                        } else {
                            +"The accent shades would look like this:"
                        }
                    }
                }
                modelStore.data.map { it.accentColorsShadeLists }
                    .renderEach(idProvider = {
                        "accent_color_${it.name}_${it.shadedColors.size}"
                    }) { shadeList ->
                        div("grid grid-cols-12 mt-2 gap-4") {
                            div("col-span-2 group flex justify-between") {
                                div("place-self-center") {
                                    +shadeList.name
                                }

                                div("place-self-center") {
                                    button(
                                        Button(
                                            customClass = "hidden group-hover:inline-block",
                                            icon = { iconEdit() },
                                            customCode = { clicks.map { shadeList.name } handledBy modelStore.renameAccentColor }
                                        )
                                    )
                                }
                            }
                            div("col-span-9 border rounded-lg p-2 shadow-inner") {
                                inlineStyle("max-width:46rem;")
                                colorList(
                                    width = 2.5,
                                    height = 2.5,
                                    shadeList.shadedColors,
                                    handler = modelStore.copyColorToClipboard
                                )
                            }
                            div("col-span-1 place-self-center") {
                                button(Button(
                                    icon = { iconTrash() },
                                    customCode = { clicks.map { shadeList.name } handledBy modelStore.removeAccentColor }
                                ))
                            }
                        }
                    }
            }
        }

        section(
            number = 4,
            title = "Options",
        ) {
            div("grid grid-cols-5 gap-4") {
                div("col-span-1") {
                    +"Include base color"
                }
                div("col-span-4") {
                    checkbox(
                        value = modelStore.data.map { it.enforcePrimaryColorInShades },
                        handler = modelStore.setPrimaryColorEnforcedInShades,
                        label = "Make sure, the primary color is part of the generated shades."
                    )
                }

                div("col-span-1") {
                    +"Shade count"
                }
                div("col-span-4 flex justify-between gap-4") {
                    label("block mb-2 text-sm text-gray-900") {
                        `for`("shade-count")
                        modelStore.data.map { it.shadeCount }.renderText(into = this)
                    }
                    input("w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer place-self-center") {
                        id("shade-count")
                        type("range")
                        min(SHADES_MIN.toString())
                        max(SHADES_MAX.toString())
                        value(modelStore.data.map { it.shadeCount.toString() })
                        changes.map { it.target.unsafeCast<HTMLInputElement>().value.toInt() } handledBy modelStore.updateShadeCount
                    }
                }
            }
        }

        section(
            number = 5,
            title = "Download",
        ) {
            div("grid grid-cols-12 gap-2") {
                for (generator in OutputGenerator.allGenerators) {
                    div("col-span-2") {
                        button(
                            Button(
                                customClass = "w-full",
                                icon = { iconDownload() },
                                text = generator.title,
                                customCode = { clicks.map { generator } handledBy modelStore.downloadOutput }
                            )
                        )
                    }
                    div("col-span-10 place-self-center w-full") {
                        +generator.description
                    }
                }
            }
        }
    }
    (document.getElementById("on_footer") as? HTMLElement)
        ?.style?.display = "block"
}
