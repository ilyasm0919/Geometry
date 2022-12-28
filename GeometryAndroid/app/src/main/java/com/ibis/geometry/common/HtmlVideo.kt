package com.ibis.geometry.common

data class HtmlVideo(val drawer: HtmlDrawer, val rate: Number, var frame: Int): Screenshot.Record() {
    init {
        drawer.writer.write("var play=true;" +
                "document.onkeyup=function(e){if(e.keyCode==32)play=!play;};" +
                "document.onclick=function(){play=!play;};" +
                "var time=0;" +
                "setInterval(function(){" +
                "if(!play)return;" +
                "ctx.clearRect(${drawer.bounds.left},${drawer.bounds.right},${drawer.bounds.width},${drawer.bounds.height});" +
                "switch(time){")
    }

    override fun frame(content: Drawer.() -> Unit) {
        drawer.writer.write("case $frame:")
        drawer.content()
        drawer.writer.write("break;")
        frame++
    }

    override fun finish() {
        drawer.writer.write("}time = (time+1)%$frame;},$rate);")
        drawer.finish()
    }
}
