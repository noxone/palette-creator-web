package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.HtmlTag
import dev.fritz2.core.RenderContext
import org.w3c.dom.HTMLDivElement

fun RenderContext.section(
    number: Int,
    title: String,
    instruction: String? = null,
    explanation: String? = null,
    content: HtmlTag<HTMLDivElement>.() -> Unit,
) =
    boxy {
        div("hidden lg:block") {
            div("on-title-font text-7xl antialiased text-slate-500 w-full text-center lining-nums mt-5") {
                +number.toString()
            }
        }
        div("col-span-12 lg:col-span-11") {
            content()
        }
    }

fun RenderContext.boxy(additionalClasses: String = "bg-slate-50 grid grid-cols-12", content: HtmlTag<HTMLDivElement>.() -> Unit) =
    div {
        classList(listOf("my-4 p-4 md:rounded-xl shadow-xl", additionalClasses))
        content()
    }

