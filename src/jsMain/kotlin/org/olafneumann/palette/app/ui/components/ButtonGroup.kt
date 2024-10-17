package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.Handler
import dev.fritz2.core.RenderContext
import dev.fritz2.core.`for`
import dev.fritz2.core.id
import dev.fritz2.core.type
import dev.fritz2.core.value
import dev.fritz2.core.values
import kotlinx.coroutines.flow.Flow
import org.olafneumann.palette.app.npm.Floater
import org.olafneumann.palette.app.npm.FloaterEventType
import org.olafneumann.palette.app.npm.Options
import org.olafneumann.palette.app.utils.IdGenerator
import org.w3c.dom.events.MouseEvent

enum class ButtonType {
    Button, ColorPicker
}

data class Button(
    val type: ButtonType = ButtonType.Button,
    val id: String = IdGenerator.next,
    val text: String? = null,
    val value: Flow<String>? = null,
    val mouseHandler: Handler<MouseEvent>? = null,
    val textHandler: Handler<String>? = null,
    val floaterElementId: String? = null,
    val floaterElement: (RenderContext.(id: String) -> Unit)? = null,
    val floaterOptions: Options? = null,
    val floaterEvents: List<FloaterEventType> = emptyList(),
    val floaterBlurOnOutsideClick: Boolean = true,
)

fun RenderContext.buttonGroup(buttons: List<Button>) =
    div {
        className("inline-flex rounded-md shadow-sm")

        for (button in buttons) {
            val classes = mutableListOf(
                "px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200",
                "hover:bg-gray-100 hover:text-blue-700",
                "focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700",
                "dark:bg-gray-800 dark:border-gray-700 dark:text-white",
                "dark:hover:text-white dark:hover:bg-gray-700",
                "dark:focus:ring-blue-500 dark:focus:text-white"
            )
            if (button == buttons.first()) {
                classes.add("rounded-s-lg")
            } else if (button == buttons.last()) {
                classes.add("rounded-e-lg")
            }

            if (button.type == ButtonType.Button) {
                buttonGroupButton(classes, button)
            } else if (button.type == ButtonType.ColorPicker) {
                buttonGroupColorPicker(classes, button)
            }
        }
    }

private fun RenderContext.buttonGroupButton(classes: List<String>, button: Button) =
    button {
        type("button")
        id(button.id)
        classList(classes)
        button.value?.renderText(into = this)
        button.text?.let { +it }
        button.mouseHandler?.let { clicks handledBy it }

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

            button.floaterEvents.forEach { floater.install(`in` = this, `for` = it) }
        }

        button.floaterElement?.let {
            val id = IdGenerator.next
            it(id)
            val floater = Floater(
                referenceElementId = button.id,
                floatingElementId = id,
                backgroundElementId = backgroundElementId,
                options = button.floaterOptions ?: Options()
            )

            button.floaterEvents.forEach { floater.install(`in` = this, `for` = it) }
        }
    }

private fun RenderContext.buttonGroupColorPicker(classes: MutableList<String>, button: Button) =
    label {
        classes.add("z-20 cursor-pointer")//relative
        classList(classes)

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
