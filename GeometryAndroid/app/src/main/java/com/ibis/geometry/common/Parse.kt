package com.ibis.geometry.common

import androidx.compose.ui.graphics.Color

//Random(7605164863913659101)
class ParseContext(
    val names: MutableMap<String, Reactive<Geometric>>,
    var movableSource: String?
)

inline fun<A, B, C> Pair<A, C>.mapFst(selector: (A) -> B) = selector(first) to second // to - Pair constructor
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
        drawable.style.name = spanned
        modifiers.forEach { it(drawable.style) }
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
            functions.flatMap { it.second }.find { it.name == word }?.parser?.invoke(args)?.to(next2.substring(1).trimStart())
        }
        else names[word]?.to(next) ?: error("Name not found: $word")
    } ?: parseComplex(line)?.let { (res, next) -> Static(res) to next.trimStart() }
    ?: error("Parsing failed: $line")

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
        "scale" -> {
            check(next.startsWith("(")) { error("Modifier \"scale\" is a function") }
            parseReal(next.substring(1))?.let { (num, next) ->
                if (num < 0) error("scale's argument has to be positive")
                check(next.startsWith(")")) { error("Missing \")\"") }
                return set { scale = num } to next.substring(1)
            }
        }
    }
    when (word) {
        "hide" -> set { border = Border.No }
        "dot" -> set { border = Border.Dot }
        "dash" -> set { border = Border.Dash }
        "dash_dot" -> set { border = Border.DashDot }
        "fill" -> set { fill = true }
        "hide_label" -> set { name = null }
        "equal1" -> set { equalityGroup = EqualityGroup.Equal1 }
        "equal2" -> set { equalityGroup = EqualityGroup.Equal2 }
        "equal3" -> set { equalityGroup = EqualityGroup.Equal3 }
        "equalV" -> set { equalityGroup = EqualityGroup.EqualV }
        "equalO" -> set { equalityGroup = EqualityGroup.EqualO }
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
//        TODO("add parser for [function(args)]")
        val (modifier, next) = parseModifier(line.substring(1))
        check(next.startsWith("]")) { "Expected ']'" }
        modifiers += modifier
        line = next.substring(1).trimStart()
    }
    return modifiers to line
}

fun parseWord(line: String): Pair<String, String>? =
    line.takeWhile { it.isLetterOrDigit() || it in "_'{}" }
        .takeIf { it.isNotEmpty() && it[0].isLetter() && it != "i" }?.let {
        it to line.substring(it.length).trimStart()
    }
