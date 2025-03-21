package org.olafneumann.palette.app.utils

import org.w3c.dom.url.URLSearchParams
import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.format(digits: Int) =
    ((10.0.pow(digits) * this).roundToInt().toDouble() / 10.0.pow(digits)).toString()

fun URLSearchParams.toMap(): Map<String, String?> {
    return object : Map<String, String?> {
        override val entries: Set<Map.Entry<String, String?>>
            get() = TODO("Not yet implemented")
        override val keys: Set<String>
            get() = TODO("Not yet implemented")
        override val size: Int
            get() = TODO("Not yet implemented")
        override val values: Collection<String?>
            get() = TODO("Not yet implemented")

        override fun isEmpty(): Boolean {
            TODO("Not yet implemented")
        }

        override fun get(key: String): String? = this@toMap.get(key)

        override fun containsValue(value: String?): Boolean {
            TODO("Not yet implemented")
        }

        override fun containsKey(key: String): Boolean {
            TODO("Not yet implemented")
        }

    }
}
