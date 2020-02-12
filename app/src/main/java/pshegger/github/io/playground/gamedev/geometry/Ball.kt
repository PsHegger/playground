package pshegger.github.io.playground.gamedev.geometry

import android.graphics.Canvas
import android.graphics.Paint

data class Ball(var c: Vector, var r: Float, val initialColor: Int, var v: Velocity) {
    val paint = Paint().apply {
        color = initialColor
    }
    var color: Int = initialColor
        set(value) {
            paint.color = value
            field = value
        }

    fun move(deltaTime: Long) {
        val deltaSec = deltaTime / 1000f
        val dx = deltaSec * v.x
        val dy = deltaSec * v.y

        c = Vector(c.x + dx, c.y + dy)
    }

    fun checkWallCollision(width: Int, height: Int) {
        if (c.x >= width - r) {
            c = c.copy(x = width - r)
            v = v.oppositeX()
        }

        if (c.x < r) {
            c = c.copy(x = r)
            v = v.oppositeX()
        }

        if (c.y >= height - r) {
            c = c.copy(y = height - r)
            v = v.oppositeY()
        }

        if (c.y < r) {
            c = c.copy(y = r)
            v = v.oppositeY()
        }
    }

    fun collidesWithBall(b: Ball) = (b != this) && ((b.c - c).length() <= (b.r + r))

    fun render(canvas: Canvas) {
        canvas.drawCircle(c.x, c.y, r, paint)
    }
}
