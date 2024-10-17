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
            attr("fill-rule", "evenodd")
            attr("clip-rule", "evenodd")
            d("M20.8477 1.87868C19.6761 0.707109 17.7766 0.707105 16.605 1.87868L2.44744 16.0363C2.02864 16.4551 1.74317 16.9885 1.62702 17.5692L1.03995 20.5046C0.760062 21.904 1.9939 23.1379 3.39334 22.858L6.32868 22.2709C6.90945 22.1548 7.44285 21.8693 7.86165 21.4505L22.0192 7.29289C23.1908 6.12132 23.1908 4.22183 22.0192 3.05025L20.8477 1.87868ZM18.0192 3.29289C18.4098 2.90237 19.0429 2.90237 19.4335 3.29289L20.605 4.46447C20.9956 4.85499 20.9956 5.48815 20.605 5.87868L17.9334 8.55027L15.3477 5.96448L18.0192 3.29289ZM13.9334 7.3787L3.86165 17.4505C3.72205 17.5901 3.6269 17.7679 3.58818 17.9615L3.00111 20.8968L5.93645 20.3097C6.13004 20.271 6.30784 20.1759 6.44744 20.0363L16.5192 9.96448L13.9334 7.3787Z")
        }
    }


// TODO: Create a generic ICON function so we don't need to rewrite the SVG stuff for each icon.
