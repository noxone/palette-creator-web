package org.olafneumann.palette.js

import kotlinx.browser.window

fun copyToClipboard(text: String, success: () -> Unit = {}) =
    window.navigator.clipboard
        .writeText(text)
        .then { success() }
        .catch(onRejected = { window.alert("Could not copy text: $it") })
