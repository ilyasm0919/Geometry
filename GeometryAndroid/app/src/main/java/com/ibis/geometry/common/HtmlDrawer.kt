package com.ibis.geometry.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import java.io.OutputStreamWriter

class HtmlDrawer(size: Size, val writer: OutputStreamWriter): Drawer {
    override val size = size * 200f / size.minDimension

    init {
        writer.write("""
            <!DOCTYPE html>
            <html>
            <body>
            <canvas id="canvas" width="${size.width}" height="${size.height}"></canvas>
            <script>
            const ctx = document.getElementById("canvas").getContext("2d");
            ctx.translate(${size.center.x}, ${size.center.y});
            ctx.scale(${size.minDimension / 200}, ${size.minDimension / 200});
            ctx.lineWidth = 0.8;
            
        """.trimIndent())
    }

    fun finish() {
        writer.write("""
            </script>
            </body>
            </html>
            
        """.trimIndent())
        writer.close()
    }

    override fun point(offset: Offset, color: Color) {
        writer.write("""
            ctx.beginPath();
            ctx.arc(${offset.x}, ${offset.y}, 1.8, 0, 2*Math.PI);
            ctx.fillStyle = "${color.svg}";
            ctx.fill();
            
        """.trimIndent())
    }

    override fun circle(center: Offset, radius: Float, style: Style) = style.styled {
        writer.write("ctx.arc(${center.x}, ${center.y}, $radius, 0, 2*Math.PI);\n")
    }

    override fun line(from: Offset, to: Offset, style: Style) = style.styled {
        writer.write("""
            ctx.moveTo(${from.x}, ${from.y});
            ctx.lineTo(${to.x}, ${to.y});
            
        """.trimIndent())
    }

    override fun polygon(points: List<Offset>, style: Style) = style.styled {
        writer.write("ctx.moveTo(${points[0].x}, ${points[0].y});\n")
        points.drop(1).forEach {
            writer.write("ctx.lineTo(${it.x}, ${it.y});\n")
        }
        writer.write("ctx.closePath();\n")
    }

    override fun text(pos: Offset, text: List<String>, color: Color) {
        writer.write("""
            ctx.beginPath();
            var x = ${pos.x + 3}
            
        """.trimIndent())
        text.forEachIndexed { index, str ->
            writer.write("""
                ctx.font = "${if (index % 2 == 0) 8 else 6}px roboto";
                ctx.fillText("$str", x, ${pos.y + if (index % 2 == 0) 3 else 6});
                x += ctx.measureText("$str").width;
                
            """.trimIndent())
        }
    }

    override fun angle(center: Offset, from: Float, to: Float, style: Style) = style.styled {
        writer.write("""
            ctx.arc(${center.x}, ${center.y}, 10, ${-from}, ${-to}, ${from < to});
            ctx.lineTo(${center.x}, ${center.y});
            ctx.closePath();
            
        """.trimIndent())
    }

    private fun Style.styled(action: () -> Unit) = if (fill || border != Border.No) {
        writer.write("ctx.beginPath();\n")
        action()
        if (fill) {
            writer.write("""
                ctx.fillStyle = ${color.html(0.3f)};
                ctx.fill();
                
            """.trimIndent())
        }
        if (border != Border.No) {
            border.effect?.toList().orEmpty().let {
                writer.write("ctx.setLineDash([${it.joinToString()}]);\n")
            }
            writer.write("""
                ctx.strokeStyle = "${color.svg}";
                ctx.stroke();
                
            """.trimIndent())
        } else Unit
    } else Unit
}

fun Color.html(alpha: Float) = "\"rgba(${red*255}, ${green*255}, ${blue*255}, $alpha)\""
