
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ibis.geometry.common.App
import com.ibis.geometry.common.theme.GeometryTheme
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val inputFile = File("/${System.getenv("LocalAppData")}/GeometryDesktop/input.geo")
    inputFile.parentFile.mkdirs()
    inputFile.createNewFile()

    val state = rememberWindowState()
    Window(
        onCloseRequest = ::exitApplication,
        state = state,
        title = "Geometry Desktop",
        icon = painterResource("icon.png"),
        onPreviewKeyEvent = {
            if (it.key == Key.F11 && it.type == KeyEventType.KeyUp) {
                if (state.placement == WindowPlacement.Fullscreen)
                    state.placement = WindowPlacement.Floating
                else state.placement = WindowPlacement.Fullscreen
                true
            } else false
        }
    ) {
        GeometryTheme {
            App(inputFile, MediaStore, TextDrawer, state.placement == WindowPlacement.Fullscreen)
        }
    }
}
