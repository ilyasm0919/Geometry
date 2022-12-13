package com.ibis.geometry.common

data class Polygon(override val points: List<Complex>): AbstractPolygon() {
    override fun rebuild(f: (Complex) -> Complex) = Polygon(points.map(f))
}
