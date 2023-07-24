package com.ibis.geometry.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun MenuItem(icon: ImageVector, text: String) {
    Icon(icon, null)
    Spacer(Modifier.width(7.dp))
    Text(text)
}

@Composable
fun Menu(
    mode: MutableState<Mode>,
    content: @Composable (() -> Unit) -> Unit
) = Row {
    TabRow(mode.value.ordinal, Modifier.weight(1f), contentColor = Color.White) {
        Tab(mode.value == Mode.Edit, { mode.value = Mode.Edit }, text = {
            Text(Mode.Edit.toString())
        })
        Tab(mode.value == Mode.View, { mode.value = Mode.View }, text = {
            Text(Mode.View.toString())
        })
    }

    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton({ expanded = true }, Modifier.background(
            MaterialTheme.colors.primary)) {
            Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colors.onPrimary)
        }
        DropdownMenu(expanded, { expanded = false }) {
            content { expanded = false }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(globalFile: File, fileManager: FileManager, textDrawer: TextDrawer, fullscreen: Boolean) {
    val mode = remember { mutableStateOf(Mode.View) }
    val input = remember { mutableStateOf(TextFieldValue(fileManager.readFile())) }
    val globalText = remember { mutableStateOf(globalFile.readText()) }
    val global = remember(globalText.value) {
        try {
            parseGlobal(globalText.value)
        } catch (_: Exception) { mutableMapOf() }
    }
    val tapped = remember { mutableStateOf(Offset.Zero) }
    val transformation = remember { TransformationState() }
    val cursor = remember { mutableStateOf(false) }
    val play = remember { mutableStateOf(true) }
    val time = remember { mutableStateOf(0) }

    Column(Modifier.onPreviewKeyEvent {
        when {
            it.key == Key.Tab && it.type == KeyEventType.KeyDown -> {
                mode.value = when (mode.value) {
                    Mode.Edit -> Mode.View
                    Mode.View -> Mode.Edit
                }
                true
            }
            else -> false
        }
    }) {
        when (mode.value) {
            Mode.View ->
                try { global.parse(input.value.text) } catch (e: Exception) { Static(listOf(Drawable {
                    text(bounds.topLeft + Offset(10f, 10f), listOf(e.toString()), Color.Black)
                } to null)) }.let { drawable ->
                    ViewMode(
                        fileManager,
                        textDrawer,
                        input,
                        fullscreen,
                        mode,
                        drawable,
                        transformation,
                        cursor,
                        tapped,
                        play,
                        time,
                    )
                }
            Mode.Edit -> EditMode(
                fileManager,
                globalFile,
                fullscreen,
                mode,
                input,
                globalText
            )
        }
    }
}

enum class Mode {
    Edit, View
}
