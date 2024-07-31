
import korlibs.math.geom.*
import korlibs.render.*
import java.awt.*
import java.awt.event.*
import kotlin.properties.*
import kotlin.reflect.*

var GameWindow.pos: Vector2D by Pos(Vector2D.ZERO)
var GameWindow.posC:Vector2D by Pos(Vector2D.ZERO)
var (GameWindow.ICursor).pos:Vector2D by Pos(Vector2D.ZERO)
class Pos<T>(private var value: T) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

fun GameWindow.dectPos() {
    val awtWindow=Frame.getFrames().find { it.isVisible }
    awtWindow?.addComponentListener(object : ComponentAdapter() {
        override fun componentMoved(e: ComponentEvent) {
           this@dectPos.pos=Vector2D(awtWindow.x,awtWindow.y)
           this@dectPos.posC=this@dectPos.pos+Vector2D(awtWindow.width/2,awtWindow.height/2)
        }
    })
    /*awtWindow?.addMouseMotionListener(object : MouseAdapter() {
        override fun mouseMoved(e: MouseEvent) {
            this@dectPos.cursor.pos=Vector2D(MouseInfo.getPointerInfo().location.x,MouseInfo.getPointerInfo().location.y)
        }
    })*/

}

