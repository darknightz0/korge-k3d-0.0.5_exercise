
import korlibs.korge.input.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import korlibs.render.*
import java.awt.*
import java.awt.event.*
import java.awt.image.*
import kotlin.properties.*
import kotlin.reflect.*

var GameWindow.pos: Vector2D by more(Vector2D.ZERO)
var GameWindow.posC:Vector2D by more(Vector2D.ZERO)

var Cursor.lock:Boolean by more(false)
var Cursor.dxy:Vector2D by more(Vector2D.ZERO)
val rb = Robot()

var awtWindow:Frame?=null
lateinit var awtCursor:Cursor



val invisibleCursor = Toolkit.getDefaultToolkit().createCustomCursor(
    BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), java.awt.Point(0, 0), "invisibleCursor")
class more<T>(private var value: T) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
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



