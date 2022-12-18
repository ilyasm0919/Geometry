package com.ibis.geometry

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.OutputStream

@JvmInline
value class MediaStore(val resolver: ContentResolver) : com.ibis.geometry.common.MediaStore {
    override fun writeImage(image: ImageBitmap, stream: OutputStream) {
        image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 90, stream)
    }

    private fun save(ext: String, pend: Boolean): Pair<OutputStream, () -> Unit> {
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "geometry.$ext")
            if (pend && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values)!!
        values.clear()
        return resolver.openOutputStream(uri)!! to {
            if (pend && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
    }

    override fun saveImage(ext: String, content: (OutputStream) -> Unit) {
        val (stream, update) = save(ext, false)
        stream.use(content)
        update()
    }

    override fun initVideo(ext: String): OutputStream {
        val (stream, update) = save(ext, true)
        return object : OutputStream() {
            override fun write(value: Int) {
                stream.write(value)
            }

            override fun write(b: ByteArray?, off: Int, len: Int) {
                stream.write(b, off, len)
            }

            override fun write(b: ByteArray?) {
                stream.write(b)
            }

            override fun flush() {
                stream.flush()
            }

            override fun close() {
                stream.close()
                update()
            }
        }
    }
}
