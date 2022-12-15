package com.ibis.geometry.common

val templates = listOf(
    "Triangle" to """
        A = #(20+60i)
        B = #(60-40i)
        C = #(-60-40i)
        [orange] [fill] t = triangle(A, B, C)
    """,
    "Quadrilateral" to """
        A = #(20+60i)
        B = #(60-40i)
        C = #(-60-40i)
        D = #(-30+40i)
        [orange] [fill] polygon(A, B, C, D)
    """,
    "Cyclic quadrilateral" to """
        A = #(20+60i)
        B = #(60-40i)
        C = #(-60-40i)
        c = circumcircle(A, B, C)
        D = cproject(#(-45+45i), c)
        [orange] [fill] polygon(A, B, C, D)
    """,
    "Animated cyclic quadrilateral" to """
        A = #(20+60i)
        B = #(60-40i)
        C = #(-60-40i)
        c = circumcircle(A, B, C)
        D = choose(c, time()/1000)
        [orange] [fill] polygon(A, B, C, D)
    """
)
