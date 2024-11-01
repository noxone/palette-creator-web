package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.HtmlTag
import dev.fritz2.core.RenderContext
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.HTMLDivElement

fun RenderContext.section(
    number: Int,
    title: String,
    content: HtmlTag<HTMLDivElement>.() -> Unit,
) =
    boxy {
        div("hidden xl:block") {
            div("on-title-font text-7xl antialiased text-slate-500 w-full text-center lining-nums mt-5") {
                +number.toString()
            }
        }
        div("col-span-12 xl:col-span-11") {
            h2("on-title-font font-semibold text-3xl antialiased mb-4") { +title }
            content()
        }
    }

data class ToastConfig(
    val flow: Flow<Boolean>,
    val message: String,
)

@Suppress("LongParameterList")
fun RenderContext.section(
    number: Int,
    title: String,
    instruction: String,
    explanation: String,
    actions: List<Button>,
    toastConfig: ToastConfig? = null,
    contentBelow: Boolean = false,
    customContent: (RenderContext.() -> Unit)? = null,
) =
    section(
        number = number,
        title = title,
    ) {
        val classList = mutableListOf("grid grid-cols-6 gap-4")
        if (!contentBelow) {
            classList.add("lg:grid-cols-12")
        }
        div {
            classList(classList)
            div("col-span-6 grid gap-3") {
                p { +instruction }
                div("text-sm text-slate-500") {
                    explanation.split("\n")
                        .forEach { p("mb-1 last:mb-0") { +it } }
                }

                div("w-fit border rounded-xl p-3 shadow-inner bg-slate-100 grid gap-2") {
                    p("text-sm font-normal") { +"Possible actions:" }
                    buttonGroup(buttons = actions)
                    toastConfig?.let { toast -> toast.flow.renderTrue { warningToast(toast.message) } }
                }
            }

            div {
                classList(if (contentBelow) listOf("col-span-full") else listOf("col-span-6"))
                customContent?.invoke(this)
            }
        }
    }


private fun RenderContext.boxy(
    additionalClasses: String = "bg-slate-50 grid grid-cols-12",
    content: HtmlTag<HTMLDivElement>.() -> Unit,
) =
    div {
        classList(listOf("my-4 p-4 md:rounded-xl shadow-xl", additionalClasses))
        content()
    }

