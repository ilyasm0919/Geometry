package com.ibis.geometry.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditMode(
    inputFile: File,
    fullscreen: Boolean,
    mode: MutableState<Mode>,
    input: MutableState<TextFieldValue>,
    config: MutableState<TextFieldValue>,
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
    }
    TextField(input.value, {
        input.value = it
        inputFile.writeText(input.value.text)
    },
        Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .focusRequester(requester),
        textStyle = TextStyle(fontFamily = FontFamily.Monospace/*, fontSize = calculateRelativeFontSize()*/),
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White))
    LaunchedEffect(null) {
        requester.requestFocus()
    }
}

//@Composable
//fun calculateRelativeFontSize(): TextUnit {
//    val height =
//    val width =
//    ...
//}
