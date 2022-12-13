package com.ibis.geometry.common

data class SvgVideo(val drawer: SvgDrawer, val rate: Number, var frame: Int): Screenshot() {
    fun frame(content: SvgDrawer.() -> Unit) {
        drawer.write("g", *if (frame == 0) arrayOf() else arrayOf("opacity" to 0)) {
            drawer.write(
                "set",
                "id" to "show$frame",
                "attributeName" to "opacity",
                "to" to 1,
                "begin" to if (frame == 0) "0ms;finish.begin" else "hide${frame-1}.begin",
                "dur" to "1ms",
                "fill" to "freeze"
            )
            drawer.write(
                "set",
                "id" to "hide$frame",
                "attributeName" to "opacity",
                "to" to 0,
                "begin" to "show$frame.begin + ${rate}ms",
                "dur" to "1ms",
                "fill" to "freeze"
            )
            drawer.content()
            frame++
        }
    }

    fun finish() {
        drawer.write(
            "set",
            "id" to "finish",
            "attributeName" to "opacity",
            "to" to 1,
            "begin" to "hide${frame-1}.begin",
            "dur" to "1ms"
        )
        drawer.finish()
    }
}
