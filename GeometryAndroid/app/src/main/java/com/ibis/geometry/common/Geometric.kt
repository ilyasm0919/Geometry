package com.ibis.geometry.common

sealed class Geometric {
    abstract fun symmetry(l: Line): Geometric
    abstract fun translation(a: Complex): Geometric
    abstract fun homothety(a: Complex, k: Complex): Geometric
    abstract fun inversion(c: Circle): Geometric
    abstract fun choose(time: Float): Complex
    abstract fun toDrawable(): Drawable
}
