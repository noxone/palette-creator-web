package org.olafneumann.palette.app

import org.olafneumann.palette.app.npm.JSZip
import org.olafneumann.palette.app.npm.generateAsync
import org.olafneumann.palette.app.npm.saveAs
import org.olafneumann.palette.colors.ShadeList
import org.olafneumann.palette.model.PaletteModel
import org.w3c.files.Blob
import kotlin.js.Promise

interface OutputGenerator {
    val title: String
    val description: String
    fun generateOutput(model: PaletteModel): Promise<FileOutput>

    private class HexDescriptionGenerator : OutputGenerator {
        override val title: String = "Hex values"
        override val description: String = "Text file containing all RGB values for the generated colors."

        override fun generateOutput(model: PaletteModel): Promise<FileOutput> =
            Promise { resolve, _ ->
                resolve(
                    File(
                        name = "shades.txt",
                        content = model.getShadeLists()
                            .joinToString(separator = "\n") { it.generateRgbDescription() }
                    )
                )
            }

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

        override fun generateOutput(model: PaletteModel): Promise<FileOutput> =
            Promise { resolve, _ ->
                resolve(
                    File(
                        name = "shades.css",
                        content = model.getShadeLists()
                            .flatMap { it.generateCss() }
                            .joinToString(separator = "\n") { it }
                    )
                )
            }

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

        override fun generateOutput(model: PaletteModel): Promise<FileOutput> =
            Promise { resolve, _ ->
                resolve(
                    File(
                        name = "tailwind.shades.json",
                        content = generateAllTailwindNumbers(model)
                    )
                )
            }

        private fun generateAllTailwindNumbers(model: PaletteModel): String =
            model.getShadeLists().joinToString(separator = ",\n") { it.generateTailwindNumbers() }

        private fun ShadeList.generateTailwindNumbers(): String =
            "\t\t'$name': {\n${shadedColors.joinToString(separator = ",\n") { "\t\t\t${(it.intShade)}: '${it.color.hex()}'" }}\n\t\t}"
    }

    fun startDownload(model: PaletteModel) {
        generateOutput(model = model)
            .then { file ->
                file.blob.then { blob ->
                    saveAs(blob, file.name)
                }
            }
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

        interface FileOutput {
            val name: String
            val blob: Promise<Blob>
        }

        private class File(
            override val name: String,
            content: String,
        ) : FileOutput {
            override val blob = Promise { resolve, _ -> resolve(content.toBlob()) }
        }

        private class ZipFile(
            override val name: String,
            files: List<File>,
        ) : FileOutput {
            override val blob =
                Promise { resolve, _ ->
                    Promise
                        .all(files.map { it.blob }.toTypedArray())
                        .then { blobs ->
                            val zip = JSZip()
                            for (index in files.indices) {
                                zip.file(files[index].name, blobs[index])
                            }
                            zip.generateAsync()
                                .then { resolve(it) }
                        }
                }
        }

        // TODO: Move reference to Blob to JS-part of code
        private fun String.toBlob(): Blob = Blob(arrayOf(encodeToByteArray()))
    }
}




