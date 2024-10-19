package org.olafneumann.palette.model

import org.olafneumann.palette.colors.ShadeList

fun PaletteModel.generateCss(): String {
    val shadeLists = (listOf(primaryColorShadeList, neutralColorShadeList) + accentColorsShadeLists)
    val list = shadeLists.flatMap { it.generateCss() }
    return list.joinToString(separator = "\n") { it }
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
        .map { ".$prefix-$name-${(it.shade * 1000).toInt()} {\n\t$propertyName: ${it.color.hex()}\n}" }
    return list
}
