package com.ibis.geometry.common

import androidx.compose.ui.graphics.Color

enum class Border(val effect: FloatArray?) {
    No(null),
    Dot(floatArrayOf(1f, 1f)),
    Dash(floatArrayOf(5f, 5f)),
    DashDot(floatArrayOf(5f, 2f, 1f, 2f)),
    Line(null);
}

fun spans(text: String) = buildList {
    var last = 0
    while (true) {
        var next = text.indexOf('_', last)
        if (next == text.length - 1 || next == -1) next = text.length
        add(text.substring(last, next))
        if (next == text.length) break
        if (text[next + 1] == '{') {
            last = text.indexOf('}', next + 1)
            if (last == 0) error("Expected '}'")
            add(text.substring(next + 2, last))
            last++
        } else {
            add(text[next + 1].toString())
            last = next + 2
        }
    }
}

data class Style(var name: List<String>?, var color: Color, var border: Border, var fill: Boolean)
