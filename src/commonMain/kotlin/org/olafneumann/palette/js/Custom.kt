package org.olafneumann.palette.js

import kotlin.js.Json
import kotlin.js.json
import kotlinx.browser.window

fun copyToClipboard(text: String, success: () -> Unit = {}) =
    window.navigator.clipboard
        .writeText(text)
        .then { success() }
        .catch(onRejected = { window.alert("Could not copy text: $it") })


fun Map<String, Any?>.toJson(): Json =
    json(
        pairs = this
            .filter { it.value != null }
            .map { it.key to it.value }
            .toTypedArray()
    )

