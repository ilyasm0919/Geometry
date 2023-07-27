package com.ibis.geometry.common

class HtmlVideo(val drawer: TransformationDrawer<HtmlDrawer>, val rate: Number, var frame: Int): Screenshot.Record() {
    init {
        drawer.drawer.writer.write("var play=true;" +
                "document.onkeyup=function(e){if(e.keyCode==32)play=!play;};" +
                "document.onclick=function(){play=!play;};" +
                "var time=0;" +
                "setInterval(function(){" +
                "if(!play)return;" +
                "ctx.clearRect(0,0,${drawer.size.width},${drawer.size.height});" +
                "switch(time){")
    }

    override fun frame(content: Drawer.() -> Unit) {
        drawer.drawer.writer.write("case $frame:")
        drawer.content()
        drawer.drawer.writer.write("break;")
        frame++
    }

    override fun finish() {
        drawer.drawer.writer.write("}time = (time+1)%$frame;},$rate);")
        drawer.drawer.finish()
    }
}
