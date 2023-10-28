import kotlin.system.exitProcess
import okio.*
import okio.Path.Companion.toPath

data class GeoGenContext(
    private val out: BufferedSink,
    private val points: MutableMap<String, String> = mutableMapOf(),
    private val lines: MutableMap<String, String> = mutableMapOf(),
    val triangles: MutableMap<String, String> = mutableMapOf(),
    val circles: MutableMap<String, String> = mutableMapOf(),
) {
    fun text(line: String = "") {
        out.writeUtf8(line)
        out.writeUtf8("\n")
    }
    fun comment(line: String) {
        out.writeUtf8("!")
        text(line)
    }
    private fun obj(value: String, prefix: String, from: MutableMap<String, String>, modificators: String) =
        when (val res = from[value]) {
            null -> (prefix + "_{" + from.size.inc() + "}").also {
                text("$modificators $it = $value")
                from[value] = it
            }
            else -> res
        }
    fun point(value: String, modificators: String = "[gray]") = obj(value, "p", points, modificators)
    fun line(a: String, b: String, modificators: String = "[bounded] [gray]") = obj("line($a, $b)", "l", lines, modificators)
    fun triangle(a: String, b: String, c: String) = obj("triangle($a, $b, $c)", "t", triangles, "[gray]")
    fun circle(value: String, modificators: String = "[gray]") = obj(value, "c", circles, modificators)
    fun circumcircle(t: String, modificators: String = "[gray]") = circle("circumcircle($t)", modificators)
}

fun GeoGenContext.parseGeoGenInitial(line: String) {
    val index = line.indexOf(":")
    check(index != -1) { "Expected initial object" }
    val names = line.substring(index + 1).split(",").map(String::trim)
    comment(line)
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
                ${names[1]} = #(-60-20i)
                ${names[2]} = #(60-20i)
                ${names[0]} = cproject(#(-20), diameter_circle(${names[1]}, ${names[2]}))
                [orange] [fill] t = triangle(${names.joinToString()})
                [gray] [fill] angle(${names[1]}, ${names[0]}, ${names[2]})
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
    text()
}

fun GeoGenContext.parseGeoGenLine(line: String) {
    val index = line.indexOf("=")
    val open = line.indexOf("(")
    check(index != -1 && open > index && line.last() == ')') { "Expected definition" }
    val name = line.substring(0, index).trim()
    val args = line.substring(open + 1, line.length - 1)
        .filterNot("[](){}"::contains).split(",").map(String::trim)
    comment(line)
    when (val funName = line.substring(index + 1, open).trim()) {
        "Centroid" -> "$name = centroid(${triangle(args[0], args[1], args[2])})"
        "CircleWithCenterThroughPoint" -> "$name = circle(${args.joinToString()})"
        "CircleWithDiameter" -> "$name = diameter_circle(${args.joinToString()})"
        "Circumcenter" -> "$name = center(${circumcircle(args.joinToString())})"
        "Circumcircle" -> "$name = circumcircle(${args.joinToString()})"
        "Excenter" -> "$name = center(${circle("excircle(${args[2]}, ${args[0]}, ${args[1]})")})"
        "Excircle" -> "$name = excircle(${args[2]}, ${args[0]}, ${args[1]})"
        "ExternalAngleBisector" -> "$name = exbisector(${args[2]}, ${args[0]}, ${args[1]})"
        "Incenter" -> "$name = center(${circle("incircle(${triangle(args[0], args[1], args[2])})")})"
        "Incircle" -> "$name = incircle(${triangle(args[0], args[1], args[2])})"
        "InternalAngleBisector" -> "$name = bisector(${args[2]}, ${args[0]}, ${args[1]})"
        "IntersectionOfLines" -> "$name = intersect(${args.joinToString()})"
        "IntersectionOfLineAndLineFromPoints" -> "$name = intersect(${args[0]}, ${line(args[1], args[2])})"
        "IntersectionOfLinesFromPoints" -> "$name = intersect(${line(args[0], args[1])}, ${line(args[2], args[3])})"
        "IsoscelesTrapezoidPoint" -> "$name = symmetry(${args[0]}, midline(${args[1]}, ${args[2]}))"
        "LineFromPoints" -> "[bounded] $name = line(${args.joinToString()})"
        "LineThroughCircumcenter" -> "$name = line(${args[0]}, ${
            point("circumcenter(${args.joinToString()})")
        })"
        "Median" -> "$name = line(${args[0]}, midpoint(${args[1]}, ${args[2]}))"
        "Midline" -> "$name = line(midpoint(${args[0]}, ${args[1]}), midpoint(${args[0]}, ${args[2]}))"
        "Midpoint" -> "$name = midpoint(${args.joinToString()})".also {
            text("[gray] segment(${args.joinToString()})")
        }
        "MidpointOfArc" -> "$name = cintersect(${args[0]}, " +
                "exbisector(${args[2]}, ${args[0]}, ${args[1]}), ${circumcircle(args.joinToString())})"
        "MidpointOfOppositeArc" -> "$name = cintersect(${args[0]}, " +
                "bisector(${args[2]}, ${args[0]}, ${args[1]}), ${circumcircle(args.joinToString())})"
        "NinePointCircle" -> "$name = euler_circle(${triangle(args[0], args[1], args[2])})"
        "OppositePointOnCircumcircle" -> "$name = center(${circumcircle(args.joinToString())}) * 2 - ${args[0]}"
        "Orthocenter" -> {
            fun angle(a: String, b: String, c: String) = point("intersect($a, $name, $b, $c)", "[dot]").let {
                text("[bounded] [gray] line($a, $it)")
                text("[bounded] [gray] line($b, $c)")
                text("[gray] [fill] angle($a, $it, $b)")
            }
            text("$name = orthocenter(${triangle(args[0], args[1], args[2])})")
            angle(args[0], args[1], args[2])
            angle(args[1], args[2], args[0])
            angle(args[2], args[0], args[1])
            null
        }
        "TangentLinesIntersection" -> "$name = inversion((${args[1]} + ${args[2]}) / 2, circumcircle(${args.joinToString()}))"
        "HumptyPoint" -> "$name = humpty(${args[1]}, ${args[0]}, ${args[2]})"
        "DumptyPoint" -> "$name = dumpty(${args[1]}, ${args[0]}, ${args[2]})"
        "Isogonal" -> "$name = isogonal(${args.joinToString()})"
        "Isotomic" -> "$name = isotomic(${args.joinToString()})"
        "Centroid4" -> "$name = centroid(${args.joinToString()})"

        "Lemoine" -> "$name = lemoine(${args.joinToString()})"
        "Nagel" -> "$name = nagel(${args.joinToString()})"
        "Gergonne" -> "$name = gergonne(${args.joinToString()})"

        "Symedian" -> "[bounded] [gray] $name = symedian(${args[1]}, ${args[0]}, ${args[2]})"
        "RadiusLine" -> "[bounded] [gray] $name = line(${args[0]}, circumcenter(${args[1]}, ${args[0]}, ${args[2]}))"


        "SymedianEnd" -> "$name = intersect(symedian(${args[1]}, ${args[0]}, ${args[2]}), ${args[1]}, ${args[2]})"
        "RadiusLineEnd" -> "$name = intersect(${args[0]}, circumcenter(${args[1]}, ${args[0]}, ${args[2]}), ${args[1]}, ${args[2]})"
        "ExternalAngleBisectorEnd" -> "$name = intersect(exbisector(${args[1]}, ${args[0]}, ${args[2]}), ${args[1]}, ${args[2]})"
        "BisectorEnd" -> "$name = intersect(bisector(${args[1]}, ${args[0]}, ${args[2]}), ${args[1]}, ${args[2]})"

        "HeightMiddle" -> "$name = midpoint(${args[0]}, project(${args[0]}, ${args[1]}, ${args[2]}))"
        "MedianMiddle" -> "$name = midpoint(${args[0]}, midpoint(${args[1]}, ${args[2]}))"
        "SymedianMiddle" -> "$name = midpoint(${args[0]}, intersect(symedian(${args[1]}, ${args[0]}, ${args[2]}), ${args[1]}, ${args[2]}))"
        "RadiusLineMiddle" -> "$name = midpoint(${args[0]}, intersect(${args[0]}, circumcenter(${args[1]}, ${args[0]}, ${args[2]}), ${args[1]}, ${args[2]}))"
        "ExternalAngleBisectorMiddle" -> "$name = midpoint(${args[0]}, intersect(exbisector(${args[1]}, ${args[0]}, ${args[2]}), ${args[1]}, ${args[2]}))"
        "BisectorMiddle" -> "$name = midpoint(${args[0]}, intersect(bisector(${args[1]}, ${args[0]}, ${args[2]}), ${args[1]}, ${args[2]}))"

        "ParallelLine" -> "$name = parallel(${args.joinToString()})"
        "ParallelLineToLineFromPoints" -> "$name = parallel(${args[0]}, ${line(args[1], args[2])})"
        "ParallelogramPoint" -> {
            text("$name = ${args[1]} + ${args[2]} - ${args[0]}")
            text("[violet] [fill] polygon(${args[1]}, ${args[0]}, ${args[2]}, $name)")
            null
        }
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
    text()
}

fun GeoGenContext.parseGeoGenGoal(line: String) {
    val index = line.indexOf(":")
    check(index != -1) { "Expected goal" }
    comment(line)
    val args = buildList {
        var l = line.substring(index + 1).trimStart()
        while (true) {
            add(when (l[0]) {
                '[' -> {
                    val a = l.indexOf(",")
                    val b = l.indexOf("]", a + 1)
                    line(l.substring(1, a), l.substring(a + 1, b).trimStart(), "[red]").also {
                        l = l.substring(b + 1)
                    }
                }
                '(' -> {
                    val a = l.indexOf(")")
                    circumcircle(l.substring(1, a), "[red]").also {
                        l = l.substring(a + 1)
                    }
                }
                else -> {
                    val a = l.indexOfFirst { !it.isLetterOrDigit() }
                    l.substring(0, a).also {
                        l = l.substring(a)
                    }
                }
            })
            if (l.startsWith(",")) l = l.substring(1).trimStart()
            else break
        }
    }
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
            check(args.size == 3) { "Expected 3 lines" }
            "[red] intersect(${args[0]}, ${args[1]})"
        }
        "EqualLineSegments" -> {
            check(args.size == 4) { "Expected 4 points" }
            """
                [red] [equal1] segment(${args[0]}, ${args[1]})
                [red] [equal1] segment(${args[2]}, ${args[3]})
            """.trimIndent()
        }
        "LineTangentToCircle" -> {
            check(args.size == 2) { "Expected 1 circle and 1 line" }
            "cintersect1(${args[1]}, ${args[0]})"
        }
        "TangentCircles" -> {
            check(args.size == 2) { "Expected 2 circles" }
            "[red] ccintersect1(${args[0]}, ${args[1]})"
        }
        "ParallelLines" -> {
            check(args.size == 2) { "Expected 2 lines" }
            ""
        }
        "PerpendicularLines" -> {
            check(args.size == 2) { "Expected 2 lines" }
            """
                [hide] p = intersect(${args[0]}, ${args[1]})
                [red] [fill] [dot] angle(intersect(${args[0]}, p+1, p+i), p, intersect(${args[1]}, p+1, p+i))
            """.trimIndent()
        }
        else -> error("Unexpected $name")
    })
}

fun parseGeoGen(text: BufferedSource, out: BufferedSink) = GeoGenContext(out).run {
    parseGeoGenInitial(text.readUtf8Line() ?: error("No line"))

    while (true) {
        val line = text.readUtf8Line() ?: error("No line")
        if (line == "") break
        parseGeoGenLine(line)
    }

    parseGeoGenGoal(text.readUtf8Line() ?: error("No line"))
    text.readUtf8Line()?.takeIf { it == "" } ?: error("Bad line")
    text()
    comment(text.readUtf8Line() ?: error("No line"))
    comment(text.readUtf8Line() ?: error("No line"))
    comment(text.readUtf8Line() ?: error("No line"))
    comment(text.readUtf8Line() ?: error("No line"))
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

    val fs = FileSystem.SYSTEM
    val input = args[0].toPath()
    if (!fs.exists(input)) {
        println("Cannot read file: $input")
        exitProcess(-2)
    }

    val output = args[1].toPath()
    if (!fs.exists(output)) {
        println("Directory does not exists: $output")
        exitProcess(-3)
    }

    fs.read(input) {
        var number = 1
        while (true) {
            if (number != 1) check((readUtf8Line() ?: break) == "") { "Bad line" }
            readUtf8Line()?.takeIf { it.all { it == '-' } } ?: error("Bad line")
            readUtf8Line()?.takeIf { it.startsWith("Theorem ") } ?: error("Bad line")
            readUtf8Line()?.takeIf { it.all { it == '-' } } ?: error("Bad line")
            readUtf8Line()?.takeIf { it == "" } ?: error("Bad line")
            try {
                fs.write((output / "${input.name}$number.geo"), true) {
                    parseGeoGen(this@read, this@write)
                }
            } catch (e: Exception) {
                println(e.toString())
                exitProcess(number)
            }
            number++
        }
    }
}
