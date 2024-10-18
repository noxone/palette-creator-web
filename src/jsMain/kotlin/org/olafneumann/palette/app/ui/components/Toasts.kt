package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.RenderContext
import dev.fritz2.core.type

fun RenderContext.warningToast(text: String) =
    div {
        className("flex items-center w-full max-w-xs p-4 text-gray-500 bg-white rounded-lg shadow dark:text-gray-400 dark:bg-gray-800")
        div {
            className("inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-orange-500 bg-orange-100 rounded-lg dark:bg-orange-700 dark:text-orange-200")
            iconWarning()
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
            iconClose()
        }
    }