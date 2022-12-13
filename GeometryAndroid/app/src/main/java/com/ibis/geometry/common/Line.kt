package com.ibis.geometry.common

import androidx.compose.ui.geometry.isUnspecified

// ~a * z + a * ~z + b = 0
data class Line(val coef: Complex, val free: Float): Geometric() {
    // -(~z * a' + b') / ~a' * ~a - (z * ~a' + b') / a' * a + b = 0
    override fun symmetry(l: Line) =
        Line(coef.conj() * l.coef / l.coef.conj(), (l.free * coef / l.coef).re * 2 - free)

    // (z - p) * ~a + (~z - ~p) * a + b = 0
    override fun translation(a: Complex) = Line(coef, free - (coef * a.conj()).re * 2)

    // ((z - p) / k + p) * ~a + ((~z - ~p) / ~k + ~p) * a + b = 0
    override fun homothety(a: Complex, k: Complex) =
        Line(coef / k.conj(), (a * coef.conj() * (1 - 1/k)).re * 2 + free)

    // (r / (~z - ~o) + o) * ~a + (r / (z - o) + ~o) * a + b = 0
    // (z - o) * (~z - ~o) * (o * ~a + ~o * a + b) + (z - o) * ~a * r + (~z - ~o) * a * r = 0
    // x = o * ~a + ~o * a + b
    // z * ~z + z * (~a * r / x - ~o) + ~z * (a * r / x - o) + (o * ~o - o * ~a * r / x - ~o * a * r / x) = 0
    // norm(z - (o - a * r / x)) = norm(a * r / x)
    override fun inversion(c: Circle) = (c.center * coef.conj() + c.center.conj() * coef + free).takeIf {
        it != Complex.ZERO
    }?.let { coef * c.radiusSqr / it }?.let { Circle(c.center - it, it.norm) }
        ?: this

//    override fun choose(rand: Random) = error("Choose from line")

    override fun toDrawable() = Drawable { style ->
        val epsilon = 0.001f
        val points = listOf(
            Line(Complex.ONE, size.width * 2),
            Line(Complex.I, size.height * 2),
            Line(-Complex.ONE, size.width * 2),
            Line(-Complex.I, size.height * 2),
        ).map { intersect(this@Line, it).toOffset() }.filter {
            !it.isUnspecified &&
                    it.x in -size.width-epsilon..size.width+epsilon &&
                    it.y in -size.height-epsilon..size.height+epsilon
        }
        assert(points.size == 2) { "Draw line ($this@Line) intersections: ${points.size}" }
        line(points[0], points[1], style)
    }
}
