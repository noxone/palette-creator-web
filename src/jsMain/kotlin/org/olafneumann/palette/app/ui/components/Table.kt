package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.RenderContext

fun RenderContext.tableRow(vararg classes: String, content: RenderContext.() -> Unit) =
    div("grid grid-cols-12 gap-4 p-2 first:rounded-t-xl last:rounded-b-xl first:border-t last:border-b border-s border-e even:bg-slate-100") {
        classList(classes.asList())
        content.invoke(this)
    }