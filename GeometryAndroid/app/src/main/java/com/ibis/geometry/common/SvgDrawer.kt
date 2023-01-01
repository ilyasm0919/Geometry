package com.ibis.geometry.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import java.io.OutputStreamWriter
import kotlin.math.PI
import kotlin.math.abs

class SvgDrawer(transformation: TransformationState, size: Size, val writer: OutputStreamWriter): Drawer {
    private fun writeOpen(tag: String, vararg attributes: Pair<String, Any>) {
        writer.write("<$tag${attributes.joinToString("") { (name, value) ->
            " $name=\"$value\""
        }}")
    }

    override val bounds = transformation.getBounds(size)

    init {
        writeOpen(
            "svg",
            "width" to size.width,
            "height" to size.height,
            "xmlns" to "http://www.w3.org/2000/svg"
        )
        writer.write(">")
        writeOpen(
            "g",
            "transform" to
                    "scale(${transformation.zoom * size.minDimension / 200f}) " +
                    "rotate(${transformation.rotation}) " +
                    transformation.getTranslation(size).let { "translate(${it.x}, ${it.y})" },
            "stroke-width" to 0.8,
            "font-size" to 8,
            "font-family" to "roboto"
        )
        writer.write(">")
    }

    fun finish() {
        writer.write("</g></svg>")
        writer.close()
    }

    fun write(tag: String, vararg attributes: Pair<String, Any>, content: (() -> Unit)? = null) {
        writeOpen(tag, *attributes)
        content?.let {
            writer.write(">")
            it()
            writer.write("</$tag>")
        } ?: writer.write("/>")
    }

    override fun point(offset: Offset, color: Color) {
        write(
            "circle",
            "cx" to offset.x,
            "cy" to offset.y,
            "r" to 1.8,
            "fill" to color.svg
        )
    }

    override fun circle(center: Offset, radius: Float, style: Style) = style.styled {
        write(
            "circle",
            "cx" to center.x,
            "cy" to center.y,
            "r" to radius,
            *it
        )
    }

    override fun line(from: Offset, to: Offset, style: Style) = style.styled {
        write(
            "line",
            "x1" to from.x,
            "y1" to from.y,
            "x2" to to.x,
            "y2" to to.y,
            *it
        )
    }

    override fun polygon(points: List<Offset>, style: Style) = style.styled {
        write(
            "polygon",
            "points" to points.joinToString { point ->
                "${point.x} ${point.y}"
            },
            *it
        )
    }

    override fun text(pos: Offset, text: List<String>, color: Color) {
        write(
            "text",
            "x" to pos.x + 3f,
            "y" to pos.y + 3f,
            "fill" to color.svg
        ) {
            text.forEachIndexed { index, str ->
                if (index % 2 == 0) writer.write(str)
                else write("tspan", "baseline-shift" to "sub") {
                    writer.write(str)
                }
            }
        }
    }

    override fun angle(center: Offset, from: Float, to: Float, style: Style) = style.styled {
        val start = center + from.imagine().exp().toOffset() * (10f * style.scale)
        val end = center + to.imagine().exp().toOffset() * (10f * style.scale)
        write(
            "path",
            "d" to """
                M ${center.x} ${center.y}
                L ${start.x} ${start.y}
                A ${10 * style.scale} ${10 * style.scale} 0 ${if (abs(to - from) <= PI) 0 else 1} ${if (from <= to) 0 else 1} ${end.x} ${end.y}
                Z
            """.trimIndent(),
            *it
        )
    }
}

val Color.svg get() = "rgb(${red*255}, ${green*255}, ${blue*255})"

private fun Style.styled(action: (Array<Pair<String, Any>>) -> Unit) = if (fill || border != Border.No) {
    val filled: Array<Pair<String, Any>> =
        if (fill) arrayOf("fill" to color.svg, "fill-opacity" to 0.3)
        else arrayOf("fill" to "transparent")
    val stroked =
        if (border == Border.No) arrayOf()
        else arrayOf("stroke" to color.svg)
    val effect = border.effect?.let {
        arrayOf("stroke-dasharray" to it.joinToString(","))
    }.orEmpty()
    action(filled + stroked + effect)
} else Unit
