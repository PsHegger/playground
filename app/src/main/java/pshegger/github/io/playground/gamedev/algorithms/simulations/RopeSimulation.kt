package pshegger.github.io.playground.gamedev.algorithms.simulations

import pshegger.github.io.playground.gamedev.geometry.Vector

class RopeSimulation(private val maxStickLength: Float = 100f) {

    var rope: Rope = Rope()
        private set

    fun reset() {
        rope = Rope()
    }

    fun addPoint(position: Vector) {
        rope.points.add(Point(position, position, false))
    }

    fun addStick(pointA: Point, pointB: Point) {
        rope.sticks.add(
            Stick(
                pointA, pointB,
                (pointB.position - pointA.position).length()
            )
        )
    }

    fun subdivideRope() {
        while (rope.sticks.any { it.length > maxStickLength }) {
            val stick = rope.sticks.first { it.length > maxStickLength }
            val stickCenter = (stick.pointA.position + stick.pointB.position) / 2f
            addPoint(stickCenter)
            rope.sticks.remove(stick)
            addStick(
                stick.pointA,
                rope.points.last(),
            )
            addStick(
                rope.points.last(),
                stick.pointB,
            )
        }
    }

    fun update(deltaTime: Long, gravity: Vector?) {
        val gravity = gravity ?: (Vector.Down * 9.81f)
        val deltaTimeSec = deltaTime / 1000f
        rope.points.forEach { p ->
            if (!p.locked) {
                val posBeforeUpdate = p.position
                p.position += p.position - p.prevPosition
                p.position += gravity * 1000f * deltaTimeSec * deltaTimeSec
                p.prevPosition = posBeforeUpdate
            }
        }

        repeat(10) {
            rope.sticks.forEach { stick ->
                val stickCenter = (stick.pointA.position + stick.pointB.position) / 2f
                val stickDir = (stick.pointA.position - stick.pointB.position).normalize()
                if (!stick.pointA.locked) {
                    stick.pointA.position = stickCenter + stickDir * stick.length / 2f
                }
                if (!stick.pointB.locked) {
                    stick.pointB.position = stickCenter- stickDir * stick.length / 2f
                }
            }
        }
    }

    data class Rope(
        val points: MutableList<Point> = mutableListOf(),
        val sticks: MutableList<Stick> = mutableListOf(),
    )

    data class Point(var position: Vector, var prevPosition: Vector, var locked: Boolean)

    data class Stick(val pointA: Point, val pointB: Point, val length: Float)
}
