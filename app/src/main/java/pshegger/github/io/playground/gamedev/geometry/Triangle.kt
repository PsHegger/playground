package pshegger.github.io.playground.gamedev.geometry

data class Triangle(val a: Vector, val b: Vector, val c: Vector) {
    val edges: List<Edge> = listOf(
        Edge(a, b),
        Edge(a, c),
        Edge(b, c)
    )

    val points: List<Vector> = listOf(a, b, c)

    val circumscribedCircleCenter: Vector by lazy {
        val l1 = Line.perpendicularBiselector(edges[0])
        val l2 = Line.perpendicularBiselector(edges[1])

        l1.intersection(l2)
    }

    val circumscribedCircle: Circle by lazy { Circle(circumscribedCircleCenter, circumscribedCircleCenter.distance(a)) }

    fun contains(p: Vector): Boolean {
        val d = ((b.y - c.y) * (a.x - c.x) + (c.x - b.x) * (a.y - c.y))
        val aa = ((b.y - c.y) * (p.x - c.x) + (c.x - b.x) * (p.y - c.y)) / d
        val bb = ((c.y - a.y) * (p.x - c.x) + (a.x - c.x) * (p.y - c.y)) / d
        val cc = 1 - aa - bb

        return aa in 0.0..1.0 && bb in 0.0..1.0 && cc in 0.0..1.0
    }

    fun commonEdge(o: Triangle): Edge? = edges.find { e1 -> o.edges.any { e2 -> e1 == e2 } }
    fun thirdPoint(p1: Vector, p2: Vector): Vector? = when (p1) {
        a -> if (p2 == b) c else if (p2 == c) b else null
        b -> if (p2 == a) c else if (p2 == c) a else null
        c -> if (p2 == a) b else if (p2 == b) a else null
        else -> null
    }

    fun shift(f: Float) = Triangle(
        Vector(a.x + f, a.y + f),
        Vector(b.x + f, b.y + f),
        Vector(c.x + f, c.y + f)
    )

    fun shift(n: Int) = shift(n.toFloat())
}
