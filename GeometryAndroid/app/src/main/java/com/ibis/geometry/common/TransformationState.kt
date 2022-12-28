package com.ibis.geometry.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class TransformationState {
    var pan: Offset = Offset.Zero
        private set
    var zoom: Float = 1f
        private set
    var rotation: Float = 0f
        private set
    private var consumed: MutableState<Int> = mutableStateOf(0)
    val index get() = consumed.value
    fun apply(centroid: Offset, panChange: Offset, zoomChange: Float, rotationChange: Float) {
        pan += untransform(centroid).let {
            it.rotate(-rotationChange) / zoomChange - it + untransform(panChange)
        }
        zoom *= zoomChange
        rotation += rotationChange
        consumed.value++
    }
    fun move(offset: Offset) {
        pan -= untransform(offset)
        consumed.value++
    }
    fun rotate(degrees: Float) {
        rotation += degrees
        consumed.value++
    }
    fun scale(scale: Float) {
        zoom *= scale
        consumed.value++
    }

    fun untransform(point: Offset) = point.rotate(-rotation) / zoom

    fun getBounds(size: Size): Rect {
        val points = size.toRect().let {
            listOf(it.topLeft, it.topRight, it.bottomRight, it.bottomLeft)
        }.map { transform(it, size) }
        return Rect(
            points.minOf(Offset::x),
            points.minOf(Offset::y),
            points.maxOf(Offset::x),
            points.maxOf(Offset::y)
        )
    }

    fun transform(point: Offset, size: Size) =
        (untransform(point - size.center) - pan) * 200f / size.minDimension

    fun getTranslation(size: Size) =
        (pan + untransform(size.center)) * 200f / size.minDimension
}

fun Offset.rotate(degrees: Float) = (degrees * PI.toFloat() / 180).let {
    val sin = sin(it)
    val cos = cos(it)
    Offset(x * cos - y * sin, x * sin + y * cos)
}
