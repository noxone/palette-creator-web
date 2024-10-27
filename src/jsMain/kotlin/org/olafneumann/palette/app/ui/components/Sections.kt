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
        div {
            className("hidden lg:block")
            div {
                className("on-title-font text-7xl antialiased text-slate-500 w-full text-center lining-nums mt-5")
                +number.toString()
            }
        }
        div {
            className("col-span-12 lg:col-span-11")
            div {
                className("mb-3")
                h2 {
                    className("on-title-font font-semibold text-3xl antialiased")
                    +title
                }
                instruction?.let { p { +it } }
            }

            div {
                className("text-sm text-slate-500 mb-2")
                explanation?.split("\n")?.forEach {
                    p { +it }
                }
            }

            content()
        }
    }

fun RenderContext.boxy(additionalClasses: String = "bg-slate-50 grid grid-cols-12", content: HtmlTag<HTMLDivElement>.() -> Unit) =
    div {
        classList(listOf("my-4 p-4 md:rounded-xl shadow-xl", additionalClasses))
        content()
    }

