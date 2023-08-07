package com.ibis.geometry.common

import androidx.compose.ui.graphics.Color

typealias LocalFunctions = MutableMap<String, (List<Reactive<Geometric>>) -> Reactive<Geometric>>

//Random(7605164863913659101)
class ParseContext(
    val names: MutableMap<String, Reactive<Geometric>>,
    val localFunctions: LocalFunctions,
    var movableSource: String?
)

inline fun<A, B, C> Pair<A, C>.mapFst(selector: (A) -> B) = selector(first) to second
inline fun<A, B, C> Pair<A, B>.mapSnd(selector: (B) -> C) = first to selector(second)

fun LocalFunctions.parse(content: String) = ParseContext(mutableMapOf(), this, null).run {
    content.lines().filter {
        it.isNotBlank() && it.trimStart()[0] != '!'
    }.mapNotNull(::parseLine).sequenceA()
}

fun parseGlobal(content: String) = ParseContext(mutableMapOf(), mutableMapOf(), null).run {
    content.lines().filter(String::isNotBlank).forEach {
        check(it.trimStart().startsWith("fun")) { "Expected 'fun'" }
        parseFun(it.trimStart().substring(3).trimStart())
    }
    localFunctions
}

fun ParseContext.parseLine(init: String): Reactive<Pair<Drawable, Movable?>>? {
    if (init.trimStart().startsWith("fun")) {
        parseFun(init.trimStart().substring(3).trimStart())
        return null
    }

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
        drawable.style.name = spanned
        modifiers.forEach { it(drawable.style) }
        drawable to movable?.let { Movable(geometric as? Complex ?: error("Expected point"), it) }
    }
}

fun ParseContext.parseFun(init: String) {
    val (name, next) = parseWord(init) ?: error("Parsing failed: $init")
    check(next.startsWith("(")) { "Expected '('" }
    val (args, next2) = parseInfix(next.substring(1).trimStart(), {
        parseWord(it)?.let { (type, next) ->
            parseWord(next)?.mapFst {
                listOf(it to when (type) {
                    "Object" -> geometric
                    "Real", "Complex", "Point" -> point
                    "Line" -> line
                    "Segment" -> segment
                    "Triangle" -> triangle
                    "Polygon" -> polygon
                    "Circle" -> circle
                    "Angle" -> angle
                    else -> error("Type not found: $type")
                })
            }
        } ?: error("Parsing failed: $it")
    }, "," to { a, b -> a + b })
    check(next2.startsWith(")")) { "Expected ')'" }
    check(next2.substring(1).trimStart().startsWith("=")) { "Expected '='" }
    val newContext = ParseContext(mutableMapOf<String, Reactive<Geometric>>().apply {
        putAll(names)
        args.forEachIndexed { i, (n, _) ->
            put(n, Dynamic(timeUsed = false, argsUsed = true) {
                this.args[i]
            })
        }
    }, localFunctions, null)
    val (body, next3) = newContext.parseSum(next2.substring(1).trimStart().substring(1).trimStart())
    check(next3.isBlank()) { "Line not consumed: $next3" }
    localFunctions.put(name) {
        body.withArgs(parseArgs(it) {
            args.map { it.second() }
        })
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
            (localFunctions[word] ?: functions.flatMap { it.second }.find { it.name == word }?.parser)?.invoke(args)?.to(next2.substring(1).trimStart())
        } else names[word]?.to(next) ?: error("Name not found: $word")
    }?.let { (obj, next) ->
        if (next.startsWith(".")) parseInt(next.substring(1))?.mapFst { index ->
            obj.map {
                when (it) {
                    is AbstractPolygon -> it.points[index]
                    is Segment -> when (index) {
                        0 -> it.from
                        1 -> it.to
                        else -> throw IndexOutOfBoundsException()
                    }
                    else -> error("Invalid indexation")
                }
            }
        } else obj to next
    } ?: parseComplex(line)?.let { (res, next) -> Static(res) to next.trimStart() }
    ?: error("Parsing failed: $line")

fun parseInt(line: String) = line.takeWhile("0123456789"::contains).let {
    it.toIntOrNull()?.let { n -> n to line.substring(it.length) }
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
        "hide_label" -> set { name = null }
        "bounded" -> set { bounded = true }
        "equal1" -> set { equalityGroup = EqualityGroup.Equal1 }
        "equal2" -> set { equalityGroup = EqualityGroup.Equal2 }
        "equal3" -> set { equalityGroup = EqualityGroup.Equal3 }
        "equalV" -> set { equalityGroup = EqualityGroup.EqualV }
        "equalO" -> set { equalityGroup = EqualityGroup.EqualO }
        "dark_red" -> set { color = Color(0xFF87221D) }
        "red" -> set { color = Color(0xFFB20000) }
        "light_red" -> set { color = Color(0xFFCE4B46) }
        "dark_green" -> set { color = Color(0xFF259826) }
        "green" -> set { color = Color(0xFF00B200) }
        "light_green" -> set { color = Color(0xFFAAE9B2) }
        "dark_blue" -> set { color = Color(0xFF22226D) }
        "blue" -> set { color = Color(0xFF0000B2) }
        "light_blue" -> set { color = Color(0xFF3DD5FF) }
        "orange" -> set { color = Color(0xFFB26600) }
        "violet" -> set { color = Color(0xFF9400D3) }
        "purple" -> set { color = Color(0xFF7851A9) }
        "white" -> set { color = Color(0xFFFFFFFF) }
        "dark_gray" -> set { color = Color(0xFF303030) }
        "gray" -> set { color = Color(0xFF808080) }
        "light_grray" -> set { color = Color(0xFFBABABA) }
        "black" -> set { color = Color(0xFF000000) }
        "scale" -> {
            check(next.startsWith("(")) { error("Expected '('") }
            parseReal(next.substring(1).trimStart())?.let { (num, next) ->
                check(num > 0) { "scale's argument has to be positive" }
                check(next.startsWith(")")) { error("Expected ')'") }
                return set { scale = num } to next.substring(1)
            } ?: error("Parsing failed: $next")
        }
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

fun parseWord(line: String): Pair<String, String>? = line.takeWhile {
    it.isLetterOrDigit() || it in "_'{}"
}.takeIf {
    it.isNotEmpty() && it[0].isLetter() && it != "i"
}?.let {
    it to line.substring(it.length).trimStart()
}
