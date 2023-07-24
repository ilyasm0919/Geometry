
import androidx.compose.runtime.MutableState
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import org.jetbrains.skiko.toImage
import java.awt.FileDialog
import java.io.File
import java.io.OutputStream

class FileManager(private val window: ComposeWindow, private val inputFile: MutableState<File>) : com.ibis.geometry.common.FileManager {
    override fun writeImage(image: ImageBitmap, stream: OutputStream) {
        stream.write(image.toAwtImage().toImage().encodeToData()!!.bytes)
    }

    override fun init(ext: String): OutputStream {
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

    override fun saveFile() = FileDialog(window, "Save as", FileDialog.SAVE).apply {
        setFilenameFilter { _, name -> name.endsWith(".geo") }
        isVisible = true
    }.let {
        if (it.file != null) File(it.directory, it.file) else null
    }?.let {
        it.bufferedWriter().use { o ->
            inputFile.value.bufferedReader().use { i ->
                i.copyTo(o)
            }
        }
        inputFile.value = it
    } ?: Unit

    override fun writeFile(content: String) = inputFile.value.writeText(content)

    override fun readFile() = inputFile.value.readText()
}
