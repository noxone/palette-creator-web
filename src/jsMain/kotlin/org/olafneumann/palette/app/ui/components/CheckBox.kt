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
    div("flex items-start") {
        val id = IdGenerator.next
        div("flex flex-items-center h-5 pt-1") {
            input {
                classList(
                    listOf(
                        "peer", "relative",
                        "appearance-none shrink-0 w-4 h-4 rounded checked:bg-blue-500 ring-blue-500 ring-2"
                    )
                )
                id(id)
                type("checkbox")
                checked(value)
                handler?.let { handler ->
                    changes.map { event -> event.target.unsafeCast<HTMLInputElement>().checked } handledBy handler
                }
            }
            checkMark("hidden peer-checked:block absolute w-4 h-4 pointer-events-none text-white")
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
