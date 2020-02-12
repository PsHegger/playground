package pshegger.github.io.playground.gamedev.geometry

data class Line(val p: Vector, private val direction: Vector) {
    companion object {
        fun perpendicularBiselector(e: Edge) = Line(e.middle, e.direction)
    }

    fun intersection(o: Line): Vector {
        val a1 = direction.x
        val b1 = direction.y
        val c1 = a1 * p.x + b1 * p.y

        val a2 = o.direction.x
        val b2 = o.direction.y
        val c2 = a2 * o.p.x + b2 * o.p.y

        val cx = ((c1 * b2) - (c2 * b1)) / (a1 * b2 - a2 * b1)
        val cy = (c1 - a1 * cx) / b1

        return Vector(cx, cy)
    }
}
