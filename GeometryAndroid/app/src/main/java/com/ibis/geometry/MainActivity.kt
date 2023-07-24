package com.ibis.geometry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.ibis.geometry.common.App
import com.ibis.geometry.common.theme.GeometryTheme
import java.io.File

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTextApi::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inputFile = mutableStateOf(intent.data?.let { uri ->
            if (DocumentFile.fromSingleUri(this, uri)?.canWrite() == true) uri
            else File(getExternalFilesDir(null), "input.geo").also {
                it.outputStream().use { o ->
                    contentResolver.openInputStream(uri)?.use { i ->
                        i.copyTo(o)
                    }
                }
            }.toUri()
        } ?: File(getExternalFilesDir(null), "input.geo").also {
            it.createNewFile()
        }.toUri())
        val globalFile = File(getExternalFilesDir(null), "global.geo")
        globalFile.createNewFile()

        setContent {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("*/*"),
                onResult = {
                    it?.let { file ->
                        contentResolver.openOutputStream(file)!!.use { o ->
                            contentResolver.openInputStream(inputFile.value)!!.use { i ->
                                i.copyTo(o)
                            }
                        }
                        inputFile.value = file
                    }
                }
            )
            GeometryTheme {
                App(globalFile, FileManager(contentResolver, launcher, inputFile), TextDrawer, false)
            }
        }
    }
}
