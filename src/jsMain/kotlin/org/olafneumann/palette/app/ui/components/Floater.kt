package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.Handler
import dev.fritz2.core.HtmlTag
import dev.fritz2.core.RootStore
import dev.fritz2.core.SimpleHandler
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.olafneumann.palette.app.npm.Middleware
import org.olafneumann.palette.app.npm.computePosition
import org.olafneumann.palette.app.npm.flip
import org.olafneumann.palette.app.npm.offset
import org.olafneumann.palette.app.npm.shift
import org.olafneumann.palette.app.utils.toJson
import org.w3c.dom.HTMLElement

@Suppress("UnusedPrivateProperty")
private const val KEYCODE_ESCAPE = 0x0001
private const val DEFAULT_OFFSET = 5

data class Options(
    val placement: Placement = Placement.Top,
    val middleware: Array<Middleware> = arrayOf(offset(DEFAULT_OFFSET), flip(), shift()),
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
    Right, Bottom, Left, Top, RightStart, RightEnd, BottomStart, BottomEnd, LeftStart, LeftEnd, TopStart, TopEnd;

    var string: String =
        name.replace(Regex("[A-Z]"), "-$0")
            .replace(Regex("^-"), "")
            .lowercase()
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
    private val backgroundElementId: String? = null,
    private val options: Options = Options(),
) {
    private val referenceElement: HTMLElement by lazy {
        document.querySelector("#$referenceElementId") as HTMLElement
    }
    private val floatingElement: HTMLElement by lazy {
        document.querySelector("#$floatingElementId") as HTMLElement
    }
    private val backgroundElement: HTMLElement? by lazy {
        document.querySelector("#$backgroundElementId") as? HTMLElement
    }

    private val floaterStore = FloaterStore()

    private var timeoutHandle: Int? = null

    init {
        runLater { initialize() }
    }

    private fun initialize() {
        floatingElement.style.display = "none"
        floatingElement.setOpacity(show = false)
        floatingElement.style.width = "max-content"
        floatingElement.style.position = "absolute"
        floatingElement.style.top = "0"
        floatingElement.style.left = "0"
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
            backgroundElement?.style?.display = "block"
            floatingElement.style.display = "block"
            runLater {
                floatingElement.setOpacity(show)
            }
        } else {
            backgroundElement?.style?.display = "none"
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

    fun install(inHtmlTag: HtmlTag<*>, forType: FloaterEventType) {
        if (forType == FloaterEventType.MouseOver) {
            inHtmlTag.installForMouseOver()
        } else if (forType == FloaterEventType.Click) {
            inHtmlTag.installForClick()
        }
    }

    private fun HtmlTag<*>.installForMouseOver() {
        mouseenters.map { true } handledBy floaterStore.update
        mouseleaves.map { false } handledBy floaterStore.update
        blurs.map { false } handledBy floaterStore.update
    }

    private fun HtmlTag<*>.installForClick() {
        clicks.map { true } handledBy floaterStore.toggle
        blurs.map { false } handledBy floaterStore.update
        // TODO keydowns.filter { it.keyCode == KEYCODE_ESCAPE }.map { false } handledBy floaterStore.update
    }

    companion object {
        private const val TIMEOUT = 300
    }

    private inner class FloaterStore : RootStore<Boolean>(false, job = Job()) {
        override val update: SimpleHandler<Boolean> = handle { _, newValue ->
            showOrHide(show = newValue)
            newValue
        }
        val toggle: Handler<Boolean> = handle { current, _ ->
            showOrHide(show = !current)
            !current
        }
    }

}

enum class FloaterEventType {
    MouseOver, Click
}
