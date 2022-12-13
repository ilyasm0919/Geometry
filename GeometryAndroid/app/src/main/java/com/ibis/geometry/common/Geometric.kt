package com.ibis.geometry.common

sealed class Geometric {
    abstract fun symmetry(l: Line): Geometric
    abstract fun translation(a: Complex): Geometric
    abstract fun homothety(a: Complex, k: Complex): Geometric
    abstract fun inversion(c: Circle): Geometric
//    abstract fun choose(rand: Random): Geometric
    abstract fun toDrawable(): Drawable
}
