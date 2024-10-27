package org.olafneumann.palette.model

import org.olafneumann.palette.colors.ShadeList
import kotlin.reflect.KClass
import kotlin.reflect.createInstance

interface OutputGenerator {
    val title: String
    val description: String
    fun generateOutput(model: PaletteModel): DownloadObject

    private class HexDescriptionGenerator : OutputGenerator {
        override val title: String
            get() = "Hex values"
        override val description: String
            get() = "Text file containing all RGB values for the generated colors."
        override fun generateOutput(model: PaletteModel): DownloadObject =
            DownloadObject(
                filename = "shades.txt",
                content = model.getShadeLists()
                    .joinToString { it.generateRgbDescription() }
            )


        private fun ShadeList.generateRgbDescription(): String =
            "[$name]\n" +
                    "base color: " + baseColor.hex() + "\n" +
                    shadedColors.joinToString(separator = "\n") { "${(it.shade * 1000).toInt()} : ${it.color.hex()}" } + "\n"
    }

    private class CssGenerator : OutputGenerator {
        override val title: String
            get() = "CSS"
        override val description: String
            get() = "CSS file containing several classes for each color to be used as background color, text color " +
                    "or border color."
        override fun generateOutput(model: PaletteModel): DownloadObject =
            DownloadObject(
                filename = "shades.css",
                content = model.getShadeLists()
                    .flatMap { it.generateCss() }
                    .joinToString(separator = "\n") { it }
            )

        private fun ShadeList.generateCss(): List<String> {
            val pairs = listOf(
                "bg" to "background-color",
                "text" to "color",
                "border" to "border-color",
            )
            val list = pairs.flatMap { generateCss(prefix = it.first, propertyName = it.second) }
            return list
        }

        private fun ShadeList.generateCss(prefix: String, propertyName: String): List<String> {
            val list = shadedColors
                .map { ".$prefix-$name-${(it.shade * 1000).toInt()} {\n\t$propertyName: ${it.color.hex()}\n}" }
            return list
        }
    }

    companion object {
        val allGenerators = listOf(HexDescriptionGenerator(), CssGenerator())
            .sortedBy { it.title.lowercase() }

        private fun PaletteModel.getShadeLists(): List<ShadeList> =
            listOf(primaryColorShadeList, neutralColorShadeList) + accentColorsShadeLists

        data class DownloadObject(
            val zipFilename: String? = null,
            val filename: String,
            val content: String,
        )
    }
}

