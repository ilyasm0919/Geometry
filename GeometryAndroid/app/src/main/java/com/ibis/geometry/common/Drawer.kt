package com.ibis.geometry.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

interface Drawer {
    val bounds: Rect
    fun point(offset: Offset, color: Color)
    fun circle(center: Offset, radius: Float, style: Style)
    fun line(from: Offset, to: Offset, style: Style)
    fun polygon(points: List<Offset>, style: Style)
    fun text(pos: Offset, text: List<String>, color: Color)
    fun angle(center: Offset, from: Float, to: Float, style: Style)
}
