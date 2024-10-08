package org.olafneumann.palette.app

import dev.fritz2.core.Handler
import dev.fritz2.core.HtmlTag
import dev.fritz2.core.Id
import dev.fritz2.core.RenderContext
import dev.fritz2.core.checked
import dev.fritz2.core.d
import dev.fritz2.core.fill
import dev.fritz2.core.`for`
import dev.fritz2.core.id
import dev.fritz2.core.type
import dev.fritz2.core.viewBox
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLabelElement


fun RenderContext.checkbox(value: Flow<Boolean>, handler: Handler<Boolean>? = null, label: String) =
    checkbox(value = value, handler = handler) { +label }

fun RenderContext.checkbox(value: Flow<Boolean>, handler: Handler<Boolean>? = null, label: HtmlTag<HTMLLabelElement>.() -> Unit) =
    div {
        val id = Id.next()
        className("flex items-start mb-6")
        div {
            className("flex-items-center h-5")
            input {
                className("w-4 h-4 border border-gray-300 rounded bg-gray-50 focus:ring-3 focus:ring-blue-300 dark:bg-gray-700 dark:border-gray-600 dark:focus:ring-blue-600 dark:ring-offset-gray-800")
                id(id)
                type("checkbox")
                checked(value)
                handler?.let { handler ->
                    changes.map { event -> event.target.unsafeCast<HTMLInputElement>().checked } handledBy handler
                }
            }
        }
        label {
            className("ms-2 text-sm font-medium text-gray-900 dark:text-gray-300")
            `for`(id)
            label()
        }
    }

fun RenderContext.warningToast(text: String) =
    div {
        className("flex items-center w-full max-w-xs p-4 text-gray-500 bg-white rounded-lg shadow dark:text-gray-400 dark:bg-gray-800")
        div {
            className("inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-orange-500 bg-orange-100 rounded-lg dark:bg-orange-700 dark:text-orange-200")
            svg {
                className("w-5 h-5")
                xmlns("http://www.w3.org/2000/svg")
                viewBox("0 0 20 20")
                path {
                    d("M10 .5a9.5 9.5 0 1 0 9.5 9.5A9.51 9.51 0 0 0 10 .5ZM10 15a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm1-4a1 1 0 0 1-2 0V6a1 1 0 0 1 2 0v5Z")
                }
                span {
                    className("sr-only")
                    +"Warning icon"
                }
            }
        }
        div {
            className("ms-3 text-sm font-normal")
            +text
        }
        button {
            className("ms-auto -mx-1.5 -my-1.5 bg-white text-gray-400 hover:text-gray-900 rounded-lg focus:ring-2 focus:ring-gray-300 p-1.5 hover:bg-gray-100 inline-flex items-center justify-center h-8 w-8 dark:text-gray-500 dark:hover:text-white dark:bg-gray-800 dark:hover:bg-gray-700")
            type("button")
            span {
                className("sr-only")
                +"Close"
            }
            svg {
                className("h-3 w-3")
                xmlns("http://www.w3.org/2000/svg")
                fill("none")
                viewBox("0 0 14 14")
                path {
                    d("m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6")
                    attr("stroke", "currentColor")
                    attr("stroke-linecap", "round")
                    attr("stroke-linejoin", "round")
                    attr("stroke-width", "2")
                }
            }
        }
    }