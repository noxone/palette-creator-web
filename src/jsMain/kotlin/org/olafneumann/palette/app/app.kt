package org.olafneumann.palette.app

import dev.fritz2.core.Handler
import dev.fritz2.core.RootStore
import dev.fritz2.core.Window
import dev.fritz2.core.`for`
import dev.fritz2.core.href
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
import org.olafneumann.palette.app.ui.components.ColorBoxType
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
import org.olafneumann.palette.app.ui.components.tableRow
import org.olafneumann.palette.app.utils.copyToClipboard
import org.olafneumann.palette.app.utils.toCurrentWindowLocation
import org.olafneumann.palette.app.utils.toMap
import org.olafneumann.palette.colorful.Color
import org.olafneumann.palette.colors.ColorGenerator
import org.olafneumann.palette.colors.fittingFontColor
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

// TODO: (bg|text|border)-(?!color|center|none|primary)[a-z]{4,}

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
        val setUsePredefinedShades: Handler<Boolean> = handle { model: PaletteModel, action: Boolean ->
            model.copy(usePredefinedShades = action)
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
            generator.startDownload(model)
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
                    width = "w-3",
                    height = "h-10",
                    colors = (0..<colorCount).map {
                        Color.hsluv(
                            h = 290.0 / colorCount * it,
                            s = 0.1 + 0.85 / colorCount * it,
                            l = 0.7
                        )
                    })
            }
        }

        div("fixed top-0 left-0 right-0") {
            p("sm:hidden") { +"xs" }
            p("hidden sm:block md:hidden") { +"sm" }
            p("hidden md:block lg:hidden") { +"md" }
            p("hidden lg:block xl:hidden") { +"lg" }
            p("hidden xl:block 2xl:hidden") { +"xl" }
            p("hidden 2xl:block") { +"2xl" }
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
            explanation = """True black or white often looks strange to the eye, so we should go with some other very dark or light colors. These shades may also be used for neutral borders or slight highlighting.
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
            explanation = """In order to highlight something you probably don't want to use your primary color. So, please add one or more accent colors.
                    |Do not solely rely on accent colors. Express specific conditions through multiple options, as colors are used and understood differently in different parts of the world. 
                    |Be aware that too many color will also not do the trick ;)""".trimMargin(),
            actions = listOf(
                Button(
                    // TODO: Disable this button of there are not colors...
                    text = "Derived from primary color",
                    floaterElement = {
                        modelStore.data.map { it.proposedAccentColors }
                            .renderEach(idProvider = { "proposedAccentColor_${it.color.hex()}" }) { color ->
                                div("w-full h-12 p-1") {
                                    colorBox(
                                        type = ColorBoxType.Button,
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
            modelStore.data.map { it.namedAccentColors.isEmpty() }.renderTrue {
                p { +"No accent colors added yet." }
            }
            modelStore.data.map { it.accentColorsShadeLists }
                .renderEach(idProvider = {
                    "accent_color_${it.name}_${it.shadedColors.size}"
                }) { shadeList ->
                    tableRow("group/buttons") {
                        div("col-span-3 flex justify-between") {
                            div("place-self-center") {
                                +shadeList.name
                            }

                            div("place-self-center") {
                                buttonGroup(
                                    Button(
                                        customClass = "hidden group-hover/buttons:inline-block",
                                        icon = { iconEdit() },
                                        textHandler = modelStore.renameAccentColor,
                                        stringMapper = { shadeList.name }
                                    ), Button(
                                        customClass = "hidden group-hover/buttons:inline-block",
                                        icon = { iconTrash() },
                                        textHandler = modelStore.removeAccentColor,
                                        stringMapper = { shadeList.name }
                                    ))
                            }
                        }
                        div("col-span-9") {
                            colorDisplay(
                                shadeList = shadeList,
                                vertical = true,
                                handler = modelStore.copyColorToClipboard
                            )
                        }
                    }
                }
        }

        section(
            number = 4,
            title = "Options",
        ) {
            div {
                tableRow("grid grid-cols-5 gap-4") {
                    div("col-span-1") {
                        +"Include base color"
                    }
                    div("col-span-4") {
                        checkbox(
                            value = modelStore.data.map { it.enforcePrimaryColorInShades },
                            handler = modelStore.setPrimaryColorEnforcedInShades,
                            label = "Make sure, the primary color is part of the generated shades.",
                            explanation = "If checked, the selected primary, neutral or accent color will explicitly be part of the list of shades. If unchecked, we will just use the hue and saturation and adjust the luminance accordingly.",
                        )
                    }
                }
                /*tableRow("grid grid-cols-5 gap-4") {
                    div("col-span-1") {
                        +"Predefined shades"
                    }
                    div("col-span-4") {
                        checkbox(
                            value = modelStore.data.map { it.usePredefinedShades },
                            handler = modelStore.setUsePredefinedShades,
                            label = "Use predefined shades",
                            explanation = "When checked, we will use predefined shades for each color. If unchecked we will simply distribute the shades equally across the all luminance levels."
                        )
                    }
                }*/
                tableRow("grid grid-cols-5 gap-4") {
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
        }

        section(
            number = 5,
            title = "Download",
        ) {
            p("mb-3") { +"The following download options prepare the colors in certain ways that you might need." }
            div {
                for (generator in OutputGenerator.allGenerators) {
                    tableRow {
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
            p("mt-3 text-sm text-slate-600") {
                +"If you are missing a download functionality or maybe some IDE integration, please open a "
                a("hover:underline hover:text-blue-700") {
                    +"Github issue"
                    href("https://github.com/noxone/palette-creator-web/issues")
                }
                +" to propose a change."
            }
        }
    }
    (document.getElementById("on_footer") as? HTMLElement)
        ?.style?.display = "block"
}

