package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.Handler
import dev.fritz2.core.HtmlTag
import dev.fritz2.core.RenderContext
import dev.fritz2.core.checked
import dev.fritz2.core.`for`
import dev.fritz2.core.id
import dev.fritz2.core.type
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.olafneumann.palette.app.utils.IdGenerator
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLabelElement

fun RenderContext.checkbox(
    value: Flow<Boolean>,
    handler: Handler<Boolean>? = null,
    label: String,
    explanation: String? = null,
) =
    checkbox(value = value, handler = handler, label = { +label }, explanation = explanation)

fun RenderContext.checkbox(
    value: Flow<Boolean>,
    handler: Handler<Boolean>? = null,
    label: HtmlTag<HTMLLabelElement>.() -> Unit,
    explanation: String? = null,
) =
    div {
        val id = IdGenerator.next
        className("flex items-start")
        div {
            className("flex-items-center h-5")
            input {
                classList(
                    listOf(
                        "w-4 h-4 border border-gray-300 rounded bg-gray-50",
                        "focus:ring-3 focus:ring-blue-300",
                        //"dark:bg-gray-700 dark:border-gray-600 dark:ring-offset-gray-800",
                        //"dark:focus:ring-blue-600"
                    )
                )
                id(id)
                type("checkbox")
                checked(value)
                handler?.let { handler ->
                    changes.map { event -> event.target.unsafeCast<HTMLInputElement>().checked } handledBy handler
                }
            }
        }
        div("ms-2") {
            label("text-gray-900") {
                `for`(id)
                label()
            }
            explanation?.let {
                p("text-sm text-slate-600") {
                    +explanation
                }
            }
        }
    }
