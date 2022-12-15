package com.ibis.geometry.common

import kotlin.math.PI
import kotlin.math.sqrt

data class Circle(val center: Complex, val radiusSqr: Float): Geometric() {
    override fun symmetry(l: Line) = Circle(center.symmetry(l), radiusSqr)

    override fun translation(a: Complex) = Circle(center.translation(a), radiusSqr)

    override fun homothety(a: Complex, k: Complex) =
        Circle(center.homothety(a, k), radiusSqr * k.norm)

    // (r' / (~z - ~o') + o' - o) * (r' / (z - o') + ~o' - ~o) = r
    // (z - o') * (~z - ~o') * (norm(o' - o) - r) + (z - o') * r' * (~o' - ~o) + (~z - ~o') * r' * (o' - o) + r'^2 = 0
    // x = norm(o' - o) - r
    // IF x != 0 THEN
    // (z - o') * (~z - ~o') + (z - o') * r' * (~o' - ~o) / x + (~z - ~o') * r' * (o' - o) / x + r'^2 / x = 0
    // z * ~z - z * (r' * (~o' - ~o) / x - ~o') - ~z * (r' * (o' - o) / x - o') + (o' * ~o' - o' * r' * (~o' - ~o) / x - ~o' * r' * (o' - o) / x) + r'^2 / x = 0
    // norm(z - (o' - r' * (o' - o) / x)) = norm(r' * (o' - o) / x) + r'^2 / x
    // ELSE
    // (z - o') * (~o' - ~o) + (~z - ~o') * (o' - o) + r' = 0
    // z * (~o' - ~o) + ~z * (o' - o) + (r' + o * ~o' + ~o * o' - norm(o') * 2) = 0
    override fun inversion(c: Circle) = ((c.center - center).norm - radiusSqr).takeIf {
        it != 0f
    }?.let {
        val x = c.radiusSqr * (c.center - center) / it
        Circle(c.center - x, x.norm - c.radiusSqr * c.radiusSqr / it)
    } ?: Line(c.center - center,
        c.radiusSqr + (c.center * center.conj()).re * 2 - c.center.norm * 2)

    override fun choose(time: Float) =
        center + (time * 2 * PI).imagine().exp() * sqrt(radiusSqr)

    override fun toDrawable() = Drawable {
        circle(center.toOffset(), sqrt(radiusSqr), it)
    }
}
