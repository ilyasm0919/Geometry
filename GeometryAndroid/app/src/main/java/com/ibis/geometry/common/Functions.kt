package com.ibis.geometry.common

import kotlin.math.PI
import kotlin.math.sqrt

interface ArgParser {
    val size: Int
    fun nextArg(): Geometric?
    operator fun<T> Arg<T>.invoke() = parser() ?: error("Expected $arg")
}

class Arg<out T>(val arg: String, val parser: ArgParser.() -> T?) {
    override fun toString() = arg
}

val geometric = Arg("Object") { nextArg() }

val real = Arg("Real") { nextArg() as? Complex }
val complex = Arg("Complex") { nextArg() as? Complex }
val point = Arg("Point") { nextArg() as? Complex }

val line = Arg("Line") {
    when (val arg = nextArg()) {
        is Line -> arg
        is Segment -> line(arg.from, arg.to)
        is Complex -> line(arg, point())
        else -> null
    }
}

val segment = Arg("Segment") {
    when (val arg = nextArg()) {
        is Segment -> arg
        is Complex -> Segment(arg, point())
        else -> null
    }
}

val triangle = Arg("Triangle") {
    when (val arg = nextArg()) {
        is Triangle -> arg
        is Complex -> Triangle(arg, point(), point())
        else -> null
    }
}

val polygon = Arg("Polygon") {
    when (val arg = nextArg()) {
        is AbstractPolygon -> arg
        is Complex -> Polygon(listOf(arg) + List(size - 1) { point() })
        else -> null
    }
}

val circle = Arg("Circle") {
    when (val arg = nextArg()) {
        is Circle -> arg
        is Complex -> circle(arg, point())
        else -> null
    }
}

val angle = Arg("Angle") {
    when (val arg = nextArg()) {
        is Angle -> arg
        is Complex -> point().let { Angle(it, (arg - it).arg(), (point() - it).arg()) }
        else -> null
    }
}

class Function(val name: String, private val args: List<String>, private val ret: String, val parser: (List<Reactive<Geometric>>) -> Reactive<Geometric>) {
    override fun toString() = "$name(${args.joinToString()}): $ret"
}

fun<T> parseArgs(args: List<Reactive<Geometric>>, parser: ArgParser.() -> T): Reactive<T> = args.traverse { input ->
    val arguments = object : ArgParser {
        override val size = input.size
        val iterator = input.iterator()
        override fun nextArg() = if (iterator.hasNext()) iterator.next() else null
    }
    val res = parser(arguments)
    check(!arguments.iterator.hasNext()) { "Unexpected arguments" }
    res
}

fun<T: Geometric> Arg<T>.mkFunction(name: String, vararg args: Any, parser: ArgParser.() -> T) = Function(name, args.map(Any::toString), arg) {
    parseArgs(it, parser)
}
fun<T: Geometric, T1> Arg<T>.function(
    name: String,
    arg: Arg<T1>,
    parser: (T1) -> T
) = mkFunction(name, arg) {
    parser(arg())
}
fun<T: Geometric, T1, T2> Arg<T>.function(
    name: String,
    arg1: Arg<T1>,
    arg2: Arg<T2>,
    parser: (T1, T2) -> T
) = mkFunction(name, arg1, arg2) {
    parser(arg1(), arg2())
}
fun<T: Geometric, T1, T2, T3> Arg<T>.function(
    name: String,
    arg1: Arg<T1>,
    arg2: Arg<T2>,
    arg3: Arg<T3>,
    parser: (T1, T2, T3) -> T
) = mkFunction(name, arg1, arg2, arg3) {
    parser(arg1(), arg2(), arg3())
}
fun<T: Geometric> mkTrinomial(name: String, vararg args: Any, ret: Arg<T>, parser: ArgParser.(TrinomialOption) -> T) = arrayOf(
    ret.mkFunction(name, point, *args) { parser(TrinomialOption.RootNot(point())) },
    ret.mkFunction(name + "1", *args) { parser(TrinomialOption.Root1) },
    ret.mkFunction(name + "2", *args) { parser(TrinomialOption.Root2) },
)
fun<T: Geometric, T1, T2> Arg<T>.trinomial(
    name: String,
    arg1: Arg<T1>,
    arg2: Arg<T2>,
    parser: (TrinomialOption, T1, T2) -> T
) = mkTrinomial(name, arg1, arg2, ret = this) {
    parser(it, arg1(), arg2())
}
fun<T: Geometric> Arg<T>.constructor(name: String) = function(name, this) { it }


fun line(a: Complex, b: Complex) =
    Line((a - b) * Complex.I, (a.conj() * b * Complex.I).re * 2)

fun intersect(l1: Line, l2: Line) =
    (l1.coef * l2.free - l1.free * l2.coef) /
            (l1.coef.conj() * l2.coef - l1.coef * l2.coef.conj())

fun divide(alpha: Complex, l: Segment) = l.from * (1 - alpha) + l.to * alpha

fun project(a: Complex, l: Line) = (a - (a.conj() * l.coef + l.free) / l.coef.conj()) / 2

fun centroid(p: AbstractPolygon) = p.points.reduce(Complex::plus) / p.points.size

fun circumcenter(t: Triangle) =
    ((t.b - t.c) * t.a.norm + (t.c - t.a) * t.b.norm + (t.a - t.b) * t.c.norm) /
            ((t.b - t.c) * t.a.conj() + (t.c - t.a) * t.b.conj() + (t.a - t.b) * t.c.conj())

fun circle(center: Complex, point: Complex) = Circle(center, (point - center).norm)

fun circumcircle(t: Triangle) = circle(circumcenter(t), t.a)

fun incenter(t: Triangle): Complex {
    val sa = (t.b - t.c).sqrt()
    val sb = (t.c - t.a).sqrt()
    val sc = (t.a - t.b).sqrt()
    return (t.a * sa * sc.conj() - t.c * sa.conj() * sc - sa * sc * (t.a - t.c).norm / sb.norm) / (sa * sc.conj() - sa.conj() * sc)
}

fun excenter(a: Complex, b: Complex, c: Complex): Complex {
    val sa = (b - c).sqrt()
    val sb = (c - a).sqrt()
    val sc = (a - b).sqrt()
    return (a * sa * sc.conj() - c * sa.conj() * sc + sa * sc * (a - c).norm / sb.norm) / (sa * sc.conj() - sa.conj() * sc)
}

fun midtriangle(t: Triangle) = Triangle((t.a + t.b) / 2, (t.b + t.c) / 2, (t.c + t.a) / 2)

fun normalize(a: Complex) = if (a == Complex.ZERO) Complex.ZERO else a / a.abs()

fun tangentPoint(option: TrinomialOption, a: Complex, c: Circle) = root(option,
    (a - c.center).conj(),
    -c.radiusSqr.real() * 2 - c.center * (a - c.center).conj() * 2,
    (a + c.center) * c.radiusSqr + c.center * c.center * (a - c.center).conj()
)


val functions = listOf(
    "Constructors" to listOf(
        point.constructor("point"),
        line.constructor("line"),
        segment.constructor("segment"),
        circle.constructor("circle"),
        triangle.constructor("triangle"),
        polygon.constructor("polygon"),
        angle.function("angle", angle) { it.normal() },
        angle.function("clockwise", angle) { it.clockwise() },
        angle.function("counterclockwise", angle) { it.counterclockwise() },
    ),
    "Intersections" to listOf(
        point.function("intersect", line, line, ::intersect),
        *point.trinomial("cintersect", line, circle) { option, l, c ->
            root(option,
                l.coef.conj(),
                l.free + c.center.conj() * l.coef - c.center * l.coef.conj(),
                c.radiusSqr * l.coef - c.center * l.free - c.center.norm * l.coef
            )
        },
        *point.trinomial("ccintersect", circle, circle) { option, c1, c2 ->
            (c2.center - c1.center).conj().let {
                root(option,
                    it,
                    (c2.radiusSqr - c1.radiusSqr).real() - (c2.center + c1.center) * it,
                    c1.center * c2.center * it - c1.center * c2.radiusSqr + c2.center * c1.radiusSqr
                )
            }
        },
    ),
    "Triangle's centers" to listOf(
        point.function("centroid", polygon, ::centroid),
        point.function("circumcenter", triangle, ::circumcenter),
        point.function("orthocenter", triangle) { t ->
            ((t.b - t.a) * t.c.norm + (t.c - t.b) * t.a.norm + (t.a - t.c) * t.b.norm +
                    (t.a * t.a - t.b * t.b) * t.c.conj() + (t.b * t.b - t.c * t.c) * t.a.conj() + (t.c * t.c - t.a * t.a) * t.b.conj()) /
                    ((t.b - t.c) * t.a.conj() + (t.c - t.a) * t.b.conj() + (t.a - t.b) * t.c.conj())
        },
        point.function("incenter", triangle, ::incenter),
        point.function("euler_center", triangle) { t ->
            circumcenter(midtriangle(t))
        },
        point.function("gergonne", triangle) { t ->
            val la = (t.b - t.c).abs()
            val lb = (t.c - t.a).abs()
            val lc = (t.a - t.b).abs()
            val pa = lb + lc - la
            val pb = lc + la - lb
            val pc = la + lb - lc
            intersect(
                line(t.a, divide(pb.real() / pc, Segment(t.b, t.c))),
                line(t.b, divide(pa.real() / pc, Segment(t.a, t.c))),
            )
        },
        point.function("nagel", triangle) { t ->
            val la = (t.b - t.c).abs()
            val lb = (t.c - t.a).abs()
            val lc = (t.a - t.b).abs()
            val pa = lb + lc - la
            val pb = lc + la - lb
            val pc = la + lb - lc
            intersect(
                line(t.a, divide(pb.real() / pc, Segment(t.c, t.b))),
                line(t.b, divide(pa.real() / pc, Segment(t.c, t.a))),
            )
        },
    ),
    "Geometry of a triangle" to listOf(
        circle.function("circumcircle", triangle, ::circumcircle),
        circle.function("incircle", triangle) { t ->
            incenter(t).let {
                circle(it, project(it, line(t.a, t.c)))
            }
        },
        point.function("excenter", point, point, point, ::excenter),
        circle.function("excircle", point, point, point) { a, b, c ->
            excenter(a, b, c).let {
                circle(it, project(it, line(a, c)))
            }
        },
        line.function("euler_line", triangle) { t ->
            line(centroid(t), circumcenter(t))
        },
        triangle.function("midtriangle", triangle, ::midtriangle),
        circle.function("euler_circle", triangle) { t ->
            circumcircle(midtriangle(t))
        },
        point.function("isogonal", point, triangle) { p, t ->
            circumcenter(Triangle(
                p.symmetry(line(t.a, t.b)),
                p.symmetry(line(t.b, t.c)),
                p.symmetry(line(t.c, t.a)),
            ))
        },
    ),
    "Lines" to listOf(
        line.function("midline", segment) { l ->
            Line(l.from - l.to, l.to.norm - l.from.norm)
        },
        point.function("midpoint", segment) { l ->
            (l.from + l.to) / 2
        },
        point.function("divide", complex, segment, ::divide),
        line.function("parallel", point, line) { a, l ->
            Line(l.coef, -(a.conj() * l.coef).re * 2)
        },
        line.function("perpendicular", point, line) { a, l ->
            Line(l.coef * Complex.I, -(a.conj() * l.coef * Complex.I).re * 2)
        },
        point.function("project", point, line, ::project),
        line.function("bisector", angle) { a ->
            (a.to + a.from + PI).times(0.5.imagine()).exp().let {
                Line(it, -(a.center * it.conj()).re*2)
            }
        },
        line.function("exbisector", angle) { a ->
            (a.to + a.from).times(0.5.imagine()).exp().let {
                Line(it, -(a.center * it.conj()).re*2)
            }
        },
    ),
    "Circles" to listOf(
        real.function("radius", circle) { sqrt(it.radiusSqr).real() },
        point.function("center", circle, Circle::center),
        circle.function("diameter_circle", segment) { l ->
            circle((l.from + l.to) / 2, l.from)
        },
        *line.trinomial("tangent", point, circle) { option, a, c ->
            (tangentPoint(option, a, c) - c.center).let {
                Line(it, -(it*c.center.conj()).re * 2 - c.radiusSqr * 2)
            }
        },
        *point.trinomial("tangentPoint", point, circle, ::tangentPoint),
        line.function("radical_axis", circle, circle) { c1, c2 ->
            Line(c1.center - c2.center, c1.radiusSqr - c2.radiusSqr - c1.center.norm + c2.center.norm)
        },
        point.function("radical_center", circle, circle, circle) { c1, c2, c3 ->
            ((c1.radiusSqr - c1.center.norm) * (c2.center - c3.center) +
                    (c2.radiusSqr - c2.center.norm) * (c3.center - c1.center) +
                    (c3.radiusSqr - c3.center.norm) * (c1.center - c2.center)) /
            (c1.center * c2.center.conj() - c1.center.conj() * c2.center +
                    c2.center * c3.center.conj() - c2.center.conj() * c3.center +
                    c3.center * c1.center.conj() - c3.center.conj() * c1.center)
        },
        line.function("polar", point, circle) { a, c ->
            (a - c.center).let {
                Line(it, -2 * (c.center * it.conj()).re - 2 * c.radiusSqr)
            }
        },
        point.function("pole", line, circle) { l, c ->
            -l.coef * c.radiusSqr / (l.free / 2 + (c.center * l.coef.conj()).re) + c.center
        },
        point.function("cproject", point, circle) { a, c ->
            (a - c.center).let {
                it * sqrt(c.radiusSqr / it.norm) + c.center
            }
        },
    ),
    "Algebra" to listOf(
        real.function("re", complex) { it.re.real() },
        real.function("im", complex) { it.im.real() },
        complex.function("sqr", complex) { it*it },
        complex.function("sqrt", complex, Complex::sqrt),
        complex.function("exp", complex, Complex::exp),
        complex.function("ln", complex, Complex::ln),
        real.function("abs", complex) { it.abs().real() },
        real.function("length", segment) { l ->
            (l.to - l.from).abs().real()
        },
        complex.function("normalize", complex, ::normalize),
        complex.function("dir", segment) { l ->
            normalize(l.to - l.from)
        },
    ),
    "Transformations" to listOf(
        geometric.function("symmetry", geometric, line, Geometric::symmetry),
        geometric.function("translation", geometric, point, Geometric::translation),
        geometric.function("homothety", geometric, point, complex, Geometric::homothety),
        geometric.function("inversion", geometric, circle, Geometric::inversion),
    ),
    "Special" to listOf(
        Function("time", listOf(), "Natural") {
            check(it.isEmpty()) { "Unexpected arguments" }
            Dynamic(timeUsed = true, argsUsed = false) { time.real() }
        },
        point.function("choose", geometric, real) { g, t -> g.choose(t.abs()) },
        Function("line_trace", listOf(point.arg), line.arg) {
            @Suppress("ComplexRedundantLet")
            it.single().fixTime {
                line(
                    it(3658) as? Complex ?: error("Expected point"),
                    it(9137) as? Complex ?: error("Expected point")
                )
            }
        },
        Function("circle_trace", listOf(point.arg), circle.arg) {
            @Suppress("ComplexRedundantLet")
            it.single().fixTime {
                circumcircle(
                    Triangle(
                        it(2709) as? Complex ?: error("Expected point"),
                        it(5073) as? Complex ?: error("Expected point"),
                        it(6349) as? Complex ?: error("Expected point")
                    )
                )
            }
        },
        point.function("assert", point, point) { x, y ->
            check((x - y).norm < x.norm * 0.0001f) { "Assertion failed: $x != $y" }
            x
        }
    ),
)
