package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.Handler
import dev.fritz2.core.RenderContext
import dev.fritz2.core.type

fun RenderContext.warningToast(text: String, maxWidth: Boolean = false, closeHandler: Handler<Any>? = null) =
    div {
        val classList = mutableListOf(
            "flex items-center w-full p-4 bg-orange-50 rounded-lg shadow",
            //"dark:text-gray-400 dark:bg-gray-800"
        )
        if (maxWidth) {
            classList.add("max-w-xs")
        }
        classList(classList)
        div {
            classList(
                listOf(
                    "inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-orange-500 bg-orange-200 rounded-lg",
                    //"dark:bg-orange-700 dark:text-orange-200"
                )
            )
            iconWarning()
        }
        div("ms-3 text-sm font-normal") {
            +text
        }
        closeHandler?.let {
            // TODO: Replace by normal button
            button {
                classList(
                    listOf(
                        "ms-auto -mx-1.5 -my-1.5 bg-white text-gray-400 rounded-lg p-1.5 inline-flex items-center justify-center h-8 w-8",
                        "hover:text-gray-900 hover:bg-gray-100",
                        //"focus:ring-2 focus:ring-gray-300",
                        //"dark:text-gray-500 dark:bg-gray-800",
                        //"dark:hover:text-white dark:hover:bg-gray-700",
                    )
                )
                type("button")
                screenReader("Close")
                iconClose()
                clicks handledBy it
            }
        }
    }
