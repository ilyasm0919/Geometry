package com.ibis.geometry.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.io.File
import java.awt.Toolkit
import kotlin.math.round

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditMode(
    inputFile: File,
    globalFile: File,
    fullscreen: Boolean,
    mode: MutableState<Mode>,
    input: MutableState<TextFieldValue>,
    globalText: MutableState<String>,
) {
    val requester = remember { FocusRequester() }
    val showDocumentation = documentation()
    if (!fullscreen) Menu(mode) { hide ->
        DropdownMenuItem({
            input.value = TextFieldValue()
            hide()
        }) {
            MenuItem(Icons.Default.Clear, "Clear")
        }
        DropdownMenuItem({
            showDocumentation()
            hide()
        }) {
            MenuItem(Icons.Default.MenuBook, "Documentation")
        }
        templates.forEach {
            DropdownMenuItem({
                input.value = TextFieldValue(it.second.trimIndent())
                hide()
            }) {
                Text(it.first)
            }
        }
        DropdownMenuItem({
            input.value = TextFieldValue(globalText.value)
            hide()
        }) {
            MenuItem(Icons.Default.Download, "Load global file")
        }
        DropdownMenuItem({
            globalText.value = input.value.text
            globalFile.writeText(globalText.value)
            hide()
        }) {
            MenuItem(Icons.Default.Upload, "Save to global file")
        }
    }
    TextField(input.value, {
        input.value = it
        inputFile.writeText(input.value.text)
    },
        Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .focusRequester(requester),
        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = calculateFontSize()),
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White))
    LaunchedEffect(null) {
        requester.requestFocus()
    }
}

fun calculateFontSize(): TextUnit {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    return (round(screenSize.getHeight()/1920)*25).sp
}