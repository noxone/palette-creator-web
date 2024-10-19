@file:JsModule("@floating-ui/dom")
@file:JsNonModule
package org.olafneumann.palette.app.npm

import org.w3c.dom.Element
import kotlin.js.Json
import kotlin.js.Promise

external fun computePosition(parent: Element, tooltip: Element, options: Json): Promise<Position>
external fun flip(): Middleware
external fun shift(): Middleware
external fun offset(offset: Int): Middleware
//external fun arrow(options: ArrowOptions): Middleware

external class Position {
    val x: Double
    val y: Double
    val placement: String
    val strategy: String
    //val middlewareData: dynamic
}

external class Middleware
