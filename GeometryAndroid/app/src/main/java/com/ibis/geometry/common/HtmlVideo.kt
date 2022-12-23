package com.ibis.geometry.common

import androidx.compose.ui.geometry.center

data class HtmlVideo(val drawer: HtmlDrawer, val rate: Number, var frame: Int): Screenshot.Record() {
    init {
        drawer.writer.write("""
            var time = 0;
            setInterval(function() {
            ctx.clearRect(${-drawer.size.center.x}, ${-drawer.size.center.y}, ${drawer.size.width}, ${drawer.size.height});
            switch(time) {
            
        """.trimIndent())
    }

    override fun frame(content: Drawer.() -> Unit) {
        drawer.writer.write("case $frame:\n")
        drawer.content()
        drawer.writer.write("break;\n")
        frame++
    }

    override fun finish() {
        drawer.writer.write("""
            }
            time = (time + 1) % $frame;
            }, $rate);
            
        """.trimIndent())
        drawer.finish()
    }
}
