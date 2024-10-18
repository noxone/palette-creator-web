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
            attr("stroke", "currentColor")
            attr("stroke-linecap", "round")
            attr("stroke-linejoin", "round")
            attr("stroke-width", "2")
            d("m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6")
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

fun RenderContext.iconDownload() =
    svg {
        className("w-5 h-5")
        xmlns("http://www.w3.org/2000/svg")
        viewBox("0 0 24 24")
        fill("none")
        path {
            attr("stroke", "currentColor")
            attr("stroke-linecap", "round")
            attr("stroke-linejoin", "round")
            attr("stroke-width", "2")
            d("M3 15C3 17.8284 3 19.2426 3.87868 20.1213C4.75736 21 6.17157 21 9 21H15C17.8284 21 19.2426 21 20.1213 20.1213C21 19.2426 21 17.8284 21 15")
        }
        path {
            attr("stroke", "currentColor")
            attr("stroke-linecap", "round")
            attr("stroke-linejoin", "round")
            attr("stroke-width", "2")
            d("M12 3V16M12 16L16 11.625M12 16L8 11.625")
        }
    }

fun RenderContext.iconEdit() =
    svg {
        className("w-5 h-5")
        xmlns("http://www.w3.org/2000/svg")
        viewBox("0 0 24 24")
        fill("none")
        path {
            attr("stroke", "currentColor")
            attr("stroke-linecap", "round")
            attr("stroke-linejoin", "round")
            attr("stroke-width", "2")
            d("m3.99 16.854-1.314 3.504a.75.75 0 0 0 .966.965l3.503-1.314a3 3 0 0 0 1.068-.687L18.36 9.175s-.354-1.061-1.414-2.122c-1.06-1.06-2.122-1.414-2.122-1.414L4.677 15.786a3 3 0 0 0-.687 1.068zm12.249-12.63 1.383-1.383c.248-.248.579-.406.925-.348.487.08 1.232.322 1.934 1.025.703.703.945 1.447 1.025 1.934.058.346-.1.677-.348.925L19.774 7.76s-.353-1.06-1.414-2.12c-1.06-1.062-2.121-1.415-2.121-1.415z")
        }
    }

// TODO: Create a generic ICON function so we don't need to rewrite the SVG stuff for each icon.
