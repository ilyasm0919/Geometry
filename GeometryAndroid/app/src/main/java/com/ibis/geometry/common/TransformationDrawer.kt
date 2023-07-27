package com.ibis.geometry.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.PI

class TransformationDrawer<T : Drawer>(val drawer: T, private val transformation: TransformationState, val size: Size) : Drawer {
    override val zoom = drawer.zoom * transformation.zoom

    override val bounds = transformation.getBounds(size)

    private fun get(point: Offset) = transformation.normalToScreen(point, size)

    override fun point(offset: Offset, color: Color) = drawer.point(get(offset), color)

    override fun circle(center: Offset, radius: Float, style: Style) = drawer.circle(get(center), radius * size.minDimension * zoom / 200f, style)

    override fun line(from: Offset, to: Offset, style: Style) = drawer.line(get(from), get(to), style)

    override fun polygon(points: List<Offset>, style: Style) = drawer.polygon(points.map(::get), style)

    override fun text(pos: Offset, text: List<String>, color: Color) = drawer.text(get(pos), text, color)

    override fun angle(center: Offset, from: Float, to: Float, style: Style) = drawer.angle(get(center), from - transformation.rotation * PI.toFloat() / 180, to - transformation.rotation * PI.toFloat() / 180, style)
}