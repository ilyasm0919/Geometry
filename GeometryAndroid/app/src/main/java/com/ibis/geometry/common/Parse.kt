package com.ibis.geometry.common

import androidx.compose.ui.graphics.Color
import kotlin.math.sqrt

//Random(7605164863913659101)
class ParseContext(
    val names: MutableMap<String, Reactive<Geometric>>,
    var movableSource: String?
)

inline fun<A, B, C> Pair<A, C>.mapFst(selector: (A) -> B) = selector(first) to second
inline fun<A, B, C> Pair<A, B>.mapSnd(selector: (B) -> C) = first to selector(second)

fun parse(content: String) = ParseContext(mutableMapOf(), null).run {
    content.lines().filter(String::isNotBlank).map(::parseLine).sequenceA()
}

fun ParseContext.parseLine(init: String): Reactive<Pair<Drawable, Movable?>> {
    val (modifiers, line) = parseModifiers(init)
    val index = line.indexOf("=")
    val value = line.substring(index + 1).trimStart()
    val (res, next) = parseSum(value)
    check(next.isBlank()) { "Line not consumed: $next" }
    val name = index.takeIf { it != -1 }?.let {
        line.substring(0, it).trimEnd()
    }?.also { names[it] = res }
    val spanned = name?.let(::spans)
    val movable = movableSource
    movableSource = null
    return res.map { geometric ->
        val drawable = geometric.toDrawable()
        modifiers.forEach { it(drawable.style) }
        drawable.style.name = spanned
        drawable to movable?.let { Movable(geometric as? Complex ?: error("Expected point"), it) }
    }
}

fun<T> parseInfix(
    init: String,
    parser: (String) -> Pair<T, String>,
    vararg ops: Pair<String, (T, T) -> T>
): Pair<T, String> {
    var (res, line) = parser(init)
    line = line.trimStart()
    while (true) {
        val op = ops.firstOrNull { line.startsWith(it.first) } ?: return res to line
        val (next, nextLine) = parser(line.substring(op.first.length).trimStart())
        res = op.second(res, next)
        line = nextLine
    }
}

fun ParseContext.parseSum(line: String) =
    parseInfix(line, ::parseProd, "+" to Complex::plus::precast, "-" to Complex::minus::precast)

fun ParseContext.parseProd(line: String) =
    parseInfix(line, ::parsePow, "*" to Complex::times::precast, "/" to Complex::div::precast)

fun ParseContext.parsePow(init: String): Pair<Reactive<Geometric>, String> {
    var (res, line) = parseApp(init)
    line = line.trimStart()
    while (line.startsWith("^")) {
        val (pow, next) = parseReal(line.substring(1).trimStart()) ?: error("Expected number")
        res = { it: Complex -> (it.ln()*pow).exp() }.precast(res)
        line = next.trimStart()
    }
    return res to line
}

fun ParseContext.parseApp(line: String): Pair<Reactive<Geometric>, String> = when {
    line.startsWith("~") ->
        parseApp(line.substring(1).trimStart()).mapFst(Complex::conj::precast)
    line.startsWith("(") ->
        parseSum(line.substring(1).trimStart()).mapSnd {
            check(it.startsWith(")")) { IllegalArgumentException("Expected ')'") }
            it.substring(1)
        }
    line.startsWith("#") ->
        parseApp(line.substring(1).trimStart()).let {
            movableSource = line.substring(0, line.length - it.second.length)
            it
        }
    else -> parseAtom(line)
}

fun ParseContext.parseAtom(line: String): Pair<Reactive<Geometric>, String> =
    parseWord(line)?.let { (word, next) ->
        if (next.startsWith("(")) {
            val (args, next2) = if (next.substring(1).trimStart().startsWith(")"))
                emptyList<Reactive<Geometric>>() to next.substring(1).trimStart()
            else parseInfix(next.substring(1),
                { parseSum(it).mapFst(::listOf) }, "," to { a, b -> a + b }
            )
            check(next2.startsWith(")")) { "Expected ')'" }
            function(word, args) to next2.substring(1).trimStart()
        }
        else names[word]?.to(next) ?: error("Name not found: $word")
    } ?: parseComplex(line)?.let { (res, next) -> Static(res) to next.trimStart() }
    ?: error("Parsing failed: $line")

fun function(name: String, args: Collection<Reactive<Geometric>>): Reactive<Geometric> {
    fun reactive(action: ArgParser.() -> Geometric) = args.traverse {
        parseArgs(it, action)
    }
    @Suppress("ComplexRedundantLet")
    return when (name) {
        "point" -> reactive { point() }
        "line" -> reactive { line() }
        "segment" -> reactive { segment() }
        "circle" -> reactive { circle() }
        "triangle" -> reactive { triangle() }
        "polygon" -> reactive { polygon() }
        "intersect" -> reactive { intersect(line(), line()) }
        "cintersect" -> reactive { cintersect(TrinomialOption.RootNot(point()), line(), circle()) }
        "cintersect1" -> reactive { cintersect(TrinomialOption.Root1, line(), circle()) }
        "cintersect2" -> reactive { cintersect(TrinomialOption.Root2, line(), circle()) }
        "ccintersect" -> reactive { ccintersect(TrinomialOption.RootNot(point()), circle(), circle()) }
        "ccintersect1" -> reactive { ccintersect(TrinomialOption.Root1, circle(), circle()) }
        "ccintersect2" -> reactive { ccintersect(TrinomialOption.Root2, circle(), circle()) }
        "midpoint" -> reactive { midpoint(segment()) }
        "divide" -> reactive { divide(point(), segment()) }
        "midline" -> reactive { midline(segment()) }
        "parallel" -> reactive { parallel(point(), line()) }
        "perpendicular" -> reactive { perpendicular(point(), line()) }
        "project" -> reactive { project(point(), line()) }
        "cproject" -> reactive { cproject(point(), circle()) }
        "bisector" -> reactive { bisector(point(), point(), point()) }
        "exbisector" -> reactive { exbisector(point(), point(), point()) }
        "centroid" -> reactive { centroid(polygon()) }
        "circumcenter" -> reactive { circumcenter(triangle()) }
        "orthocenter" -> reactive { orthocenter(triangle()) }
        "incenter" -> reactive { incenter(triangle()) }
        "incircle" -> reactive { incircle(triangle()) }
        "excenter" -> reactive { excenter(point(), point(), point()) }
        "excircle" -> reactive { excircle(point(), point(), point()) }
        "euler_line" -> reactive { eulerLine(triangle()) }
        "diameter_circle" -> reactive { diameterCircle(segment()) }
        "midtriangle" -> reactive { midtriangle(triangle()) }
        "euler_center" -> reactive { eulerCenter(triangle()) }
        "euler_circle" -> reactive { eulerCircle(triangle()) }
        "gergonne" -> reactive { gergonne(triangle()) }
        "nagel" -> reactive { nagel(triangle()) }
        "isogonal" -> reactive { isogonal(point(), triangle()) }
        "circumcircle" -> reactive { circumcircle(triangle()) }
        "re" -> reactive { point().re.real() }
        "im" -> reactive { point().im.real() }
        "sqr" -> reactive { point().let { it * it } }
        "sqrt" -> reactive { point().sqrt() }
        "exp" -> reactive { point().exp() }
        "ln" -> reactive { point().ln() }
        "abs" -> reactive { point().abs().real() }
        "length" -> reactive { length(segment()).real() }
        "normalize" -> reactive { normalize(point()) }
        "dir" -> reactive { dir(segment()) }
        "radius" -> reactive { sqrt(circle().radiusSqr).real() }
        "center" -> reactive { circle().center }
        "tangentPoint" -> reactive { tangentPoint(TrinomialOption.RootNot(point()), point(), circle()) }
        "tangentPoint1" -> reactive { tangentPoint(TrinomialOption.Root1, point(), circle()) }
        "tangentPoint2" -> reactive { tangentPoint(TrinomialOption.Root2, point(), circle()) }
        "tangent" -> reactive { tangent(TrinomialOption.RootNot(point()), point(), circle()) }
        "tangent1" -> reactive { tangent(TrinomialOption.Root1, point(), circle()) }
        "tangent2" -> reactive { tangent(TrinomialOption.Root2, point(), circle()) }
        "symmetry" -> reactive { geometric().symmetry(line()) }
        "translation" -> reactive { geometric().translation(point()) }
        "homothety" -> reactive { geometric().homothety(point(), point()) }
        "inversion" -> reactive { geometric().inversion(circle()) }
        "time" -> {
            check(args.isEmpty()) { "Unexpected arguments" }
            Dynamic { time.real() }
        }
        "choose" -> reactive { geometric().choose(point().abs()) }
        "line_trace" -> Static(args.single().let {
            line(
                it(ReactiveInput(3658)) as? Complex ?: error("Expected point"),
                it(ReactiveInput(9137)) as? Complex ?: error("Expected point")
            )
        })
        "circle_trace" -> Static(args.single().let {
            circumcircle(
                Triangle(
                    it(ReactiveInput(2709)) as? Complex ?: error("Expected point"),
                    it(ReactiveInput(5073)) as? Complex ?: error("Expected point"),
                    it(ReactiveInput(6349)) as? Complex ?: error("Expected point")
                )
            )
        })
        "assert" -> reactive {
            val x = point()
            val y = point()
            check((x - y).norm < x.norm * 0.0001f) { "Assertion failed: $x != $y" }
            x
        }
        else -> error("Fun not found: $name")
    }
}

fun parseReal(init: String): Pair<Float, String>? {
    val (sign, line) = if (init.startsWith("-")) -1 to init.substring(1) else 1 to init
    return line.takeWhile(".0123456789"::contains).let {
        it.toFloatOrNull()?.let { n -> n * sign to line.substring(it.length) }
    }
}

fun parseComplex(line: String) = when {
    line.startsWith("i") -> Complex.I to line.substring(1)
    line.startsWith("-i") -> -Complex.I to line.substring(2)
    else -> parseReal(line)?.let { (res, next) ->
        if (next.startsWith("i")) res.imagine() to next.substring(1)
        else res.real() to next
    }
}

fun parseModifier(line: String): Pair<(Style) -> Unit, String> = parseWord(line)?.let { (word, next) ->
    fun set(action: Style.() -> Unit): (Style) -> Unit = action
    when (word) {
        "hide" -> set { border = Border.No }
        "dot" -> set { border = Border.Dot }
        "dash" -> set { border = Border.Dash }
        "dash_dot" -> set { border = Border.DashDot }
        "fill" -> set { fill = true }
        "red" -> set { color = Color(0xFFB20000) }
        "green" -> set { color = Color(0xFF00B200) }
        "blue" -> set { color = Color(0xFF0000B2) }
        "orange" -> set { color = Color(0xFFB26600) }
        "violet" -> set { color = Color(0xFF9400D3) }
        "white" -> set { color = Color(0xFFFFFFFF) }
        "gray" -> set { color = Color(0xFF808080) }
        "black" -> set { color = Color(0xFF000000) }
        else -> error("Invalid modifier: $word")
    } to next
} ?: error("Expected modifier: $line")

fun parseModifiers(init: String): Pair<List<(Style) -> Unit>, String> {
    var line = init.trimStart()
    val modifiers = mutableListOf<(Style) -> Unit>()
    while (line.startsWith("[")) {
        val (modifier, next) = parseModifier(line.substring(1))
        check(next.startsWith("]")) { "Expected ']'" }
        modifiers += modifier
        line = next.substring(1).trimStart()
    }
    return modifiers to line
}

fun parseWord(line: String): Pair<String, String>? =
    line.takeWhile { it.isLetterOrDigit() || it in "_'{}" }.takeIf { it.isNotEmpty() && it[0].isLetter() && it != "i" }?.let {
        it to line.substring(it.length).trimStart()
    }
