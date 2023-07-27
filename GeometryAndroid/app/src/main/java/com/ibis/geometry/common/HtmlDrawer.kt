package com.ibis.geometry.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import java.io.OutputStreamWriter

class HtmlDrawer(size: Size, val writer: OutputStreamWriter): Drawer {
    init {
        writer.write("<!DOCTYPE html>" +
                "<html>" +
                "<body>" +
                "<canvas id=\"canvas\" width=\"${size.width}\" height=\"${size.height}\"></canvas>" +
                "<script>" +
                "const ctx=document.getElementById(\"canvas\").getContext(\"2d\");" +
                "ctx.lineWidth=4.2;")
    }

    fun finish() {
        writer.write("</script></body></html>")
        writer.close()
    }

    override fun point(offset: Offset, color: Color) {
        writer.write("ctx.beginPath();" +
                "ctx.arc(${offset.x},${offset.y},9,0,2*Math.PI);" +
                "ctx.fillStyle=\"${color.svg}\";" +
                "ctx.fill();")
    }

    override fun circle(center: Offset, radius: Float, style: Style) = style.styled {
        writer.write("ctx.arc(${center.x},${center.y},$radius,0,2*Math.PI);")
    }

    override fun line(from: Offset, to: Offset, style: Style) = style.styled {
        writer.write("ctx.moveTo(${from.x},${from.y});" +
                "ctx.lineTo(${to.x},${to.y});")
    }

    override fun polygon(points: List<Offset>, style: Style) = style.styled {
        writer.write("ctx.moveTo(${points[0].x},${points[0].y});")
        points.drop(1).forEach {
            writer.write("ctx.lineTo(${it.x},${it.y});")
        }
        writer.write("ctx.closePath();")
    }

    override fun text(pos: Offset, text: List<String>, color: Color) {
        writer.write("ctx.beginPath();" +
                "var x=${pos.x + 12};")
        text.forEachIndexed { index, str ->
            writer.write("ctx.font=\"${if (index % 2 == 0) 40 else 30}px roboto\";" +
                    "ctx.fillText(\"$str\",x,${pos.y + if (index % 2 == 0) 12 else 24});" +
                    "x+=ctx.measureText(\"$str\").width;")
        }
    }

    override fun angle(center: Offset, from: Float, to: Float, style: Style) = style.styled {
        writer.write("ctx.arc(${center.x},${center.y},${50 * style.scale},${-from},${-to},${from < to});" +
                "ctx.lineTo(${center.x},${center.y});" +
                "ctx.closePath();")
    }

    private fun Style.styled(action: () -> Unit) = if (fill || border != Border.No) {
        writer.write("ctx.beginPath();")
        action()
        if (fill) {
            writer.write("ctx.fillStyle=${color.html(0.3f)};" +
                    "ctx.fill();")
        }
        if (border != Border.No) {
            border.effect?.toList().orEmpty().let {
                writer.write("ctx.setLineDash([${it.joinToString()}]);")
            }
            writer.write("ctx.strokeStyle=\"${color.svg}\";" +
                    "ctx.stroke();")
        } else Unit
    } else Unit
}

fun Color.html(alpha: Float) = "\"rgba(${red*255},${green*255},${blue*255},$alpha)\""
