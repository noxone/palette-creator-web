package org.olafneumann.palette.app.npm

import kotlinx.browser.document
import org.olafneumann.palette.app.utils.toJson
import org.w3c.dom.HTMLElement

data class Options(
    val placement: Placement = Placement.right,
    val middleware: Array<Middleware> = arrayOf(offset(5), flip(), shift()),
) {
    private fun toMap(): Map<String, Any> =
        mapOf(
            "placement" to placement.string,
            "middleware" to middleware,
        )

    fun toJson() = toMap().toJson()
}

@Suppress("unused")
enum class Placement {
    right, bottom, left, top, rightStart, rightEnd, bottomStart, bottomEnd, leftStart, leftEnd, topStart, topEnd;

    var string: String =
        name.replace(Regex("[A-Z]"), "-$0").lowercase()
}

data class ShiftOptions(
    val padding: Int = 0
) {
    private fun toMap(): Map<String, Any> =
        mapOf(
            "padding" to padding,
        )

    fun toJson() = toMap().toJson()
}

//data class ArrowOptions(
//    val element: HTMLElement
//) {
//    private fun toMap(): Map<String, Any> =
//        mapOf(
//            "element" to element,
//        )
//
//    fun toJson() = toMap().toJson()
//}



class Floater(
    private val referenceElementId: String,
    private val floatingElementId: String,
    private val options: Options = Options())
{
    private val referenceElement: HTMLElement by lazy { document.querySelector("#$referenceElementId") as HTMLElement }
    private val floatingElement: HTMLElement by lazy { document.querySelector("#$floatingElementId") as HTMLElement }

    private fun update() {
        computePosition(referenceElement, floatingElement, options.toJson())
            .then { position ->
                floatingElement.style.left = "${position.x}px"
                floatingElement.style.top = "${position.y}px"
            }
    }

    fun show() {
        floatingElement.style.display = "block"
        update()
    }

    fun hide() {
        floatingElement.style.display = "none"
    }
}
