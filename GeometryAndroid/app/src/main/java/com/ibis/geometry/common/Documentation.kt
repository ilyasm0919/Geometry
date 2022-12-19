package com.ibis.geometry.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import com.ibis.geometry.common.theme.Typography

val documentationText = listOf(
    "Style modifiers" to """
        hide
        dot
        dash
        dash_dot
        fill
        hide_label
    """,
    "Color modifiers" to """
        red
        green
        blue
        orange
        violet
        white
        gray
        black
    """,
    "Constructors" to """
        point(Point): Point
        line(Line): Line
        segment(Segment): Segment
        circle(Circle): Circle
        triangle(Triangle): Triangle
        polygon(Polygon): Polygon
        angle(Angle): Angle
    """,
    "Intersections" to """
        intersect(Line, Line): Point
        cintersect(Point, Line, Circle): Point
        cintersect1(Line, Circle): Point
        cintersect2(Line, Circle): Point
        ccintersect(Point, Circle, Circle): Point
        ccintersect1(Circle, Circle): Point
        ccintersect2(Circle, Circle): Point
    """,
    "Points" to """
        midpoint(Segment): Point
        divide(Complex, Segment): Point
        project(Point, Line): Point
        cproject(Point, Circle): Point
        centroid(Polygon): Point
        circumcenter(Triangle): Point
        orthocenter(Triangle): Point
        incenter(Triangle): Point
        excenter(Point, Point, Point): Point
        euler_center(Triangle): Point
        gergonne(Triangle): Point
        nagel(Triangle): Point
        isogonal(Point, Triangle): Point
        pole(Line, Circle): Line
    """,
    "Lines" to """
        midline(Segment): Line
        parallel(Point, Line): Line
        perpendicular(Point, Line): Line
        euler_line(Triangle): Line
        polar(Point, Circle): Line
    """,
    "Angles" to """
        clockwise(Angle): Angle
        counterclockwise(Angle): Angle
        bisector(Angle): Line
        exbisector(Angle): Line
    """,
    "Circles" to """
        radius(Circle): Real
        center(Circle): Point
        diameter_circle(Segment): Circle
        circumcircle(Triangle): Circle
        incircle(Triangle): Circle
        excircle(Point, Point, Point): Circle
        euler_circle(Triangle): Circle
        tangentPoint(Point, Point, Circle): Point
        tangentPoint1(Point, Circle): Point
        tangentPoint2(Point, Circle): Point
        tangent(Point, Point, Circle): Line
        tangent1(Point, Circle): Line
        tangent2(Point, Circle): Line
    """,
    "Transformations" to """
        symmetry(Object, Line): Object
        translation(Object, Complex): Object
        homothety(Object, Point, Complex): Object
        inversion(Object, Circle): Object
        midtriangle(Triangle): Triangle
    """,
    "Algebra" to """
        re(Complex): Real
        im(Complex): Real
        sqr(Complex): Complex
        sqrt(Complex): Complex
        exp(Complex): Complex
        ln(Complex): Complex
        abs(Complex): Real
        length(Segment): Real
        normalize(Complex): Complex
        dir(Segment): Complex
    """,
    "Special" to """
        time(): Natural
        choose(Object, Real): Point
        line_trace(Point): Line
        circle_trace(Point): Circle
        assert(Point, Point): Point
    """,
)

@Composable
fun documentation(): () -> Unit {
    var documentation by remember { mutableStateOf(false) }
    if (documentation) Dialog({ documentation = false }) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            Text("Documentation", style = Typography.h5)
            documentationText.forEach { (title, content) ->
                var expanded by remember { mutableStateOf(false) }
                Text(title, Modifier.clickable { expanded = !expanded }, style = Typography.h6)
                if (expanded) Text(content.trimIndent())
            }
        }
    }
    return { documentation = true }
}
