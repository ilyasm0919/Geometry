package com.ibis.geometry.common

interface ArgParser {
    val size: Int
    fun nextArg(): Geometric?
}

inline fun<T> parseArgs(source: Collection<Geometric>, parser: ArgParser.() -> T): T {
    val args = object : ArgParser {
        val iterator = source.iterator()
        override val size = source.size
        override fun nextArg() = if (iterator.hasNext()) iterator.next() else null
    }
    val res = parser(args)
    check(!args.iterator.hasNext()) { "Unexpected arguments" }
    return res
}

fun ArgParser.geometric() = nextArg() ?: error("Expected object")

fun ArgParser.point() = nextArg() as? Complex ?: error("Expected point")

fun ArgParser.line() = when (val arg = nextArg()) {
    is Line -> arg
    is Segment -> line(arg.from, arg.to)
    is Complex -> line(arg, point())
    else -> error("Expected line")
}

fun ArgParser.segment() = when (val arg = nextArg()) {
    is Segment -> arg
    is Complex -> Segment(arg, point())
    else -> error("Expected segment")
}

fun ArgParser.triangle() = when (val arg = nextArg()) {
    is Triangle -> arg
    is Complex -> Triangle(arg, point(), point())
    else -> error("Expected triangle")
}

fun ArgParser.polygon() = when (val arg = nextArg()) {
    is AbstractPolygon -> arg
    is Complex -> Polygon(listOf(arg) + List(size-1) { point() })
    else -> error("Expected polygon")
}

fun ArgParser.circle() = when (val arg = nextArg()) {
    is Circle -> arg
    is Complex -> circle(arg, point())
    else -> error("Expected circle")
}
