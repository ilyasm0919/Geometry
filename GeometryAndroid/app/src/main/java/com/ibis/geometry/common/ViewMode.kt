package com.ibis.geometry.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import java.io.OutputStreamWriter
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.ViewMode(
    mediaStore: MediaStore,
    textDrawer: TextDrawer,
    input: MutableState<TextFieldValue>,
    fullscreen: Boolean,
    mode: MutableState<Mode>,
    drawable: Reactive<List<Pair<Drawable, Movable?>>>,
    cursor: MutableState<Boolean>,
    tapped: MutableState<Offset>,
    play: MutableState<Boolean>,
    time: MutableState<Int>,
) {
    val requester = remember { FocusRequester() }
    var screenshot by remember { mutableStateOf<Screenshot>(Screenshot.No) }
    var size by remember { mutableStateOf(IntSize(100, 100)) }
    var movable by remember { mutableStateOf(listOf<Movable>()) }
    val currentDrawable = remember(drawable, time.value) {
        try {
            drawable(ReactiveInput(time.value)).unzip().let {
                movable = it.second.filterNotNull()
                it.first
            }
        } catch(e: Exception) { listOf(Drawable {
            text(-size.toSize().center + Offset(10f, 10f), listOf(e.toString()), Color.Black)
        }) }
    }
    var chosen by remember(mode.value, fullscreen) { mutableStateOf<Int?>(null) }
    val bmp = remember(size, currentDrawable, cursor.value, tapped.value) {
        ImageBitmap(size.width, size.height).also { bmp ->
            Canvas(bmp).apply {
                var fSize = size.toSize()
                translate(fSize.width / 2, fSize.height / 2)
                scale(fSize.minDimension / 200)
                fSize *= 200f / fSize.minDimension
                try {
                    CanvasDrawer(textDrawer, fSize, this).let {
                        currentDrawable.forEach(it::draw)
                    }
                } catch (_: Exception) {}
                if (cursor.value) {
                    val paint = Paint().apply {
                        color = Color.Black
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(1f, 1f),
                            0f
                        )
                    }
                    drawLine(
                        Offset(tapped.value.x, -fSize.height / 2),
                        Offset(tapped.value.x, fSize.height / 2),
                        paint
                    )
                    drawLine(
                        Offset(-fSize.width / 2, tapped.value.y),
                        Offset(fSize.width / 2, tapped.value.y),
                        paint
                    )
                } else if (chosen != null) {
                    drawCircle(movable[chosen!!].pos.toOffset(), 3f, Paint().apply {
                        color = Color.Red
                        style = PaintingStyle.Stroke
                        strokeWidth = 1f
                    })
                }
            }
        }
    }

    fun<T> startVideo(ext: String, drawer: (Size, OutputStreamWriter) -> T, video: (T, Number, Int) -> Screenshot.Record) {
        screenshot = video(drawer(size.toSize(), mediaStore.init(ext).writer()), 18.451, 0)
    }

    if (!fullscreen) Menu(mode) { hide ->
        DropdownMenuItem({
            cursor.value = !cursor.value
            hide()
        }) {
            if (cursor.value) MenuItem(Icons.Default.LocationDisabled, "Hide cursor")
            else MenuItem(Icons.Default.LocationSearching, "Show cursor")
        }
        if (drawable !is Static) DropdownMenuItem({
            play.value = !play.value
            hide()
        }) {
            if (play.value) MenuItem(Icons.Default.Pause, "Pause")
            else MenuItem(Icons.Default.PlayArrow, "Play")
        }
        if (screenshot == Screenshot.No) {
            DropdownMenuItem({
                screenshot = Screenshot.Png
                hide()
            }) {
                MenuItem(Icons.Default.Image, "Screenshot (png)")
            }
            DropdownMenuItem({
                screenshot = Screenshot.Svg
                hide()
            }) {
                MenuItem(Icons.Default.PhotoSizeSelectLarge, "Screenshot (svg)")
            }
            DropdownMenuItem({
                screenshot = Screenshot.Html
                hide()
            }) {
                MenuItem(Icons.Default.Html, "Screenshot (html)")
            }
            if (drawable !is Static) {
                DropdownMenuItem({
                    startVideo("svg", ::SvgDrawer, ::SvgVideo)
                    hide()
                }) {
                    MenuItem(Icons.Default.VideoFile, "Start recording (svg)")
                }
                DropdownMenuItem({
                    startVideo("html", ::HtmlDrawer, ::HtmlVideo)
                    hide()
                }) {
                    MenuItem(Icons.Default.Javascript, "Start recording (html)")
                }
            }
        }
        if (screenshot is Screenshot.Record) DropdownMenuItem({
            (screenshot as Screenshot.Record).finish()
            screenshot = Screenshot.No
            hide()
        }) {
            MenuItem(Icons.Default.VideoFile, "Stop recording")
        }
    }

    LaunchedEffect(play.value, time.value, screenshot) {
        if (drawable !is Static && play.value) time.value++
        when (screenshot) {
            Screenshot.No -> {}
            Screenshot.Png -> mediaStore.saveImage("png", bmp)
            Screenshot.Svg -> mediaStore.saveImage("svg") {
                val drawer = SvgDrawer(size.toSize(), it.writer())
                currentDrawable.forEach(drawer::draw)
                drawer.finish()
            }
            Screenshot.Html -> mediaStore.saveImage("html") {
                val drawer = HtmlDrawer(size.toSize(), it.writer())
                currentDrawable.forEach(drawer::draw)
                drawer.finish()
            }
            is Screenshot.Record -> (screenshot as Screenshot.Record).frame {
                currentDrawable.forEach(::draw)
            }
        }
        if (screenshot !is Screenshot.Record) screenshot = Screenshot.No
    }
    val cursorHandler = Modifier.pointerInput(cursor.value, mode.value, size, fullscreen) {
        val scale = 200 / size.toSize().minDimension
        detectDragGestures({ offset ->
            val value = (offset - size.toSize().center) * scale
            tapped.value = value
            if (!cursor.value) chosen = movable.indexOfFirst {
                (it.pos.toOffset() - value).getDistanceSquared() < 40f
            }.takeIf { it != -1 }
        }, { chosen = null }) { change, delta ->
            change.consume()
            tapped.value += delta * scale
            if (!cursor.value && chosen != null) {
                val c = movable[chosen!!]
                input.value = TextFieldValue(
                    input.value.text.replaceFirst(
                        c.source,
                        "#(${Complex(tapped.value.x, -tapped.value.y)})"
                    )
                )
            }
        }
    }
    Image(bmp, null,
        cursorHandler
            .weight(1f)
            .fillMaxSize()
            .onSizeChanged { size = it }
            .focusRequester(requester)
            .focusable()
            .onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyDown && cursor.value) {
                    val step = if (it.isCtrlPressed) 0.1f else 1f
                    when (it.key) {
                        Key.DirectionLeft -> tapped.value -= Offset(step, 0f)
                        Key.DirectionUp -> tapped.value -= Offset(0f, step)
                        Key.DirectionRight -> tapped.value += Offset(step, 0f)
                        Key.DirectionDown -> tapped.value += Offset(0f, step)
                    }
                }
                if (it.type != KeyEventType.KeyUp) false
                else when {
                    it.key == Key.Spacebar -> {
                        play.value = !play.value
                        true
                    }
                    it.isCtrlPressed && it.key == Key.S && screenshot == Screenshot.No -> {
                        screenshot = when {
                            it.isShiftPressed -> Screenshot.Svg
                            it.isAltPressed -> Screenshot.Html
                            else -> Screenshot.Png
                        }
                        true
                    }
                    it.isCtrlPressed && it.key == Key.R && screenshot == Screenshot.No -> {
                        if (it.isAltPressed) startVideo("html", ::HtmlDrawer, ::HtmlVideo)
                        else startVideo("svg", ::SvgDrawer, ::SvgVideo)
                        true
                    }
                    it.isCtrlPressed && it.key == Key.R && screenshot is Screenshot.Record -> {
                        (screenshot as Screenshot.Record).finish()
                        screenshot = Screenshot.No
                        true
                    }
                    it.key == Key.C -> {
                        cursor.value = !cursor.value
                        true
                    }
                    else -> false
                }
            })
    if (cursor.value) Text("%.1f%s%.1fi".format(
        Locale.ENGLISH,
        tapped.value.x, if (tapped.value.y >= 0) "" else "+", -tapped.value.y))
    LaunchedEffect(mode.value) {
        requester.requestFocus()
    }
}
