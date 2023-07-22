package com.ibis.geometry.common

import androidx.compose.ui.graphics.Color

class Drawable(private val asPoint: Complex? = null, val draw: Drawer.(Style) -> Unit) {
    val style = Style(null, Color.Black, Border.Line, null, fill = false, scale = 1f, bounded = false)
    fun Drawer.draw() = draw(style)
    val topmost get() = asPoint?.let {
        if (style.border != Border.No) visiblePoints.add(it)
        true
    } ?: false
}

fun Drawer.draw(drawable: Drawable) = drawable.run { draw() }
