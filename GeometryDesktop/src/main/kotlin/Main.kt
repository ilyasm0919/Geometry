
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ibis.geometry.common.App
import com.ibis.geometry.common.theme.GeometryTheme
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val inputFile = File("~/code/kt/GeometryDesktop/input.geo")
    inputFile.parentFile.mkdirs()
    inputFile.createNewFile()

    val configFile = File("~/code/kt/GeometryDesktop/config.geo") // just if exists

    val state = rememberWindowState(size = DpSize.Unspecified)
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
            App(inputFile, configFile, MediaStore, TextDrawer, state.placement == WindowPlacement.Fullscreen)
        }
    }
}
