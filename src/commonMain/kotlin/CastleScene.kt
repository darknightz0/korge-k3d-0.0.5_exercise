

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
import korlibs.render.*
import korlibs.time.*
import java.awt.*


class CastleScene:Scene() {

    val sp_s = 0.3f//跑速 (跑速/按鍵偵測間隔=m/s)
    var sp = sp_s
    val jf = 20f//彈跳力
    val gravity = Vector3.DOWN * 30f//加速度
    lateinit var chrac:GLTF2View
    lateinit var scene3D:Stage3DView
    lateinit var camera3D:Camera3D


    override suspend fun SContainer.sceneInit() {
        scene3D {

            camera.pitch
            val centerPointAxisLines = axisLines(length = 100f)
            centerPointAxisLines.position(0f,0f,0f)
            camera3D=camera.positionLookingAt(
                0f, 2f, 30f,
                0f, 0f, -3f)
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
           camera.moveSpeed=0.3f
            chrac.speed=0.3f
            camera.item=chrac

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
            justDown(Key.U) {
                println("az ${camera3D.azimuth}")
            }
            justDown(Key.R) {
                camera3D.eye+=Vector3F(0f,0f,0.1f)
                println("+z ${camera3D.eye.z}")
            }
            justDown(Key.F) {
                camera3D.eye+=Vector3F(0f,0f,-0.1f)
                println("-z ${camera3D.eye.z}")
            }
            justDown(Key.T) {
                camera3D.eye+=Vector3F(0f,0.1f,0f)
                println("+y ${camera3D.eye.y}")
            }
            justDown(Key.G) {
                camera3D.eye+=Vector3F(0f,-0.1f,0f)
                println("-y ${camera3D.eye.y}")
            }
            justDown(Key.Y) {
                camera3D.eye+=Vector3F(0.1f,0f,0f)
                println("+x ${camera3D.eye.x}")
            }
            justDown(Key.H) {
                camera3D.eye+=Vector3F(-0.1f,0f,0f)
                println("-x ${camera3D.eye.x}")
            }
            justDown(Key.N1) {
                camera3D.person=1
            }
            justDown(Key.N2) {
                camera3D.person=2
            }
            justDown(Key.N3) {
                camera3D.person=3
            }
            downFrame(listOf(Key.LEFT, Key.A), gameWindow.keydt) {
                if (input.keys[Key.W] || input.keys[Key.UP])
                    camera3D.move(Move.NW)
                else if (input.keys[Key.S] || input.keys[Key.DOWN])
                    camera3D.move(Move.SW)
                else
                    camera3D.move(Move.W)
            }
            downFrame(listOf(Key.RIGHT, Key.D), gameWindow.keydt) {
                if (input.keys[Key.W] || input.keys[Key.UP])
                    camera3D.move(Move.NE)
                else if (input.keys[Key.S] || input.keys[Key.DOWN])
                    camera3D.move(Move.SE)
                else
                    camera3D.move(Move.E)
            }
            downFrame(listOf(Key.UP, Key.W), gameWindow.keydt) {
                if (!(input.keys[Key.A] || input.keys[Key.LEFT] || input.keys[Key.D] || input.keys[Key.RIGHT]))
                    camera3D.move(Move.N)
            }
            downFrame(listOf(Key.DOWN, Key.S), gameWindow.keydt) {
                if (!(input.keys[Key.A] || input.keys[Key.LEFT] || input.keys[Key.D] || input.keys[Key.RIGHT]))
                    camera3D.move(Move.S)
            }
        }
        onScroll {
            if (it.scrollDeltaYPixels > 0 ) {//後滑
                camera3D.distance -= 1
            } else if (it.scrollDeltaYPixels < 0) {//前滑
                camera3D.distance += 1
            }
        }
        onClick {
            it.doubleClick {
                awtCursor.lock = !awtCursor.lock
                if (awtCursor.lock) {
                    awtWindow?.cursor=awtCursor.invisibleCursor
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
                   camera3D.azimuth += ((awtCursor.dxy.x)*camera3D.rotateSpeed).degrees
                   camera3D.elevation += ((awtCursor.dxy.y)*camera3D.rotateSpeed).degrees
                   awtCursor.dxy=Vector2D.ZERO
            }
             pp = (chrac.position + rigidBody!!.velocity * time.seconds)
            if (pp.y < 0) {
                pp = Vector3(pp.x, 0f, pp.z)
                rigidBody.velocity = Vector3(rigidBody.velocity.x, 0f, rigidBody.velocity.z)
            } else {
                rigidBody.velocity += rigidBody.acceleration * time.seconds
            }
            GameWindow.Cursor
            chrac.position = pp
           // cameraFix()
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
