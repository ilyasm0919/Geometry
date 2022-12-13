package com.ibis.geometry.common

//class Point(private val point: Complex): Drawable() {
//    @OptIn(ExperimentalUnitApi::class, ExperimentalTextApi::class)
//    override fun Drawer.draw() = style.border.style?.let {
//        val offset = point.toOffset()
//        circle(offset, 1.8f, style.color, Fill)
//        style.name?.let { name ->
//            val str = buildAnnotatedString {
////                this.pushStyle(SpanStyle(style.color, it.width.toSp() * 10))
//                val matches = Regex("_([a-zA-Z0-9]|\\{[a-zA-Z0-9]*\\})").findAll(name).toList()
//                append(name.replace("_", ""))
//                matches.forEachIndexed { index, match ->
//                    addStyle(SpanStyle(baselineShift = BaselineShift.Subscript),
//                        match.range.first - index, match.range.last - index)
//                }
//            }
//            text(offset + Offset(3f, -6f), str)
//        }
//    } ?: Unit
//}
