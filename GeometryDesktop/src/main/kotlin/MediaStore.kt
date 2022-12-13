
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import org.jetbrains.skiko.toImage
import java.io.File
import java.io.OutputStream

object MediaStore : com.ibis.geometry.common.MediaStore {
    override fun writeImage(image: ImageBitmap, stream: OutputStream) {
        stream.write(image.toAwtImage().toImage().encodeToData()!!.bytes)
    }

    private fun save(ext: String): OutputStream {
        val downloads = File("${System.getProperty("user.home")}/Downloads")
        var file = File(downloads, "geometry.$ext")
        if (file.exists()) {
            var number = 1
            do {
                file = File(downloads, "geometry ($number).$ext")
                number++
            } while (file.exists())
        }
        return file.outputStream()
    }

    override fun saveImage(ext: String, content: (OutputStream) -> Unit) = save(ext).use(content)

    override fun initVideo(ext: String) = save(ext)
}
