package com.ibis.geometry.common

import kotlin.math.PI
import kotlin.math.abs

// from - 2*pi < to < from + 2*pi
data class Angle(val center: Complex, val from: Float, val to: Float): Geometric() {
    override fun symmetry(l: Line) = (l.coef.arg()*2-PI.toFloat()).let {
        Angle(center.symmetry(l), it-from, it-to)
    }
    override fun translation(a: Complex) = Angle(center.translation(a), from, to)
    override fun homothety(a: Complex, k: Complex) = k.arg().let {
        Angle(center.homothety(a, k), from + it, to + it)
    }
    override fun inversion(c: Circle) = error("Inversion of angle")
    override fun choose(time: Float) = error("Choose from angle")

    override fun toDrawable() = Drawable {
        if (abs(abs(to - from) - PI/2) < 0.001f) {
            val f = from.imagine().exp()*10*it.scale
            val t = to.imagine().exp()*10*it.scale
            polygon(listOf(
                center,
                center + f,
                center + f + t,
                center + t,
            ).map(Complex::toOffset), it)
        } else angle(center.toOffset(), from, to, it)
    }

    fun normal() = when {
        from < to - PI -> Angle(center, from, to - 2*PI.toFloat())
        from > to + PI -> Angle(center, from, to + 2*PI.toFloat())
        else -> this
    }
    fun clockwise() = if (to <= from) this else Angle(center, from, to - 2*PI.toFloat())
    fun counterclockwise() = if (to >= from) this else Angle(center, from, to + 2*PI.toFloat())
}
