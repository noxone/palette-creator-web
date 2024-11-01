package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.HtmlTag
import dev.fritz2.core.RenderContext
import org.w3c.dom.HTMLDivElement

fun RenderContext.tableRow(vararg classes: String, content: HtmlTag<HTMLDivElement>.() -> Unit) =
    div("grid grid-cols-12 gap-2 sm:gap-4 p-2 first:rounded-t-xl last:rounded-b-xl first:border-t last:border-b border-s border-e even:bg-slate-100") {
        classList(classes.asList())
        content.invoke(this)
    }