import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.io.file.std.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.math.geom.*

import korlibs.render.*

lateinit var myFont: Font
suspend fun main() = Korge(
    backgroundColor = Colors["#3f3f3f"],
    quality = GameWindow.Quality.QUALITY,
    windowSize = Size2D(1536,768),
    virtualSize =Size2D(1536,768)
) {
    myFont= TtfFont(resourcesVfs["txt_tp.ttf"].readAll())

    views.injector
        .mapPrototype { MainStage3d() }
        .mapPrototype { PhysicsScene() }
        .mapPrototype { CratesScene() }
        .mapPrototype { MonkeyScene() }
        .mapPrototype { SkinningScene() }
        .mapPrototype { CastleScene() }

    sceneContainer().changeTo{ MainStage3d() }

}

