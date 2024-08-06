
import korlibs.korge.input.*
import korlibs.korge.view.*
import korlibs.korge3d.*
import korlibs.korge3d.format.gltf2.*
import korlibs.math.geom.*
import korlibs.render.*
import korlibs.time.*
import java.awt.*
import java.awt.event.*
import java.awt.image.*
import kotlin.properties.*
import kotlin.reflect.*
import kotlin.time.*

var GameWindow.pos: Vector2D by more(Vector2D.ZERO)//視窗位置
var GameWindow.posC:Vector2D by more(Vector2D.ZERO)//視窗中心位置
var GameWindow.keydt:Duration by more(30.milliseconds)//按鍵偵測間隔

var awtWindow:Frame?=null
lateinit var awtCursor:Cursor
var Cursor.lock:Boolean by more(false)
var Cursor.dxy:Vector2D by more(Vector2D.ZERO)
val rb = Robot()
val Cursor.invisibleCursor :Cursor by more( Toolkit.getDefaultToolkit().createCustomCursor(
    BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), java.awt.Point(0, 0), "invisibleCursor"))


var GLTF2View.animatedt: Duration by more(0.1.seconds)
var Camera3D.item: GLTF2View? by more(null)

var Camera3D.moveSpeed: Float by more(0.3f)
var Camera3D.rotateSpeed: Float by more(0.05f)

var Camera3D.distance: Float by camera3DChange(20f,Pair(10f,30f))
var Camera3D.azimuth: Angle by camera3DChange(0.degrees)//鏡頭角度(水平)Z+看向Z-
var Camera3D.elevation: Angle by camera3DChange(30.degrees,Pair(0.degrees,180.degrees))//鏡頭角度(垂直 俯角)0~180度 Y+看向Y-
var Camera3D.target:Vector3F by camera3DChange(Vector3F.ZERO)
var Camera3D.person: Int by personChange(2,Pair(1,4))

var Stage3D.gravity:Vector3 by more(Vector3.DOWN * 30f)



class more<T>(private var value: T,private val limit:Pair<T, T>?=null) : ReadWriteProperty<Any?, T> {
    private var max :Any?=null
    private var min :Any?=null
    init {
        if (value is Comparable<*>&&limit!=null){
            if(limit.first is Comparable<*>) min = limit.first as Comparable<Any>
            if(limit.second is Comparable<*>) max = limit.second as Comparable<Any>
        }
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
        if (value is Comparable<*>){
            val comparableValue = value as Comparable<Any>
            if(max !=null)
                if(comparableValue > max!!)
                    this.value = max as T
            if(min !=null)
                if(comparableValue < min!!)
                    this.value = min as T
        }
    }
}
class camera3DChange<T>(private var value: T,private val limit:Pair<T, T>?=null) : ReadWriteProperty<Camera3D, T> {
    private var max :Any?=null
    private var min :Any?=null
    init {
        if (value is Comparable<*>&&limit!=null){
            if(limit.first is Comparable<*>) min = limit.first as Comparable<Any>
            if(limit.second is Comparable<*>) max = limit.second as Comparable<Any>
        }
    }
    override fun getValue(thisRef: Camera3D, property: KProperty<*>): T = value
    override fun setValue(thisRef: Camera3D, property: KProperty<*>, value: T) {
        if (this.value!=value){
           this.value = value
           if (value is Comparable<*>) {
               val comparableValue = value as Comparable<Any>
               if (max != null)
                   if (comparableValue > max!!)
                      this.value = max as T
               if (min != null)
                    if (comparableValue < min!!)
                      this.value = min as T
           }

           thisRef.orbitAround( thisRef.target, thisRef.distance, thisRef.azimuth, thisRef.elevation)
        }
    }
}
class personChange<T:Comparable<T>>(private var value: T,private val pair: Pair<T,T>) : ReadWriteProperty<Camera3D, T> {
    private val max =pair.second
    private val min =pair.first
    override fun getValue(thisRef: Camera3D, property: KProperty<*>): T = value
    override fun setValue(thisRef: Camera3D, property: KProperty<*>, value: T) {
        if (this.value!=value){
            this.value = value
            if (value > max)
                this.value = max
            if (value < min)
                this.value = min
            when(thisRef.person){
                2->{
                    thisRef.target=thisRef.item?.position?:thisRef.target
                }
            }
            thisRef.orbitAround(thisRef.target, thisRef.distance, thisRef.azimuth, thisRef.elevation)
        }
    }
}


fun GameWindow.detectPos() {
    awtWindow=Frame.getFrames().find { it.isVisible }
    awtWindow?.addComponentListener(object : ComponentAdapter() {
        override fun componentMoved(e: ComponentEvent) {
           this@detectPos.pos=Vector2D(awtWindow!!.x,awtWindow!!.y)
           this@detectPos.posC=this@detectPos.pos+Vector2D(awtWindow!!.width/2,awtWindow!!.height/2)
        }
    })
    this@detectPos.pos=Vector2D(awtWindow!!.x,awtWindow!!.y)
    this@detectPos.posC=this@detectPos.pos+Vector2D(awtWindow!!.width/2,awtWindow!!.height/2)
    awtCursor=awtWindow!!.cursor
}
fun Stage.cursorLock(){
    onMove {
        if(awtCursor.lock){
            awtCursor.dxy+= Vector2D(MouseInfo.getPointerInfo().location.x,MouseInfo.getPointerInfo().location.y)-gameWindow.posC
            rb.mouseMove(gameWindow.posC.x.toInt(),gameWindow.posC.y.toInt())
        }
    }
}
fun Stage.setting(){
    this.gameWindow.detectPos()
    this.cursorLock()

}
class Move{
    companion object {
        val N: Angle = 0.degrees
        val S: Angle = 180.degrees
        val W: Angle = (-90).degrees
        val E: Angle = 90.degrees
        val NE: Angle = 45.degrees
        val NW: Angle = (-45).degrees
        val SE: Angle = 135.degrees
        val SW: Angle = (-135).degrees
    }
}

fun Camera3D.move(theta: Angle) {
    // 平移點
    val translatedX = this.x - this.target.x
    val translatedZ = this.z - this.target.z
    // 應用旋轉矩陣
    val rotatedX = translatedX * cosf(theta) - translatedZ * sinf(theta)
    val rotatedZ = translatedX * sinf(theta) + translatedZ * cosf(theta)
    // 平移回原位置
    val z:Float
    val x:Float
    when(this.person){
        1->{

        }
        2->{
            z= (this.item?.z?:this.target.z) + (this.item?.speed?:this.moveSpeed) * cosf(this.azimuth + 180.degrees - theta)
            x= (this.item?.x?:this.target.x) + (this.item?.speed?:this.moveSpeed) * sinf(this.azimuth + 180.degrees - theta)
            this.item?.lookAt(rotatedX + this.item!!.x, this.item!!.y, rotatedZ + this.item!!.z)
            this.item?.updateAnimationDelta(this.item!!.animatedt)
            this.target=Vector3F(x,this.target.y ,z)
            this.item?.position=this.target
        }
        3->{
            z= this.target.z + this.moveSpeed * cosf(this.azimuth + 180.degrees - theta)
            x= this.target.x + this.moveSpeed * sinf(this.azimuth + 180.degrees - theta)
            this.target=Vector3F(x,this.target.y ,z)
        }

    }

}


