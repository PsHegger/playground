package pshegger.github.io.playground.gamedev.geometry

data class Edge(val start: Vector, val end: Vector) {
    val middle: Vector by lazy { start + ((end - start) / 2f) }
    val direction: Vector by lazy { Vector(end.x - start.x, end.y - start.y) }
    val normal: Vector by lazy { Vector(-direction.y, direction.x) }

    override fun equals(other: Any?): Boolean {
        val o = other as? Edge ?: return false

        return (start == o.start && end == o.end) || (start == o.end && end == o.start)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
