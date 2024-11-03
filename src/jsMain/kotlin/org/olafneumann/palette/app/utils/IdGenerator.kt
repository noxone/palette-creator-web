package org.olafneumann.palette.app.utils

class IdGenerator(
    private val prefix: String = "",
    initialValue: Int = 0,
) {
    private var currentValue = initialValue
        get() {
            return ++field
        }

    val next get() = "${prefix}_${++currentValue}"

    companion object {
        private val defaultIdGenerator = IdGenerator(prefix = "on_")

        val next get() = defaultIdGenerator.next
    }
}
