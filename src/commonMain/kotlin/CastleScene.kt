import korlibs.datastructure.*
import korlibs.event.*
import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import korlibs.io.util.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.tween.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge3d.*
import korlibs.korge3d.format.*

import korlibs.korge3d.format.gltf2.*
import korlibs.korge3d.shape.*
import korlibs.math.geom.*
import korlibs.math.geom.cos
import korlibs.math.geom.sin
import korlibs.math.interpolation.*
import korlibs.time.*
import kotlin.math.*
import kotlin.time.*


class CastleScene:Scene() {
    override suspend fun SContainer.sceneInit() {
        var degh=0.degrees
        var degv=30.degrees
        val dt=10.milliseconds
        val dt_animate_s=0.1.seconds
        var  dt_animate=dt_animate_s
        var sp=0.1f
        var dis=20f
        scene3D {
            uiSlider(1f,0.1f,1f,0.1f){
                position(100,100)
                onChange{
                    sp=value.toFloat()
                    dt_animate=dt_animate_s*value
                }
            }

            val centerPointAxisLines = axisLines(length = 100f)
            centerPointAxisLines.position(0f,0f,0f)

            camera.positionLookingAt(
                0f, 2f, 30f,
                0f, 0f, 0f)
            /*
            val library = resourcesVfs["human/chara2.dae"].readColladaLibrary()
            val model = library.geometryDefs.values.first()
            val view = mesh(model.mesh)*/

            val view=gltf2View(resourcesVfs["Gest.glb"].readGLTF2(),autoAnimate = false).position(0, 0, 0)

            addChild(view)
            fun cameraFix(p:Point=Point2(0,0)){
            camera.orbitAround(view.position,dis,degh,degv)
            }
            fun moveModel(theta: Angle) {
                // 平移點
                val translatedX = camera.x-view.x
                val translatedZ = camera.z-view.z
                // 應用旋轉矩陣
                val rotatedX = translatedX * cosf(theta) - translatedZ * sinf(theta)
                val rotatedZ = translatedX * sinf(theta) + translatedZ * cosf(theta)
                // 平移回原位置
                view.lookAt(rotatedX+view.x,view.y,  rotatedZ+view.z)
                view.updateAnimationDelta(dt_animate)

                view.z+=sp*cosf(degh+180.degrees-theta)
                view.x+=sp*sinf(degh+180.degrees-theta)
                cameraFix()
            }


            keys {
                downFrame(listOf(Key.LEFT,Key.A), dt) {
                    if ( input.keys[Key.W]||input.keys[Key.UP])
                        moveModel((-45).degrees)
                    else if( input.keys[Key.S]||input.keys[Key.DOWN])
                        moveModel((-135).degrees)
                    else
                        moveModel((-90).degrees)
                    }
                downFrame(listOf(Key.RIGHT,Key.D), dt) {
                    if ( input.keys[Key.W]||input.keys[Key.UP])
                        moveModel(45.degrees)
                    else if( input.keys[Key.S]||input.keys[Key.DOWN])
                        moveModel(135.degrees)
                    else
                        moveModel(90.degrees)
                }
                downFrame(listOf(Key.UP,Key.W), dt) {
                    if(!(input.keys[Key.A]||input.keys[Key.LEFT]||input.keys[Key.D]||input.keys[Key.RIGHT]))
                       moveModel(0.degrees)
                }
                downFrame(listOf(Key.DOWN,Key.S), dt) {
                    if(!(input.keys[Key.A]||input.keys[Key.LEFT]||input.keys[Key.D]||input.keys[Key.RIGHT]))
                    moveModel(180.degrees)
                    }
                downFrame(Key.Q, dt) { degh-=1.degrees
                    cameraFix()}
                downFrame(Key.E, dt) { degh+=1.degrees
                    cameraFix()}
                downFrame(Key.U, dt) {
                    degv-=1.degrees
                    if (degv<30.degrees){ degv=30.degrees}
                    cameraFix()}
                downFrame(Key.L, dt) {
                    degv+=1.degrees
                    if (degv>110.degrees) {degv=110.degrees}
                    cameraFix()}
            }
            onScroll {
                if (it.scrollDeltaYPixels > 0&&dis>10) {//後滑
                    dis-=1
                } else if (it.scrollDeltaYPixels < 0&&dis<30) {//前滑
                    dis+=1
                }
                cameraFix()
            }


        }
    }

}
