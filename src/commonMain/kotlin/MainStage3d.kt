import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.time.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.tween.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import korlibs.korge3d.*
import korlibs.math.geom.*

import korlibs.render.*

class MainStage3d : Scene() {
    lateinit var contentSceneContainer: SceneContainer

    override suspend fun SContainer.sceneInit() {
        contentSceneContainer = sceneContainer(views).xy(0, 300)
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
        uiHorizontalStack {
            sceneButton<CratesScene>("Crates")
            sceneButton<PhysicsScene>("Physics")
            sceneButton<MonkeyScene>("Monkey")
            sceneButton<SkinningScene>("Skinning")
            sceneButton<CastleScene>("Castle")
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
        contentSceneContainer.changeToDisablingButtons<CratesScene>(this)
        //contentSceneContainer.changeToDisablingButtons<SkinningScene>(this)

    }

    inline fun <reified T : Scene> Container.sceneButton(title: String) {
        uiButton(title)
            .onClick { contentSceneContainer.changeToDisablingButtons<T>(this) }

        //this += Button(title) { contentSceneContainer.changeToDisablingButtons<T>(this) }
        //    .position(8 + x * 200, views.virtualHeight - 120)
    }


    
    private suspend fun Stage3D.orbit(v: View3D, distance: Float, time: TimeSpan) {
        viewParent.tween(time = time) { ratio ->
            val angle = 360.degrees * ratio
            camera.positionLookingAt(
                cosf(angle) * distance, 0f, sinf(angle) * distance, // Orbiting camera
                v.transform.mtranslation.x, v.transform.mtranslation.y, v.transform.mtranslation.z
            )
        }
    }

    /*
    class Button(text: String, handler: suspend () -> Unit) : Container() {
        val textField = Text(text, textSize = 32.0).apply { smoothing = false }
        private val bounds = textField.textBounds
        val g = CpuGraphics().updateShape {
            fill(Colors.DARKGREY, 0.7) {
                roundRect(bounds.x, bounds.y, bounds.width + 16, bounds.height + 16, 8.0, 8.0)
            }
        }
        var enabledButton = true
            set(value) {
                field = value
                updateState()
            }
        private var overButton = false
            set(value) {
                field = value
                updateState()
            }

        fun updateState() {
            when {
                !enabledButton -> alpha = 0.3
                overButton -> alpha = 1.0
                else -> alpha = 0.8
            }
        }

        init {
            //this += this.solidRect(bounds.width, bounds.height, Colors.TRANSPARENT_BLACK)
            this += g.apply {
                mouseEnabled = true
            }
            this += textField.position(8, 8)

            mouse {
                over { overButton = true }
                out { overButton = false }
            }
            onClick {
                if (enabledButton) handler()
            }
            updateState()
        }
    }
    */

    suspend inline fun <reified T : Scene> SceneContainer.changeToDisablingButtons(buttonContainer: Container) {
        for (child in buttonContainer.children.filterIsInstance<UIButton>()) {
            //println("DISABLE BUTTON: $child")
            child.enabled = false
        }
        try {
            changeTo<T>()
        } finally {
            for (child in buttonContainer.children.filterIsInstance<UIButton>()) {
                //println("ENABLE BUTTON: $child")
                child.enabled = true
            }
        }
    }





}
