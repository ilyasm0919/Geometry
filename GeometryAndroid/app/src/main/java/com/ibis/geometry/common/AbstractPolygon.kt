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

//    override fun choose(rand: Random): Geometric { // Only for convex polygons
//        var res = area() * rand.nextFloat()
//        var i = 0
//        while (res > 0 && i < points.size - 1) {
//            i++
//            res -= Triangle(points[0], points[i], points[i + 1]).area()
//        }
//        return Triangle(points[0], points[i], points[i + 1]).choose(rand)
//    }
//    fun area() = { x: Complex, y: Complex -> x.re * y.im - x.im * y.re }.let {
//        abs(points.zipWithNext(it).sum() + it(points.last(), points.first()))
//    }
}
