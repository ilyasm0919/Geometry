package com.ibis.geometry.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class TransformationState {
    private var pan: Offset = Offset.Zero
    var zoom: Float = 1f
    var rotation: Float = 0f
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

    private fun untransform(point: Offset) = point.rotate(-rotation) / zoom

    fun getBounds(size: Size): Rect {
        val points = size.toRect().let {
            listOf(it.topLeft, it.topRight, it.bottomRight, it.bottomLeft)
        }.map { screenToNormal(it, size) }
        return Rect(
            points.minOf(Offset::x),
            points.minOf(Offset::y),
            points.maxOf(Offset::x),
            points.maxOf(Offset::y)
        )
    }

    fun screenToNormal(point: Offset, size: Size) =
        (untransform(point - size.center) - pan) * 200f / size.minDimension

    fun normalToScreen(point: Offset, size: Size) =
        (point * size.minDimension / 200f + pan).rotate(rotation) * zoom + size.center

    fun reset() {
        pan = Offset.Zero
        zoom = 1f
        rotation = 0f
        consumed.value++
    }
}

fun Offset.rotate(degrees: Float) = (degrees * PI.toFloat() / 180).let {
    val sin = sin(it)
    val cos = cos(it)
    Offset(x * cos - y * sin, x * sin + y * cos)
}
