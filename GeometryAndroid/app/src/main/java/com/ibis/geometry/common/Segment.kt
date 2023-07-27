package com.ibis.geometry.common

import java.lang.Math.PI
import kotlin.math.sin

data class Segment(val from: Complex, val to: Complex): Geometric() {
    override fun symmetry(l: Line) = Segment(from.symmetry(l), to.symmetry(l))

    override fun translation(a: Complex) = Segment(from.translation(a), to.translation(a))

    override fun homothety(a: Complex, k: Complex) = Segment(from.homothety(a, k), to.homothety(a, k))

    override fun inversion(c: Circle) = line(from, to).inversion(c)

    override fun choose(time: Float) = divide(sin(time * 2 * PI).real() / 2 + 0.5f, this)

    override fun toDrawable() = Drawable {
        line(from.toOffset(), to.toOffset(), it)
        val center = (from + to).toOffset() / 2f
        val normed = normalize(to - from)
        val delta = (normed * 3.imagine()).toOffset() / zoom
        val step = normed.toOffset() / zoom
        when (it.equalityGroup) {
            EqualityGroup.Equal1 -> {
                line(center - delta, center + delta, it)
            }
            EqualityGroup.Equal2 -> {
                line(center - step - delta, center - step + delta, it)
                line(center + step - delta, center + step + delta, it)
            }
            EqualityGroup.Equal3 -> {
                line(center - step * 2f - delta, center - step * 2f + delta, it)
                line(center - delta, center + delta, it)
                line(center + step * 2f - delta, center + step * 2f + delta, it)
            }
            EqualityGroup.EqualV -> {
                line(center - step * 2f + delta, center - delta, it)
                line(center + step * 2f + delta, center - delta, it)
            }
            EqualityGroup.EqualO -> circle(center, 2.5f/zoom, it)
            null -> {}
        }
    }
}
