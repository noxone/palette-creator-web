package org.olafneumann.palette.app.npm

import dev.fritz2.core.Handler
import dev.fritz2.core.HtmlTag
import dev.fritz2.core.RenderContext
import dev.fritz2.core.RootStore
import dev.fritz2.core.SimpleHandler
import dev.fritz2.core.Store
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.olafneumann.palette.app.utils.toJson
import org.w3c.dom.HTMLButtonElement
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
    val padding: Int = 0,
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
    private val options: Options = Options(),
) {
    private val referenceElement: HTMLElement by lazy { document.querySelector("#$referenceElementId") as HTMLElement }
    private val floatingElement: HTMLElement by lazy { document.querySelector("#$floatingElementId") as HTMLElement }

    val store: Store<Boolean>

    private var timeoutHandle: Int? = null

    init {
        store = object : RootStore<Boolean>(false, job = Job()) {
            override val update: SimpleHandler<Boolean> = handle { _, newValue ->
                showOrHide(show = newValue)
                newValue
            }
        }

        runLater { initialize() }
    }

    private fun initialize() {
        floatingElement.style.display = "none"
        floatingElement.setOpacity(show = false)
    }

    private fun update() {
        computePosition(referenceElement, floatingElement, options.toJson())
            .then { position ->
                floatingElement.style.left = "${position.x}px"
                floatingElement.style.top = "${position.y}px"
            }
    }

    private fun showOrHide(show: Boolean) {
        animateShowHide(show = show)
        if (show) {
            update()
        }
    }

    fun show() {
        showOrHide(show = true)
    }

    fun hide() {
        showOrHide(show = false)
    }

    private fun animateShowHide(show: Boolean) {
        if (show) {
            floatingElement.style.display = "block"
            runLater {
                floatingElement.setOpacity(show)
            }
        } else {
            floatingElement.setOpacity(show)
            runLater(TIMEOUT) {
                floatingElement.style.display = "none"
            }
        }
    }

    private fun HTMLElement.setOpacity(show: Boolean) {
        classList.toggle("opacity-0", force = !show)
        classList.toggle("opacity-1", force = show)
    }

    private fun runLater(timeout: Int = 1, action: () -> Unit) {
        timeoutHandle?.let { window.clearTimeout(it) }
        timeoutHandle = window.setTimeout({
            action()
            timeoutHandle = null
        }, timeout)
    }

    fun install(`in`: HtmlTag<*>) {
        `in`.install()
    }

    private fun HtmlTag<*>.install() {
        mouseenters.map { true } handledBy store.update
        mouseleaves.map { false } handledBy store.update
        blurs.map { false } handledBy store.update
    }

    companion object {
        private const val TIMEOUT = 300
    }
}