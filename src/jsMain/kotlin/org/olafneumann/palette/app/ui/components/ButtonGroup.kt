package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.Handler
import dev.fritz2.core.HtmlTag
import dev.fritz2.core.RenderContext
import dev.fritz2.core.`for`
import dev.fritz2.core.id
import dev.fritz2.core.type
import dev.fritz2.core.value
import dev.fritz2.core.values
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.olafneumann.palette.app.utils.IdGenerator
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.MouseEvent

private val defaultButtonClasses = listOf(
    "p-2 text-sm font-normal text-gray-900 bg-white border-gray-200",
    //"border-t border-b border-s last:border-e shadow-sm",
    "border-e last:border-none shadow",
    "hover:bg-gray-100 hover:text-blue-700",
    //"focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700",
    //"dark:bg-gray-800 dark:border-gray-700 dark:text-white",
    //"dark:hover:text-white dark:hover:bg-gray-700",
    //"dark:focus:ring-blue-500 dark:focus:text-white",
    "first:rounded-s-lg last:rounded-e-lg"
)

enum class ButtonType {
    Button, ColorPicker
}

data class Button(
    val type: ButtonType = ButtonType.Button,
    val customClass: String = "",
    val id: String = IdGenerator.next,
    val icon: (RenderContext.() -> Unit)? = null,
    val text: String? = null,
    val value: Flow<String>? = null,
    val clickHandler: Handler<Unit>? = null,
    val textHandler: Handler<String>? = null,
    val floaterElementId: String? = null,
    val floaterElement: (HtmlTag<HTMLDivElement>.() -> Unit)? = null,
    val floaterOptions: Options? = null,
    val floaterEvents: List<FloaterEventType> = emptyList(),
    val floaterBlurOnOutsideClick: Boolean = true,
    val customCode: (RenderContext.() -> Unit)? = null,
    val stringMapper: ((Any) -> String)? = null,
)

@Suppress("CyclomaticComplexMethod")
fun RenderContext.button(button: Button, additionalClasses: List<String> = emptyList()) =
    button {
        type("button")
        id(button.id)
        classList(defaultButtonClasses + listOf(button.customClass) + additionalClasses)
        button.value?.renderText(into = this)
        button.icon?.let { icon ->
            span { icon() }
        }
        button.text?.let { text ->
            span { +text }
        }
        button.customCode?.let { it() }
        if (button.stringMapper == null) {
            button.clickHandler?.let { clicks handledBy it }
        } else {
            button.textHandler?.let { clicks.map { click -> button.stringMapper.invoke(click) } handledBy it }
        }

        var backgroundElementId: String? = null
        if (button.floaterBlurOnOutsideClick && (button.floaterElement != null || button.floaterElementId != null)) {
            backgroundElementId = IdGenerator.next
            div("on-background-overlay") { id(backgroundElementId) }
        }

        button.floaterElementId?.let { floatingElementId ->
            val floater = Floater(
                referenceElementId = button.id,
                floatingElementId = floatingElementId,
                options = button.floaterOptions ?: Options()
            )

            button.floaterEvents.forEach { floater.install(inHtmlTag = this, forType = it) }
        }

        button.floaterElement?.let {
            val id = IdGenerator.next
            val floater = Floater(
                referenceElementId = button.id,
                floatingElementId = id,
                backgroundElementId = backgroundElementId,
                options = button.floaterOptions ?: Options()
            )
            createFloater(id = id) {
                it(this@createFloater)
            }

            button.floaterEvents.forEach { floater.install(inHtmlTag = this, forType = it) }
        }
    }

private fun RenderContext.colorPicker(button: Button, additionalClasses: List<String> = emptyList()) =
    label {
        val realClasses = defaultButtonClasses + listOf("z-20 cursor-pointer") + additionalClasses
        classList(realClasses + listOf(button.customClass))

        inlineStyle("position:relative;")
        `for`(button.id)
        div {
            className("flex flex-wrap content-center justify-center h-full")
            button.text?.let { +it }
        }
        div {
            className("flex flex-wrap content-center justify-center z-0")
            inlineStyle("position:absolute;left:0;right:0;top:0;bottom:0;")
            input {
                type("color")
                className("cursor-pointer")
                inlineStyle("opacity:0;")
                id(button.id)
                button.value?.let { value(it) }
                button.textHandler?.let { changes.values() handledBy it }
            }
        }
    }


private fun RenderContext.createFloater(id: String, content: (HtmlTag<HTMLDivElement>.() -> Unit)) =
    div {
        id(id)
        div {
            classList(
                listOf(
                    "shadow-xl z-10 inline-block w-48 text-sm text-gray-500 bg-white border border-gray-200 rounded-lg",
                    "transition-opacity duration-300"
                    //"dark:text-gray-400 dark:border-gray-600 dark:bg-gray-800"
                )
            )

            content(this)
        }
    }

fun RenderContext.buttonGroup(classes: List<String> = emptyList(), vararg buttons: Button) =
    buttonGroup(classes = classes, buttons = buttons.asList())

fun RenderContext.buttonGroup(classes: List<String> = emptyList(), buttons: Collection<Button>) =
    div {
        classList(classes)
        div {
            className("inline-flex shadow rounded-lg *:shadow-none")

            for (button in buttons) {
                val additionalClasses = if (button.text != null) listOf("px-4") else emptyList()

                if (button.type == ButtonType.Button) {
                    button(button = button, additionalClasses = additionalClasses)
                } else if (button.type == ButtonType.ColorPicker) {
                    colorPicker(button = button, additionalClasses = additionalClasses)
                }
            }
        }
    }
