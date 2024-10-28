package org.olafneumann.palette.model

import org.olafneumann.palette.colors.ShadeList
import org.w3c.files.Blob

interface OutputGenerator {
    val title: String
    val description: String
    fun generateOutput(model: PaletteModel): DownloadObject

    private class HexDescriptionGenerator : OutputGenerator {
        override val title: String = "Hex values"
        override val description: String = "Text file containing all RGB values for the generated colors."

        override fun generateOutput(model: PaletteModel): DownloadObject =
            DownloadObject(
                filename = "shades.txt",
                stringContent = model.getShadeLists()
                    .joinToString(separator = "\n") { it.generateRgbDescription() }
            )


        private fun ShadeList.generateRgbDescription(): String =
            "[$name]\n" +
                    "base color: " + baseColor.hex() + "\n" +
                    shadedColors.joinToString(separator = "\n") { "${(it.shade * 1000).toInt()} : ${it.color.hex()}" } + "\n"
    }

    private class CssGenerator : OutputGenerator {
        override val title: String = "CSS"
        override val description: String =
            "CSS file containing several classes for each color to be used as background color, text color " +
                    "or border color."

        override fun generateOutput(model: PaletteModel): DownloadObject =
            DownloadObject(
                filename = "shades.css",
                stringContent = model.getShadeLists()
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
                .map { ".$prefix-$name-${it.intShade} {\n\t$propertyName: ${it.color.hex()};\n}" }
            return list
        }
    }

    private class TailwindConfigurationGenerator : OutputGenerator {
        override val title: String = "Tailwind config"
        override val description: String = "Part of the Tailwind configuration file that you can use to include in" +
                "your tailwind.config.js"

        override fun generateOutput(model: PaletteModel): DownloadObject =
            DownloadObject(
                filename = "tailwind.shades.json",
                stringContent = "theme: {\n\tcolors: {\n${generateAllTailwindNumbers(model)}\n\t}\n}"
            )

        private fun generateAllTailwindNumbers(model: PaletteModel): String =
            model.getShadeLists().joinToString(separator = ",\n") { it.generateTailwindNumbers() }

        private fun ShadeList.generateTailwindNumbers(): String =
            "\t\t'$name': {\n${shadedColors.joinToString(separator = ",\n") { "\t\t\t${(it.intShade)}: '${it.color.hex()}'" }}\n\t\t}"
    }

    companion object {
        val allGenerators = listOf(
            HexDescriptionGenerator(),
            CssGenerator(),
            TailwindConfigurationGenerator(),
        )
            .sortedBy { it.title.lowercase() }

        private fun PaletteModel.getShadeLists(): List<ShadeList> =
            listOf(primaryColorShadeList, neutralColorShadeList) + accentColorsShadeLists

        data class DownloadObject(
            val zipFilename: String? = null,
            val filename: String,
            val blob: Blob,
        ) {
            constructor(filename: String, stringContent: String) : this(
                filename = filename,
                blob = stringContent.toBlob()
            )
        }

        // TODO: Move reference to Blob to JS-part of code
        private fun String.toBlob(): Blob = Blob(arrayOf(encodeToByteArray()))
    }
}

