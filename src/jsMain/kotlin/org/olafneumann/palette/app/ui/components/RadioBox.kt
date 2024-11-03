package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.RenderContext
import dev.fritz2.core.`for`
import dev.fritz2.core.id
import dev.fritz2.core.name
import dev.fritz2.core.type
import dev.fritz2.core.value
import org.olafneumann.palette.app.utils.IdGenerator

data class RadioBox(
    val name: String,
    val value: String,
    val text: String,
    val description: String? = null,
)

fun RenderContext.radioBoxes(vararg radioboxes: RadioBox) =
    div {
        radioboxes.forEach { radioBox(radioBox = it) }
    }

fun RenderContext.radioBox(radioBox: RadioBox) =
    radioBox(name = radioBox.name, value = radioBox.value, text = radioBox.text, description = radioBox.description)

fun RenderContext.radioBox(name: String, value: String, text: String, description: String? = null) =
    div("flex mb-2 last:mb-0") {
        val id = IdGenerator.next
        div("items-center pt-0.5") {
            input {
                classList(
                    listOf(
                        "appearance-none shrink-0",
                        "w-4 h-4 checked:border-2 border-white rounded-full checked:bg-blue-500 ring-blue-500 ring-2"
                    )
                )
                type("radio")
                id(id)
                name(name)
                value(value)
            }
        }
        div("ms-2 test-sm") {
            label("text-gray-900") {
                `for`(id)
                +text
            }
            description?.let {
                p("text-xs font-extralight text-gray-500") {
                    +it
                }
            }
        }
    }
