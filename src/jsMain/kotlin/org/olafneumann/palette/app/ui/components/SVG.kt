package org.olafneumann.palette.app.ui.components

import dev.fritz2.core.RenderContext
import dev.fritz2.core.d
import dev.fritz2.core.fill
import dev.fritz2.core.viewBox

fun RenderContext.iconClose() =
    svg {
        className("h-3 w-3")
        xmlns("http://www.w3.org/2000/svg")
        fill("none")
        viewBox("0 0 14 14")
        path {
            d("m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6")
            attr("stroke", "currentColor")
            attr("stroke-linecap", "round")
            attr("stroke-linejoin", "round")
            attr("stroke-width", "2")
        }
    }

fun RenderContext.iconWarning() =
    svg {
        className("w-5 h-5")
        xmlns("http://www.w3.org/2000/svg")
        viewBox("0 0 20 20")
        path {
            d("M10 .5a9.5 9.5 0 1 0 9.5 9.5A9.51 9.51 0 0 0 10 .5ZM10 15a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm1-4a1 1 0 0 1-2 0V6a1 1 0 0 1 2 0v5Z")
        }
        span {
            className("sr-only")
            +"Warning icon"
        }
    }

fun RenderContext.iconTrash() =
    svg {
        className("h-5 w-5")
        xmlns("http://www.w3.org/2000/svg")
        viewBox("0 0 16 16")
        fill("currentColor")
        path {
            d("M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z")
        }
        path {
            d("M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z")
        }
    }

// TODO: Create a generic ICON function so we don't need to rewrite the SVG stuff for each icon.
