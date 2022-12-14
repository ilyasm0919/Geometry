import java.io.File
import kotlin.system.exitProcess


data class GeoGenContext(
    private val text: StringBuilder = StringBuilder(),
    private val points: MutableMap<String, String> = mutableMapOf(),
    private val lines: MutableMap<String, String> = mutableMapOf(),
    val triangles: MutableMap<String, String> = mutableMapOf(),
    val circles: MutableMap<String, String> = mutableMapOf(),
) {
    fun text(line: String) = text.appendLine(line)
    private fun obj(value: String, prefix: String, from: MutableMap<String, String>, hide: Boolean = false) =
        when (val res = from[value]) {
            null -> (prefix + from.size.inc()).also {
                text("${"[hide] ".takeIf { hide }.orEmpty()}[gray] $it = $value")
                from[value] = it
            }
            else -> res
        }
    fun point(value: String, hide: Boolean) = obj(value, "p", points, hide)
    fun line(a: String, b: String) = obj("line($a, $b)", "l", lines)
    fun triangle(a: String, b: String, c: String) = obj("triangle($a, $b, $c)", "t", triangles)
    fun circle(value: String) = obj(value, "c", circles)
    fun circumcircle(t: String) = circle("circumcircle($t)")

    override fun toString() = text.toString()
}

fun GeoGenContext.parseGeoGenInitial(line: String) {
    val index = line.indexOf(":")
    check(index != -1) { "Expected initial object" }
    val names = line.substring(index + 1).split(",").map(String::trim)
    text(when (val name = line.substring(0, index).trim()) {
        "LineSegment" -> {
            check(names.size == 2) { "Expected 2 points" }
            """
                ${names[0]} = #(-60)
                ${names[1]} = #(60)
                [orange] segment(${names.joinToString()})
            """.trimIndent()
        }
        "Triangle" -> {
            check(names.size == 3) { "Expected 3 points" }
            triangles["triangle(${names.joinToString()})"] = "t"
            """
                ${names[0]} = #(20+60i)
                ${names[1]} = #(60-40i)
                ${names[2]} = #(-60-40i)
                [orange] [fill] t = triangle(${names.joinToString()})
            """.trimIndent()
        }
        "RightTriangle" -> {
            check(names.size == 3) { "Expected 3 points" }
            triangles["triangle(${names.joinToString()})"] = "t"
            """
                ${names[0]} = #(-60-20i)
                ${names[1]} = #(60-20i)
                ${names[2]} = project(#(-60+60i), perpendicular(${names[0]}, ${names[0]}, ${names[1]}))
                [orange] [fill] t = triangle(${names.joinToString()})
            """.trimIndent()
        }
        "Quadrilateral" -> {
            check(names.size == 4) { "Expected 4 points" }
            """
                ${names[0]} = #(20+60i)
                ${names[1]} = #(60-40i)
                ${names[2]} = #(-60-40i)
                ${names[3]} = #(-30+40i)
                [orange] [fill] polygon(${names.joinToString()})
            """.trimIndent()
        }
        "CyclicQuadrilateral" -> {
            check(names.size == 4) { "Expected 4 points" }
            circles["circumcircle(${names[0]}, ${names[1]}, ${names[2]})"] = "c"
            """
                ${names[0]} = #(20+60i)
                ${names[1]} = #(60-40i)
                ${names[2]} = #(-60-40i)
                c = circumcircle(A, B, C)
                ${names[3]} = cproject(#(-45+45i), c)
                [orange] [fill] polygon(${names.joinToString()})
            """.trimIndent()
        }
        "LineAndPoint" -> {
            check(names.size == 3) { "Expected 3 points" }
            """
                ${names[0]} = #(20+60i)
                ${names[1]} = #(60-40i)
                ${names[2]} = #(-60-40i)
                [orange] segment(${names[0]}, ${names[1]})
            """.trimIndent()
        }
        "LineAndTwoPoints" -> {
            check(names.size == 4) { "Expected 4 points" }
            """
                ${names[0]} = #(20+60i)
                ${names[1]} = #(60-40i)
                ${names[2]} = #(-60-40i)
                ${names[3]} = #(-30+40i)
                [orange] segment(${names[0]}, ${names[1]})
            """.trimIndent()
        }
        else -> error("Unexpected $name")
    })
}

fun GeoGenContext.parseGeoGenLine(line: String) {
    val index = line.indexOf("=")
    val open = line.indexOf("(")
    check(index != -1 && open > index && line.last() == ')') { "Expected definition" }
    val name = line.substring(0, index).trim()
    val args = line.substring(open + 1, line.length - 1)
        .filterNot("{}"::contains).split(",").map(String::trim)
    when (val funName = line.substring(index + 1, open).trim()) {
        "Centroid" -> "$name = centroid(${triangle(args[0], args[1], args[2])})"
        "CircleWithCenterThroughPoint" -> "$name = circle(${args.joinToString()})"
        "CircleWithDiameter" -> "$name = diameter_circle(${args.joinToString()})"
        "Circumcenter" -> "$name = center(${circumcircle(args.joinToString())})"
        "Circumcircle" -> "$name = circumcircle(${args.joinToString()})"
        "Excenter" -> "$name = center(${circle("excircle(${triangle(args[2], args[0], args[1])})")})"
        "Excircle" -> "$name = excircle(${triangle(args[2], args[0], args[1])})"
        "ExternalAngleBisector" -> "$name = exbisector(${args[2]}, ${args[0]}, ${args[1]})"
        "Incenter" -> "$name = center(${circle("incircle(${triangle(args[0], args[1], args[2])})")})"
        "Incircle" -> "$name = incircle(${triangle(args[0], args[1], args[2])})"
        "InternalAngleBisector" -> "$name = bisector(${args[2]}, ${args[0]}, ${args[1]})"
        "IntersectionOfLines" -> "$name = intersect(${args.joinToString()})"
        "IntersectionOfLineAndLineFromPoints" -> "$name = intersect(${args[0]}, ${line(args[1], args[2])})"
        "IntersectionOfLinesFromPoints" -> "$name = intersect(${line(args[0], args[1])}, ${line(args[2], args[3])})"
        "IsoscelesTrapezoidPoint" -> "$name = symmetry(${args[0]}, midline(${args[1]}, ${args[2]}))"
        "LineFromPoints" -> "$name = line(${args.joinToString()})"
        "LineThroughCircumcenter" -> "$name = line(${args[0]}, ${
            point(
                "circumcenter(${args.joinToString()})",
                false
            )
        })"
        "Median" -> "$name = line(${args[0]}, midpoint(${args[1]}, ${args[2]}))"
        "Midline" -> "$name = line(midpoint(${args[0]}, ${args[1]}), midpoint(${args[0]}, ${args[2]}))"
        "Midpoint" -> "$name = midpoint(${args.joinToString()})"
        "MidpointOfArc" -> "$name = cintersect(${args[0]}, " +
                "exbisector(${args[2]}, ${args[0]}, ${args[1]}), ${circumcircle(args.joinToString())})"
        "MidpointOfOppositeArc" -> "$name = cintersect(${args[0]}, " +
                "bisector(${args[2]}, ${args[0]}, ${args[1]}), ${circumcircle(args.joinToString())})"
        "NinePointCircle" -> "$name = euler_circle(${triangle(args[0], args[1], args[2])})"
        "OppositePointOnCircumcircle" -> "$name = center(${circumcircle(args.joinToString())}) * 2 - ${args[0]}"
        "Orthocenter" -> {
            fun angle(a: String, b: String, c: String) = point("intersect($a, $name, $b, $c)", true).let {
                text("[gray] segment($a, $it)")
                text("[gray] [fill] angle($a, $it, $b)")
            }
            text("$name = orthocenter(${triangle(args[0], args[1], args[2])})")
            angle(args[0], args[1], args[2])
            angle(args[1], args[2], args[0])
            angle(args[2], args[0], args[1])
            null
        }
        "ParallelLine" -> "$name = parallel(${args.joinToString()})"
        "ParallelLineToLineFromPoints" -> "$name = parallel(${args[0]}, ${line(args[1], args[2])})"
        "ParallelogramPoint" -> "$name = ${args[1]} + ${args[2]} - ${args[0]}"
        "PerpendicularBisector" -> "$name = midline(${args.joinToString()})"
        "PerpendicularLine" -> "$name = perpendicular(${args.joinToString()})"
        "PerpendicularLineToLineFromPoints" -> "$name = perpendicular(${args[0]}, ${line(args[1], args[2])})"
        "PerpendicularLineAtPointOfLine" -> "$name = perpendicular(${args[0]}, ${args.joinToString()})"
        "PerpendicularProjection" -> "$name = project(${args.joinToString()})"
        "PerpendicularProjectionOnLineFromPoints" -> "$name = project(${args[0]}, ${line(args[1], args[2])})"
        "PointReflection" -> "$name = ${args[1]} * 2 - ${args[0]}"
        "ReflectionInLine" -> "$name = symmetry(${args.joinToString()})"
        "ReflectionInLineFromPoints" -> "$name = symmetry(${args[0]}, ${line(args[1], args[2])})"
        "SecondIntersectionOfCircleAndLineFromPoints" -> "$name = cintersect(${args[0]}, " +
                "${line(args[0], args[1])}, ${circumcircle("${args[0]}, ${args[2]}, ${args[3]}")})"
        "SecondIntersectionOfTwoCircumcircles" -> "$name = ccintersect(${args[0]}, " +
                "${circumcircle("${args[0]}, ${args[1]}, ${args[2]}")}, " +
                "${circumcircle("${args[0]}, ${args[3]}, ${args[4]}")})"
        "TangentLine" -> "$name = tangent1(${args[0]}, ${circumcircle(args.joinToString())})"
        else -> error("Unexpected $funName")
    }?.let(::text)
}

fun GeoGenContext.parseGeoGenGoal(line: String) {
    val index = line.indexOf(":")
    val dash = line.indexOf("-")
    check(index != -1 && dash > index) { "Expected goal" }
    val args = line.substring(index + 1, dash)
        .filterNot("[]"::contains).split(",").map(String::trim)
    text(when (val name = line.substring(0, index).trim()) {
        "ConcyclicPoints" -> {
            check(args.size == 4) { "Expected 4 points" }
            "[red] [dash] circumcircle(${args[0]}, ${args[1]}, ${args[2]})"
        }
        "CollinearPoints" -> {
            check(args.size == 3) { "Expected 3 points" }
            "[red] [dash] line(${args[0]}, ${args[1]})"
        }
        "ConcurrentLines" -> {
            check(args.size == 6) { "Expected 6 points" }
            """
                [red] l = segment(${args[0]}, ${args[1]})
                [red] m = segment(${args[2]}, ${args[3]})
                [red] n = segment(${args[4]}, ${args[5]})
                [red] intersect(l, m)
            """.trimIndent()
        }
        "EqualLineSegments" -> {
            check(args.size == 4) { "Expected 4 points" }
            """
                [red] [equal1] segment(${args[0]}, ${args[1]})
                [red] [equal1] segment(${args[2]}, ${args[3]})
            """.trimIndent()
        }
        "LineTangentToCircle" -> {
            check(args.size == 5) { "Expected 5 points" }
            """
                [red] l = line(${args[0]}, ${args[1]})
                [red] c = circumcircle(${args[2]}, ${args[3]}, ${args[4]})
                [red] cintersect1(l, c)
            """.trimIndent()
        }
        "TangentCircles" -> {
            check(args.size == 6) { "Expected 6 points" }
            """
                [red] c1 = circumcircle(${args[0]}, ${args[1]}, ${args[2]})
                [red] c2 = circumcircle(${args[3]}, ${args[4]}, ${args[5]})
                [red] ccintersect1(c1, c2)
            """.trimIndent()
        }
        "ParallelLines" -> {
            check(args.size == 4) { "Expected 4 points" }
            """
                [red] segment(${args[0]}, ${args[1]})
                [red] segment(${args[2]}, ${args[3]})
            """.trimIndent()
        }
        "PerpendicularLines" -> {
            check(args.size == 4) { "Expected 4 points" }
            """
                [red] l = line(${args[0]}, ${args[1]})
                [red] m = line(${args[2]}, ${args[3]})
                [red] [fill] [dot] angle(${args[0]}, intersect(l, m), ${args[2]})
            """.trimIndent()
        }
        else -> error("Unexpected $name")
    })
}

fun parseGeoGen(text: String) = text.lines().filter(String::isNotBlank).dropLast(4).let {
    GeoGenContext().run {
        parseGeoGenInitial(it[0])
        it.subList(1, it.size - 1).forEach(::parseGeoGenLine)
        parseGeoGenGoal(it.last())
        toString()
    }
}

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("""
            Usage:
                GeoGenIntegration input output
                
                input   Input file (readable output from GeoGen) for converting.
                output  Output directory
        """.trimIndent())
        exitProcess(-1)
    }

    val input = File(args[0])
    if (!input.canRead()) {
        println("Cannot read file: $input")
        exitProcess(-2)
    }

    val output = File(args[1])
    if (!output.isDirectory) {
        println("Directory does not exists: $output")
        exitProcess(-3)
    }

    var errors = 0
    input.readText().split(Regex("^-+$", RegexOption.MULTILINE)).forEachIndexed { index, str ->
        if (index == 0 || index % 2 != 0) return@forEachIndexed
        try {
            File(output, input.nameWithoutExtension + (index / 2) + ".geo")
                .writeText(parseGeoGen(str))
        } catch (e: Exception) {
            println(e.toString())
            errors++
        }
    }
    exitProcess(errors)
}
