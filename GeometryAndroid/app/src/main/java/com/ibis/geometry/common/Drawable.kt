package com.ibis.geometry.common

import androidx.compose.ui.graphics.Color

class Drawable(val draw: Drawer.(Style) -> Unit) {
    val style = Style(null, Color.Black, Border.Line, null, fill = false)
    fun Drawer.draw() = draw(style)
}

fun Drawer.draw(drawable: Drawable) = drawable.run { draw() }
