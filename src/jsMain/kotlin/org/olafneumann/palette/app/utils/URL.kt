package org.olafneumann.palette.app.utils

import kotlinx.browser.window
import org.w3c.dom.url.URL

private val URL_CURRENT = URL(window.location.toString())
fun URL.toCurrentWindowLocation(): URL {
    val url = URL(this.toString())
    url.protocol = URL_CURRENT.protocol
    url.hostname = URL_CURRENT.hostname
    url.port = URL_CURRENT.port
    return url
}
