package com.ibis.geometry.common

import androidx.compose.ui.graphics.ImageBitmap
import java.io.OutputStream

interface MediaStore {
    fun writeImage(image: ImageBitmap, stream: OutputStream)
    fun saveImage(ext: String, content: (OutputStream) -> Unit)
    fun saveImage(ext: String, image: ImageBitmap) = saveImage(ext) {
        writeImage(image, it)
    }
    fun initVideo(ext: String): OutputStream
}
