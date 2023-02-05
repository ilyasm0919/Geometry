package com.ibis.geometry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.text.ExperimentalTextApi
import com.ibis.geometry.common.App
import com.ibis.geometry.common.theme.GeometryTheme
import java.io.File

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTextApi::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inputFile = File(getExternalFilesDir(null), "input.geo")
        val globalFile = File(getExternalFilesDir(null), "global.geo")
        inputFile.createNewFile()
        globalFile.createNewFile()
        setContent {
            GeometryTheme {
                App(inputFile, globalFile, MediaStore(contentResolver), TextDrawer, false)
            }
        }
    }
}
