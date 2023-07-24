package com.ibis.geometry.common

import androidx.compose.ui.graphics.ImageBitmap
import java.io.OutputStream

interface FileManager {
    fun writeFile(content: String)
    fun readFile(): String
    fun writeImage(image: ImageBitmap, stream: OutputStream)
    fun init(ext: String): OutputStream
    fun saveFile()
}

fun FileManager.saveImage(ext: String, content: (OutputStream) -> Unit) = init(ext).use(content)
fun FileManager.saveImage(ext: String, image: ImageBitmap) = saveImage(ext) {
    writeImage(image, it)
}
