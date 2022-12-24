package com.ibis.geometry.common

import androidx.compose.ui.graphics.ImageBitmap
import java.io.OutputStream

interface MediaStore {
    fun writeImage(image: ImageBitmap, stream: OutputStream)
    fun init(ext: String): OutputStream
}

fun MediaStore.saveImage(ext: String, content: (OutputStream) -> Unit) = init(ext).use(content)
fun MediaStore.saveImage(ext: String, image: ImageBitmap) = saveImage(ext) {
    writeImage(image, it)
}
