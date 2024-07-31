

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
        val setting_c = resourcesVfs["icon/map_icon2.png"].readBitmap().resized(50, 50, ScaleMode.COVER, Anchor.CENTER)
        fun  openMap()=container{
            val bg=solidRect(Size2D(vw,vh)){
                alpha=0.5
                onClick { }
            }
            uiButton ("close"){
                centerOn(bg)
                onClick { this@container.removeFromParent() }
            }
            position(0,0)

        }
        val  openSetting=container {
            uiScrollable(Size2D(200,200)){
                uiButton("window move") {
                        onClick {
                            gameWindow.javaClass.getMethod("setBounds", Int::class.java, Int::class.java, Int::class.java, Int::class.java)
                                .invoke(gameWindow, 500, 100, gameWindow.width, gameWindow.height)
                        }
                    }
                uiButton("close") {
                    onClick { this@container.visible=false
                    }
                    position(0,300)
                }
            }
            position(0, 0)
            visible=false
        }
       fun Container.imageWithText(bitmap: Bitmap,pos:Vector2D,txt:String,f:()->Unit):Image{
        val img=image(bitmap){
            position(pos)
            anchor(0.5,0.5)
            onDown {
                tween(this::scale[0.9], time = 0.2.seconds)
                f()
                tween(this::scale[1.0], time = 0.2.seconds)
            }
            onOver { tween(this::scale[1.1], time = 0.2.seconds) }
            onOut { tween(this::scale[1.0], time = 0.2.seconds) }

        }
        fun img_txt() = container{
            val bg_w=txt.length*18+5
            val bg=roundRect(Size2D(bg_w,20),RectCorners(5)){
                positionX(img.x-img.width/4)
                if(img.y>vh/2)
                    positionY(img.y-img.height/2)
                else
                    positionY(img.y+img.height/2)
            }
            text(txt, font = myFont, color = Colors.BLACK) {
                centerOn(bg)
            }
        }
            var txt_container:Container?=null
           img.onOver { txt_container=img_txt() }
           img.onOut { txt_container?.removeFromParent() }
           return img
      }
        imageWithText(map_c, Vector2D(50,vh-50),"地圖\n地圖"){openMap()}
        imageWithText(setting_c, Vector2D(vw-50,50),"設定"){openSetting.visible=true}
        sc.changeTo{CastleScene()}
    }




}
