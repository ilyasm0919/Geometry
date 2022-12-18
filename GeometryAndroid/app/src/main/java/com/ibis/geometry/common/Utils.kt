package com.ibis.geometry.common

import kotlin.math.PI
import kotlin.math.sqrt

// (z - a) * (~a - ~b) = (~z - ~a) * (a - b)
// z * (~a - ~b) + ~z * (b - a) + (a * ~b - b * ~a) = 0
// z * (~a - ~b) * (-i) + ~z * (a - b) * i + (~a * b * i + a * ~b * (-i)) = 0
fun line(a: Complex, b: Complex) =
    Line((a - b) * Complex.I, (a.conj() * b * Complex.I).re * 2)

fun angle(a: Complex, b: Complex, c: Complex) = Angle(b, (a - b).arg(), (c - b).arg())

// z * ~a + ~z * a + b = 0
// z * ~c + ~z * c + d = 0
// z * (~a * c - a * ~c) = a * d - b * c
fun intersect(l1: Line, l2: Line) =
    (l1.coef * l2.free - l1.free * l2.coef) /
            (l1.coef.conj() * l2.coef - l1.coef * l2.coef.conj())

// (z - o) * (~z - ~o) = r
// z * ~a + ~z * a + b = 0
// (z - o) * ((-b - z * ~a) / a - ~o) = r
// (z - o) * (z * ~a + b + ~o * a) + r * a = 0
// z^2 * ~a + z * (b + ~o * a - o * ~a) + (r * a - o * b - norm(o) * a) = 0
fun cintersect(option: TrinomialOption, l: Line, c: Circle) = root(option,
    l.coef.conj(),
    l.free + c.center.conj() * l.coef - c.center * l.coef.conj(),
    c.radiusSqr * l.coef - c.center * l.free - c.center.norm * l.coef
)

// (z - o1) * (~z - ~o1) = r1
// (z - o2) * (~z - ~o2) = r2
// (z - o1) * (r2 / (z - o2) + ~o2 - ~o1) = r1
// (z - o1) * (z - o2) * ~(o2 - o1) - r1 * (z - o2) + r2 * (z - o1) = 0
// z^2 * ~(o2 - o1) + z * (r2 - r1 - (o2 + o1) * ~(o2 - o1)) + (o1 * o2 * ~(o2 - o1) - o1 * r2 + o2 * r1) = 0
fun ccintersect(option: TrinomialOption, c1: Circle, c2: Circle) = (c2.center - c1.center).conj().let {
    root(option,
        it,
        (c2.radiusSqr - c1.radiusSqr).real() - (c2.center + c1.center) * it,
        c1.center * c2.center * it - c1.center * c2.radiusSqr + c2.center * c1.radiusSqr
    )
}

fun midpoint(l: Segment) = (l.from + l.to) / 2

fun divide(alpha: Complex, l: Segment) = l.from * (1 - alpha) + l.to * alpha

// (z - a) * (~z - ~a) = (z - b) * (~z - ~b)
// z * (~b - ~a) + ~z * (b - a) + (norm(a) - norm(b)) = 0
fun midline(l: Segment) =
    Line(l.from - l.to, l.to.norm - l.from.norm)

// (z - p) * ~a + (~z - ~p) * a = 0
fun parallel(a: Complex, l: Line) = Line(l.coef, -(a.conj() * l.coef).re * 2)

// (z - p) * ~a - (~z - ~p) * a = 0
// (z - p) * ~a * (-i) + (~z - ~p) * a * i = 0
fun perpendicular(a: Complex, l: Line) =
    Line(l.coef * Complex.I, -(a.conj() * l.coef * Complex.I).re * 2)

// (z - p) * ~a - (~z - ~p) * a = 0
// z * ~a + ~z * a + b = 0
// 2 * z * ~a - p * ~a + ~p * a + b = 0
fun project(a: Complex, l: Line) = (a - (a.conj() * l.coef + l.free) / l.coef.conj()) / 2

fun cproject(a: Complex, c: Circle) = (a - c.center).let {
    it * sqrt(c.radiusSqr / it.norm) + c.center
}

fun bisector(a: Angle) = (a.to + a.from + PI).times(0.5.imagine()).exp().let {
    Line(it, -(a.center * it.conj()).re*2)
}

fun exbisector(a: Angle) = (a.to + a.from).times(0.5.imagine()).exp().let {
    Line(it, -(a.center * it.conj()).re*2)
}

fun centroid(p: AbstractPolygon) = p.points.reduce(Complex::plus) / p.points.size

// z * (~b - ~a) + ~z * (b - a) + (norm(a) - norm(b)) = 0
// z * (~c - ~a) + ~z * (c - a) + (norm(a) - norm(c)) = 0
// z * (~b - ~a) * (c - a) + ~z * (b - a) * (c - a) + (norm(a) - norm(b)) * (c - a) = 0
// z * (~c - ~a) * (b - a) + ~z * (b - a) * (c - a) + (norm(a) - norm(c)) * (b - a) = 0
// z * ((~b - ~a) * (c - a) - (~c - ~a) * (b - a)) =
//     = (norm(a) - norm(c)) * (b - a) - (norm(a) - norm(b)) * (c - a)
// z * (~b * c - a * ~b - ~a * c - b * ~c + ~a * b + a * ~c) =
//     = a * norm(c) - b * norm(c) + b * norm(a) - ...
fun circumcenter(t: Triangle) =
    ((t.b - t.c) * t.a.norm + (t.c - t.a) * t.b.norm + (t.a - t.b) * t.c.norm) /
            ((t.b - t.c) * t.a.conj() + (t.c - t.a) * t.b.conj() + (t.a - t.b) * t.c.conj())

// (z - b) * (~a - ~c) + (~z - ~b) * (a - c) = 0
// (z - a) * (~b - ~c) + (~z - ~a) * (b - c) = 0
// z * (~a - ~c) * (b - c) + ~z * (a - c) * (b - c) = (b * ~c + ~b * c - a * ~b - ~a * b) * (c - b) =
//     = b * norm(c) + ~b * c * c - a * ~b * c - ~a * b * c - b * b * ~c - norm(b) * c + a * norm(b) + ~a * b * b =
// z * (~a * b - ~a * c - b * ~c - ...) =
//     = (b - a) * norm(c) + (~b - ~a) * c * c + (a * a - b * b) * ~c + (norm(a) - norm(b)) * c + a * norm(b) - b * norm(a) + ~a * b * b - a * a * ~b
fun orthocenter(t: Triangle) =
    ((t.b - t.a) * t.c.norm + (t.c - t.b) * t.a.norm + (t.a - t.c) * t.b.norm +
            (t.a * t.a - t.b * t.b) * t.c.conj() + (t.b * t.b - t.c * t.c) * t.a.conj() + (t.c * t.c - t.a * t.a) * t.b.conj()) /
            ((t.b - t.c) * t.a.conj() + (t.c - t.a) * t.b.conj() + (t.a - t.b) * t.c.conj())

// (z - a) * ~sqrt(b - a) * ~sqrt(c - a) = (~z - ~a) * sqrt(b - a) * sqrt(c - a)
// (z - c) * ~sqrt(b - c) * ~sqrt(a - c) = (~z - ~c) * sqrt(b - c) * sqrt(a - c)
// (z - a) * ~sc * ~sb = (-~z + ~a) * sc * sb
// (z - c) * ~sa * ~sb = (-~z + ~c) * sa * sb
// (z - a) * sa * ~sc = (-~z + ~a) * sa * (c - a) * sc / norm(sb)
// (z - c) * ~sa * sc = (-~z + ~c) * sa * (c - a) * sc / norm(sb)
// z * (sa * ~sc - ~sa * sc) = (~a - ~c) * sa * (c - a) * sc / norm(sb) + a * sa * ~sc - c * ~sa * sc =
//     = a * sa * ~sc - c * ~sa * sc - norm(c - a) * sa * sc / norm(sb)
fun incenter(t: Triangle): Complex {
    val sa = (t.b - t.c).sqrt()
    val sb = (t.c - t.a).sqrt()
    val sc = (t.a - t.b).sqrt()
    return (t.a * sa * sc.conj() - t.c * sa.conj() * sc - sa * sc * (t.a - t.c).norm / sb.norm) / (sa * sc.conj() - sa.conj() * sc)
}

fun incircle(t: Triangle) = incenter(t).let {
    circle(it, project(it, line(t.a, t.c)))
}

// (z - a) * ~sqrt(b - a) * ~sqrt(c - a) = (-~z + ~a) * sqrt(b - a) * sqrt(c - a)
// (z - c) * ~sqrt(b - c) * ~sqrt(a - c) = (-~z + ~c) * sqrt(b - c) * sqrt(a - c)
// (z - a) * ~sc * ~sb = (~z - ~a) * sc * sb
// (z - c) * ~sa * ~sb = (~z - ~c) * sa * sb
// (z - a) * sa * ~sc = (~z - ~a) * sa * (c - a) * sc / norm(sb)
// (z - c) * ~sa * sc = (~z - ~c) * sa * (c - a) * sc / norm(sb)
// z * (sa * ~sc - ~sa * sc) = (~c - ~a) * sa * (c - a) * sc / norm(sb) + a * sa * ~sc - c * ~sa * sc =
//     = a * sa * ~sc - c * ~sa * sc + norm(c - a) * sa * sc / norm(sb)
fun excenter(a: Complex, b: Complex, c: Complex): Complex {
    val sa = (b - c).sqrt()
    val sb = (c - a).sqrt()
    val sc = (a - b).sqrt()
    return (a * sa * sc.conj() - c * sa.conj() * sc + sa * sc * (a - c).norm / sb.norm) / (sa * sc.conj() - sa.conj() * sc)
}

fun excircle(a: Complex, b: Complex, c: Complex) = excenter(a, b, c).let {
    circle(it, project(it, line(a, c)))
}

fun eulerLine(t: Triangle) = line(centroid(t), circumcenter(t))

fun circle(center: Complex, point: Complex) = Circle(center, (point - center).norm)

fun circumcircle(t: Triangle) = circle(circumcenter(t), t.a)

fun diameterCircle(l: Segment) = circle(midpoint(l), l.from)

fun midtriangle(t: Triangle) = Triangle((t.a + t.b) / 2, (t.b + t.c) / 2, (t.c + t.a) / 2)

fun eulerCenter(t: Triangle) = circumcenter(midtriangle(t))

fun eulerCircle(t: Triangle) = circumcircle(midtriangle(t))

fun gergonne(t: Triangle): Complex {
    val la = (t.b - t.c).abs()
    val lb = (t.c - t.a).abs()
    val lc = (t.a - t.b).abs()
    val pa = lb + lc - la
    val pb = lc + la - lb
    val pc = la + lb - lc
    return intersect(
        line(t.a, divide(pb.real() / pc, Segment(t.b, t.c))),
        line(t.b, divide(pa.real() / pc, Segment(t.a, t.c))),
    )
}

fun nagel(t: Triangle): Complex {
    val la = (t.b - t.c).abs()
    val lb = (t.c - t.a).abs()
    val lc = (t.a - t.b).abs()
    val pa = lb + lc - la
    val pb = lc + la - lb
    val pc = la + lb - lc
    return intersect(
        line(t.a, divide(pb.real() / pc, Segment(t.c, t.b))),
        line(t.b, divide(pa.real() / pc, Segment(t.c, t.a))),
    )
}

fun isogonal(p: Complex, t: Triangle) = circumcenter(Triangle(
    p.symmetry(line(t.a, t.b)),
    p.symmetry(line(t.b, t.c)),
    p.symmetry(line(t.c, t.a)),
))

fun length(l: Segment) = (l.from - l.to).abs()

fun normalize(a: Complex) = if (a == Complex.ZERO) Complex.ZERO else a / a.abs()

fun dir(l: Segment) = normalize(l.to - l.from)

// (z - o) * (~z - ~o) = r
// (z - o) * (~a - ~o) + (~z - ~o) * (a - o) = 2 * r
// (z - o) * (~a - ~o) + (a - o) * r / (z - o) = 2 * r
// (z - o)^2 * (~a - ~o) - 2*r*(z - o) + (a - o) * r = 0
// z^2 * (~a - ~o) - 2 * (r + o * (~a - ~o)) * z + ((a - o) * r + 2 * o * r + o^2 * (~a - ~o))
fun tangentPoint(option: TrinomialOption, a: Complex, c: Circle) = root(option,
    (a - c.center).conj(),
    -c.radiusSqr.real() * 2 - c.center * (a - c.center).conj() * 2,
    (a + c.center) * c.radiusSqr + c.center * c.center * (a - c.center).conj()
)

fun tangent(option: TrinomialOption, a: Complex, c: Circle) =
    (tangentPoint(option, a, c) - c.center).let {
        Line(it, -(it*c.center.conj()).re * 2 - c.radiusSqr * 2)
    }
