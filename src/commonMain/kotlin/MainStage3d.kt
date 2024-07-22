

import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.tween.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import korlibs.math.geom.*
import korlibs.time.*

class MainStage3d : Scene() {
    lateinit var sc: SceneContainer

    override suspend fun SContainer.sceneInit() {
        sc = sceneContainer(views).xy(0, 300)
        val map_c = resourcesVfs["icon/map_icon2.png"].readBitmap().resized(100, 100, ScaleMode.COVER, Anchor.CENTER)
        fun  openMap()=container{
            val bg=solidRect(Size2D(views.virtualWidth,views.virtualHeight)){
                alpha=0.5
                onClick { }
            }
            uiButton ("close"){
                centerOn(bg)
                onClick { this@container.removeFromParent() }
            }
            position(0,0)

        }


        val map_icon=image(map_c){
            position(50,views.virtualHeight-50)
            anchor(0.5,0.5)
            onDown {
                tween(this::scale[0.5], time = 0.2.seconds)
                openMap()
                tween(this::scale[1.0], time = 0.2.seconds)
            }

        }
        val map_txt = container{
            val bg=roundRect(Size2D(60,20),RectCorners(5))
            text("地圖", font = myFont, color = Colors.BLACK) {
                centerOn(bg)
            }
            centerXOn(map_icon)
            alignBottomToTopOf(map_icon,30)
            visible = false
        }

        map_icon.onOver { map_txt.visible=true }
        map_icon.onOut { map_txt.visible = false }

        uiHorizontalStack {


        }
        sc.changeTo{CastleScene()}
    }




}
