package org.olafneumann.palette.app.npm

import kotlinx.browser.document
import org.olafneumann.palette.app.utils.toJson
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

@JsModule("@popperjs/core")
@JsNonModule
external class Popper {
    companion object {
        fun createPopper(parent: Element, tooltip: Element, options: dynamic = definedExternally)
    }
}

/*fun Popper.Companion.createPopper(parent: HTMLElement, tooltip: HTMLElement, options: Options = Options()) {
    createPopper(parent, tooltip, options.toJson())
}*/

fun Popper.Companion.createPopper(parentID: String, tooltipId: String, options: Options = Options()) {
    val parent = document.querySelector("#$parentID")
    val tooltip = document.querySelector("#$tooltipId")
    createPopper(parent!!, tooltip!!, options.toJson())
}

data class Options(
    val placement: String = "left",
) {
    private fun toMap(): Map<String, String> =
        mapOf("placement" to placement)

    fun toJson() = toMap().toJson()
}
