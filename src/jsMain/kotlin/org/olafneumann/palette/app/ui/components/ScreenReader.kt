package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.RenderContext

fun RenderContext.screenReader(text: String) =
    span("sr-only") {
        +text
    }
