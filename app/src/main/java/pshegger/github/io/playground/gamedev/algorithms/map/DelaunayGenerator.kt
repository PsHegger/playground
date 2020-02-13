package pshegger.github.io.playground.gamedev.algorithms.map

import pshegger.github.io.playground.gamedev.geometry.Edge
import pshegger.github.io.playground.gamedev.geometry.Triangle
import pshegger.github.io.playground.gamedev.geometry.Vector

class DelaunayGenerator(val points: List<Vector>) {
    val edges: List<Edge>
        get() = _triangles.flatMap { it.edges }.distinct()
    val canGenerateMore: Boolean
        get() = unprocessedPoints.isNotEmpty() || containsHelperTriangle()
    val triangles: List<Triangle>
        get() = _triangles

    private val unprocessedPoints = arrayListOf<Vector>().apply { addAll(points) }

    private val _triangles = arrayListOf<Triangle>()
    private val badEdges = mutableSetOf<Edge>()

    private var width: Int = 0
    private var height: Int = 0

    fun reset(width: Int, height: Int) {
        this.width = width
        this.height = height
        val m = 2.5f * Math.max(width, height)

        _triangles.clear()
        _triangles.add(Triangle(
            Vector(-100f, -100f),
            Vector(-100f, m),
            Vector(m, -100f)
        ))

        unprocessedPoints.clear()
        unprocessedPoints.addAll(points)
    }

    fun generateNextEdge() {
        while (badEdges.isNotEmpty()) {
            val edge = badEdges.first()
            badEdges.remove(edge)
            val ts = _triangles.filter { t -> t.edges.any { e -> e == edge } }

            if (ts.size == 2) {
                val t1 = ts[0]
                val t2 = ts[1]
                val op1 = t1.thirdPoint(edge.start, edge.end)!!
                val op2 = t2.thirdPoint(edge.start, edge.end)!!
                if (t2.circumscribedCircle.contains(op1) || t1.circumscribedCircle.contains(op2)) {
                    makeFlip(Pair(t1, t2))
                    return
                }
            }
        }

        if (unprocessedPoints.isEmpty()) {
            val finalTriangles = _triangles.filterNot { it.isHelper(width, height) }
            _triangles.clear()
            _triangles.addAll(finalTriangles)
            return
        }

        val p = unprocessedPoints.random()
        unprocessedPoints.remove(p)
        val container = _triangles.first { it.contains(p) }

        _triangles.remove(container)
        _triangles.add(Triangle(container.a, container.b, p))
        _triangles.add(Triangle(container.a, container.c, p))
        _triangles.add(Triangle(container.b, container.c, p))

        badEdges.addAll(container.edges)
    }

    fun generateAll() {
        while (canGenerateMore) {
            generateNextEdge()
        }
    }

    private fun makeFlip(p: Pair<Triangle, Triangle>) {
        val t1 = p.first
        val t2 = p.second
        _triangles.remove(t1)
        _triangles.remove(t2)

        val commonEdge = t1.commonEdge(t2)
        if (commonEdge != null) {
            val o1 = t1.thirdPoint(commonEdge.start, commonEdge.end)
            val o2 = t2.thirdPoint(commonEdge.start, commonEdge.end)
            if (o1 != null && o2 != null) {
                _triangles.add(Triangle(o1, commonEdge.start, o2))
                _triangles.add(Triangle(o2, commonEdge.end, o1))

                badEdges.add(Edge(o1, commonEdge.start))
                badEdges.add(Edge(o2, commonEdge.start))
                badEdges.add(Edge(o1, commonEdge.end))
                badEdges.add(Edge(o2, commonEdge.end))
            }
        }
    }

    private fun containsHelperTriangle() = _triangles.any { it.isHelper(width, height) }
    private fun Triangle.isHelper(width: Int, height: Int) = isPointHelper(a, width, height) || isPointHelper(b, width, height) || isPointHelper(c, width, height)
    private fun isPointHelper(v: Vector, width: Int, height: Int) = v.x < 0 || v.x > width || v.y < 0 || v.y > height
}
