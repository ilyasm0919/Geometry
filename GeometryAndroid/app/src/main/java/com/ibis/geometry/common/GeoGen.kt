package com.ibis.geometry.common

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.Dialog
import com.ibis.geometry.common.theme.Typography

fun parseGeoGenInitial(line: String): String {
    val index = line.indexOf(":")
    check(index != -1) { "Expected initial object" }
    val names = line.substring(index + 1).split(",").map(String::trim)
    return when (val name = line.substring(0, index).trim()) {
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
            """
                ${names[0]} = #(20+60i)
                ${names[1]} = #(60-40i)
                ${names[2]} = #(-60-40i)
                [orange] [fill] triangle(${names.joinToString()})
            """.trimIndent()
        }
        "RightTriangle" -> {
            check(names.size == 3) { "Expected 3 points" }
            """
                ${names[0]} = #(-60-20i)
                ${names[1]} = #(60-20i)
                ${names[2]} = project(#(-60+60i), perpendicular(${names[0]}, ${names[0]}, ${names[1]}))
                [orange] [fill] triangle(${names.joinToString()})
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
    }
}

fun parseGeoGenLine(line: String): String {
    val index = line.indexOf("=")
    val open = line.indexOf("(")
    check(index != -1 && open > index && line.last() == ')') { "Expected definition" }
    val name = line.substring(0, index).trim()
    val args = line.substring(open + 1, line.length - 1)
        .filterNot("{}"::contains).split(",").map(String::trim)
    return when (val funName = line.substring(index + 1, open).trim()) {
        "CircleWithCenterThroughPoint" -> "$name = circle(${args.joinToString()})"
        "CircleWithDiameter" -> "$name = diameter_circle(${args.joinToString()})"
        "Circumcenter" -> """
            [gray] c = circumcircle(${args.joinToString()})
            $name = center(c)
        """.trimIndent()
        "Circumcircle" -> "$name = circumcircle(${args.joinToString()})"
        "Excenter" -> """
            [gray] t = triangle(${args[2]}, ${args[0]}, ${args[1]})
            [gray] c = excircle(t)
            $name = center(c)
        """.trimIndent()
        "Excircle" -> """
            [gray] t = triangle(${args[2]}, ${args[0]}, ${args[1]})
            $name = excircle(t)
        """.trimIndent()
        "ExternalAngleBisector" -> "$name = exbisector(${args[2]}, ${args[0]}, ${args[1]})"
        "Incenter" -> """
            [gray] t = triangle(${args.joinToString()})
            [gray] c = incircle(t)
            $name = center(c)
        """.trimIndent()
        "Incircle" -> """
            [gray] t = triangle(${args.joinToString()})
            $name = incircle(t)
        """.trimIndent()
        "InternalAngleBisector" -> "$name = bisector(${args[2]}, ${args[0]}, ${args[1]})"
        "IntersectionOfLines" ->
            "$name = intersect(${args.joinToString()})"
        "IntersectionOfLineAndLineFromPoints" -> """
            [gray] l = line(${args[1]}, ${args[2]})
            $name = intersect(${args[0]}, l)
        """.trimIndent()
        "IntersectionOfLinesFromPoints" -> """
            [gray] l = line(${args[0]}, ${args[1]})
            [gray] m = line(${args[2]}, ${args[3]})
            $name = intersect(l, m)
        """.trimIndent()
        "IsoscelesTrapezoidPoint" -> "$name = symmetry(${args[0]}, midline(${args[1]}, ${args[2]}))"
        "LineFromPoints" -> "$name = line(${args.joinToString()})"
        "LineThroughCircumcenter" -> """
            O = circumcenter(${args.joinToString()})
            $name = line(${args[0]}, O)
        """.trimIndent()
        "Median" -> "$name = line(${args[0]}, midpoint(${args[1]}, ${args[2]}))"
        "Midline" -> "$name = line(midpoint(${args[0]}, ${args[1]}), midpoint(${args[0]}, ${args[2]}))"
        "Midpoint" -> "$name = midpoint(${args.joinToString()})"
        "MidpointOfArc" -> """
            [gray] c = circumcircle(${args.joinToString()})
            $name = cintersect(${args[0]}, exbisector(${args[2]}, ${args[0]}, ${args[1]}), c)
        """.trimIndent()
        "MidpointOfOppositeArc" -> """
            [gray] c = circumcircle(${args.joinToString()})
            $name = cintersect(${args[0]}, bisector(${args[2]}, ${args[0]}, ${args[1]}), c)
        """.trimIndent()
        "NinePointCircle" -> "$name = euler_circle(${args.joinToString()})"
        "OppositePointOnCircumcircle" -> """
            [gray] c = circumcircle(${args.joinToString()})
            $name = center(c) * 2 - ${args[0]}
        """.trimIndent()
        "Orthocenter" -> "$name = orthocenter(${args.joinToString()})"
        "ParallelLine" -> "$name = parallel(${args.joinToString()})"
        "ParallelLineToLineFromPoints" -> """
            [gray] l = line(${args[1]}, ${args[2]})
            $name = parallel(${args[0]}, l)
        """.trimIndent()
        "ParallelogramPoint" -> "$name = ${args[1]} + ${args[2]} - ${args[0]}"
        "PerpendicularBisector" -> "$name = midline(${args.joinToString()})"
        "PerpendicularLine" -> "$name = perpendicular(${args.joinToString()})"
        "PerpendicularLineToLineFromPoints" -> """
            [gray] l = line(${args[1]}, ${args[2]})
            $name = perpendicular(${args[0]}, l)
        """.trimIndent()
        "PerpendicularLineAtPointOfLine" -> "$name = perpendicular(${args[0]}, ${args.joinToString()})"
        "PerpendicularProjection" -> "$name = project(${args.joinToString()})"
        "PerpendicularProjectionOnLineFromPoints" -> """
            [gray] l = line(${args[1]}, ${args[2]})
            $name = project(${args[0]}, l)
        """.trimIndent()
        "PointReflection" -> "$name = ${args[1]} * 2 - ${args[0]}"
        "ReflectionInLine" -> "$name = symmetry(${args.joinToString()})"
        "ReflectionInLineFromPoints" -> """
            [gray] l = line(${args[1]}, ${args[2]})
            $name = symmetry(${args[0]}, l)
        """.trimIndent()
        "SecondIntersectionOfCircleAndLineFromPoints" -> """
            [gray] l = line(${args[0]}, ${args[1]})
            [gray] c = circumcircle(${args[0]}, ${args[2]}, ${args[3]})
            $name = cintersect(${args[0]}, l, c)
        """.trimIndent()
        "SecondIntersectionOfTwoCircumcircles" -> """
            [gray] c1 = circumcircle(${args[0]}, ${args[1]}, ${args[2]})
            [gray] c2 = circumcircle(${args[0]}, ${args[3]}, ${args[4]})
            $name = ccintersect(${args[0]}, c1, c2)
        """.trimIndent()
        "TangentLine" -> """
            [gray] c = circumcircle(${args.joinToString()})
            $name = tangent1(${args[0]}, c)
        """.trimIndent()
        else -> error("Unexpected $funName")
    }
}

fun parseGeoGen(text: String) = text.lines().let {
    parseGeoGenInitial(it[0]) + "\n" + it.drop(1).joinToString("\n", transform = ::parseGeoGenLine)
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun importGeoGen(callback: (String) -> Unit): () -> Unit {
    var geoGen by remember { mutableStateOf<String?>(null) }
    var error by remember(geoGen) { mutableStateOf<String?>(null) }
    val requester = remember { FocusRequester() }
    fun callback() {
        try {
            callback(parseGeoGen(geoGen!!))
            geoGen = null
        } catch (e: Exception) { error = e.toString() }
    }
    if (geoGen != null) Dialog({ geoGen = null },
        onPreviewKeyEvent = {
            if (it.type == KeyEventType.KeyUp && it.isCtrlPressed && it.key == Key.Enter) {
                callback()
                true
            } else false
        }) {
        if (geoGen != null) Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            Text("Import from GeoGen", style = Typography.h5)
            if (error != null) Text(error!!)
            TextField(geoGen!!, { geoGen = it }, Modifier
                    .fillMaxWidth(1f)
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .focusRequester(requester),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White)
            )
            TextButton(::callback, Modifier.align(Alignment.End)) { Text("OK") }
        }
    }
    LaunchedEffect(geoGen == null) {
        if (geoGen != null) requester.requestFocus()
    }
    return { geoGen = "" }
}
