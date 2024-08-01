

import korlibs.datastructure.*
import korlibs.event.*
import korlibs.io.file.std.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.korge3d.*
import korlibs.korge3d.format.*
import korlibs.korge3d.format.gltf2.*
import korlibs.korge3d.shape.*
import korlibs.math.geom.*
import korlibs.time.*
import java.awt.*


class CastleScene:Scene() {
    var degh = 0.degrees//鏡頭開始角度(水平)Z+看向Z-
    var degv = 30.degrees//鏡頭開始角度(垂直 俯角)0~180度 Y+看向Y-
    val dt = 30.milliseconds//按鍵偵測間隔
    val dt_animate_s = 0.1.seconds//動畫播放間隔
    var dt_animate = dt_animate_s
    val sp_s = 0.3f//跑速 (跑速/按鍵偵測間隔=m/s)
    //var msfix=false
    var sp = sp_s
    val jf = 20f//彈跳力
    val gravity = Vector3.DOWN * 30f//加速度
    var dis = 20f//鏡頭對人物距離

    lateinit var chrac:GLTF2View
    lateinit var scene3D:Stage3DView
    lateinit var camera3D:Camera3D
    fun cameraFix() {
        camera3D.orbitAround(chrac.position, dis, degh, degv)

    }
    fun moveModel(theta: Angle) {
        // 平移點
        val translatedX = camera3D.x - chrac.x
        val translatedZ = camera3D.z - chrac.z
        // 應用旋轉矩陣
        val rotatedX = translatedX * cosf(theta) - translatedZ * sinf(theta)
        val rotatedZ = translatedX * sinf(theta) + translatedZ * cosf(theta)
        // 平移回原位置
        chrac.lookAt(rotatedX + chrac.x, chrac.y, rotatedZ + chrac.z)
        chrac.updateAnimationDelta(dt_animate)
        chrac.z += sp * cosf(degh + 180.degrees - theta)
        chrac.x += sp * sinf(degh + 180.degrees - theta)
        cameraFix()
    }
    override suspend fun SContainer.sceneInit() {
        scene3D {

            val centerPointAxisLines = axisLines(length = 100f)
            centerPointAxisLines.position(0f,0f,0f)
            camera3D=camera.positionLookingAt(
                0f, 2f, 30f,
                0f, 0f, 0f)
            /* val library = resourcesVfs["human/chara2.dae"].readColladaLibrary()
            val model = library.geometryDefs.values.first()
            val view = mesh(model.mesh)*/
            val boxx = mesh(resourcesVfs["human/chara2.dae"].readColladaLibrary().geometryDefs.values.first().mesh)
                .position(0, 5, 0)
                .rigidBody(RigidBody3D(1f, true))
            chrac = gltf2View(resourcesVfs["Gest.glb"].readGLTF2(), autoAnimate = false)//載入人物移動動畫
                .position(0, 0, 10)
                .rigidBody(RigidBody3D(1f, true))
                .name("player")
            addChild(chrac)
            cameraFix()
           camera.speed=0.05f
        }

    }
    override suspend fun SContainer.sceneMain() {
        keys {
            up(Key.SHIFT) {
                sp = sp_s
            }
            justDown(Key.SHIFT) {
                sp += 0.2f
            }

            justDown(Key.SPACE) {
                chrac.rigidBody!!.velocity = Vector3.UP * jf
            }
            downFrame(listOf(Key.LEFT, Key.A), dt) {
                if (input.keys[Key.W] || input.keys[Key.UP])
                    moveModel((-45).degrees)
                else if (input.keys[Key.S] || input.keys[Key.DOWN])
                    moveModel((-135).degrees)
                else
                    moveModel((-90).degrees)
            }
            downFrame(listOf(Key.RIGHT, Key.D), dt) {
                if (input.keys[Key.W] || input.keys[Key.UP])
                    moveModel(45.degrees)
                else if (input.keys[Key.S] || input.keys[Key.DOWN])
                    moveModel(135.degrees)
                else
                    moveModel(90.degrees)
            }
            downFrame(listOf(Key.UP, Key.W), dt) {
                if (!(input.keys[Key.A] || input.keys[Key.LEFT] || input.keys[Key.D] || input.keys[Key.RIGHT]))
                    moveModel(0.degrees)
            }
            downFrame(listOf(Key.DOWN, Key.S), dt) {
                if (!(input.keys[Key.A] || input.keys[Key.LEFT] || input.keys[Key.D] || input.keys[Key.RIGHT]))
                    moveModel(180.degrees)
            }
        }
        onScroll {
            if (it.scrollDeltaYPixels > 0 && dis > 10) {//後滑
                dis -= 1
            } else if (it.scrollDeltaYPixels < 0 && dis < 30) {//前滑
                dis += 1
            }
            cameraFix()
        }
        onClick {
            it.doubleClick {
                awtCursor.lock = !awtCursor.lock
                if (awtCursor.lock) {
                    awtWindow?.cursor=invisibleCursor
                } else {
                    awtWindow?.cursor=Cursor.getDefaultCursor()
                }
            }
        }

        val rigidBody = chrac.rigidBody
        rigidBody?.acceleration = gravity
        var pp=Vector3F.ZERO
        addUpdater {
                time ->
            if (awtCursor.lock) {
                degh += ((awtCursor.dxy.x)*camera3D.speed).degrees
                degv += ((awtCursor.dxy.y)*camera3D.speed).degrees
                if (degv < 30.degrees) {
                    degv = 30.degrees
                }
                if (degv > 110.degrees) {
                    degv = 110.degrees
                }
                awtCursor.dxy=Vector2D.ZERO
            }
             pp = (chrac.position + rigidBody!!.velocity * time.seconds)
            if (pp.y < 0) {
                pp = Vector3(pp.x, 0f, pp.z)
                rigidBody.velocity = Vector3(rigidBody.velocity.x, 0f, rigidBody.velocity.z)
            } else {
                rigidBody.velocity += rigidBody.acceleration * time.seconds
            }
            chrac.position = pp
            cameraFix()
        }
      /*
        addUpdater {
            /*
            scene3D?.stage3D.foreachDescendant { maj ->
                val rigid = maj.rigidBody
                val collider = maj.collider
                if (rigid != null && collider != null && rigidBody.useGravity) {
                    rigid.acceleration = gravity
                    rigid.velocity += rigidBody.acceleration * dt.seconds
                    scene3D?.stage3D.foreachDescendant { other ->
                        if (other !== maj) {
                            val otherCollider = other.collider
                            if (otherCollider != null) {
                                val collision =
                                    Colliders.collide(collider, maj.transform, otherCollider, other.transform)
                                if (collision != null) {
                                    maj.position += rigid.velocity.normalized() * collision.separation

                                    rigid.velocity = rigid.velocity.reflected(collision.normal) * 0.8

                                }
                            }
                        }
                    }
                }
            }*/


        }*/
    }


}
