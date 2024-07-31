
import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.io.file.std.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.ui.*
import korlibs.math.geom.*
import korlibs.render.*

lateinit var myFont: Font
var vw: Int=1536
var vh: Int=768
val cameraSpeed_s=0.05f
var cameraSpeed=cameraSpeed_s
var cameraSpeed_slider_value=1f
lateinit var cameraSpeed_slider:UISlider

suspend fun main() = Korge(
    backgroundColor = Colors["#3f3f3f"],
    quality = GameWindow.Quality.QUALITY,
    windowSize = Size2D(vw,vh),
    virtualSize =Size2D(vw,vh)
) {
    gameWindow.dectPos()
    myFont= TtfFont(resourcesVfs["txt_tp.ttf"].readAll())
    views.injector
        .mapPrototype { MainStage3d() }
        .mapPrototype { CastleScene() }

    sceneContainer().changeTo { MainStage3d() }
}


