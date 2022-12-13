package com.ibis.geometry.common

data class Triangle(val a: Complex, val b: Complex, val c: Complex): AbstractPolygon() {
    override val points = listOf(a, b, c)
    override fun rebuild(f: (Complex) -> Complex) = Triangle(f(a), f(b), f(c))

//    override fun choose(rand: Random) = (rand.nextFloat() to rand.nextFloat()).let { (x, y) ->
//        if (x + y > 1) 1 - x to 1 - y else x to y
//    }.let { (x, y) -> a + (b - a) * x + (c - a) * y }
}
