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
            listOf("No title" to functions).forEach { (title, functions) ->
                var expanded by remember { mutableStateOf(false) }
                Text(title, Modifier.clickable { expanded = !expanded }, style = Typography.h6)
                if (expanded) Text(functions.joinToString("\n"))
            }
        }
    }
    return { documentation = true }
}
