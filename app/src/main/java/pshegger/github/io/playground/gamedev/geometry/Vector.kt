package pshegger.github.io.playground.gamedev.geometry

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector(val x: Float, val y: Float) {
    fun normalize() = length().let { length ->
        if (length == 0f) {
            this
        } else {
            this / length
        }
    }

    fun length() = sqrt(x * x + y * y.toDouble()).toFloat()

    fun reflect(n: Vector) = this - n * 2f * (this * n)
    fun rotateBy(angle: Float): Vector {
        val cosTheta = cos(angle)
        val sinTheta = sin(angle)
        return Vector(
            x * cosTheta - y * sinTheta,
            x * sinTheta + y * cosTheta
        )
    }

    fun distance(o: Vector) = (o - this).length()
    fun distanceSquared(o: Vector) = (o - this).let { v ->
        v.x * v.x + v.y * v.y
    }

    fun angleWith(o: Vector) = acos((this * o) / (length() * o.length()))

    operator fun plus(v: Vector) = Vector(x + v.x, y + v.y)
    operator fun minus(v: Vector): Vector = Vector(x - v.x, y - v.y)
    operator fun unaryMinus(): Vector = Vector(-x, -y)

    operator fun div(d: Float): Vector = Vector(x / d, y / d)
    operator fun times(r: Float): Vector = Vector(x * r, y * r)
    operator fun times(r: Int): Vector = times(r.toFloat())
    operator fun times(v: Vector): Float = x * v.x + y * v.y            // Dot product
    operator fun rem(v: Vector): Float = y * v.x - x * v.y              // Cross product

    fun shift(f: Float) = Vector(x + f, y + f)
    fun shift(n: Int) = shift(n.toFloat())

    companion object {
        val Zero = Vector(0f, 0f)
        val Up = Vector(0f, -1f)
        val Right = Vector(1f, 0f)
        val Down = Vector(0f, 1f)
        val Left = Vector(-1f, 0f)
    }
}
