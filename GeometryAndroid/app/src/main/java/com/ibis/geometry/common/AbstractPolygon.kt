package com.ibis.geometry.common

sealed class AbstractPolygon: Geometric() {
    abstract val points: List<Complex>
    abstract fun rebuild(f: (Complex) -> Complex): AbstractPolygon

    override fun symmetry(l: Line) = rebuild { it.symmetry(l) }
    override fun translation(a: Complex) = rebuild { it.translation(a) }
    override fun homothety(a: Complex, k: Complex) = rebuild { it.homothety(a, k) }
    override fun inversion(c: Circle) = rebuild { it.inversion(c) }
    override fun toDrawable() = Drawable {
        polygon(points.map(Complex::toOffset), it)
    }

    override fun choose(time: Float) = error("Choose from polygon")
}
