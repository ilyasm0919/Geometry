package com.ibis.geometry

import android.os.Build
import android.text.Spannable
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.SubscriptSpan
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.core.text.buildSpannedString
import androidx.core.text.toSpannable
import com.ibis.geometry.common.TextDrawer
import kotlin.math.nextUp
import kotlin.math.roundToInt

object TextDrawer: TextDrawer {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun text(canvas: Canvas, pos: Offset, text: List<String>, color: Color) {
        val spanned = buildSpannedString {
            text.forEachIndexed { index, str ->
                append(if (index % 2 == 0) str
                else str.toSpannable().apply {
                    setSpan(SubscriptSpan(), 0, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                })
            }
        }

        val paint = TextPaint().apply {
            setColor(color.value.toLong())
            textSize = 8f
        }
        val layout = StaticLayout.Builder.obtain(spanned, 0, spanned.length, paint,
            paint.measureText(spanned, 0, spanned.length).nextUp().roundToInt()).build()

        canvas.translate(pos.x + 3f, pos.y - 6f)
        layout.draw(canvas.nativeCanvas)
        canvas.translate(-pos.x - 3f, -pos.y + 6f)
    }
}
