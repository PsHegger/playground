package pshegger.github.io.playground.gamedev.algorithms

import pshegger.github.io.playground.gamedev.geometry.Edge
import pshegger.github.io.playground.gamedev.geometry.Polygon
import pshegger.github.io.playground.gamedev.geometry.Triangle
import pshegger.github.io.playground.gamedev.geometry.Vector
import pshegger.github.io.playground.gamedev.utils.others

class Voronoi(private val triangles: List<Triangle>) {
    val canGenerateMore: Boolean
        get() = processingQueue.isNotEmpty()
    val polygons: List<Polygon>
        get() = _polygons.values.toList()
    val edges: List<Edge>
        get() = polygons.flatMap { it.edges }.distinct()
    val points: List<PointState>
        get() = _points.map { PointState(it, processingQueue.contains(it)) }

    private val processedPoints = mutableListOf<Vector>()
    private val processingQueue = mutableListOf<Vector>()
    private val _polygons = mutableMapOf<Vector, Polygon>()
    private val _points = triangles.flatMap { listOf(it.a, it.b, it.c) }.distinct()
    private val triangleLookup = _points.associateBy({it}) { triangles.filter { t -> t.a == it || t.b == it || t.c == it } }

    fun reset() {
        processedPoints.clear()
        processingQueue.clear()
        processingQueue.add(_points.random())
        _polygons.clear()
    }

    fun generateNextEdge() {
        val p = processingQueue[0]
        val polygon = _polygons[p] ?: let {
            val poly = Polygon(p)
            _polygons[p] = poly
            poly
        }
        val pTriangles = triangleLookup[p]!!
        val neighborPairs = pTriangles.flatMap { t1 -> pTriangles.others(t1).filter { t2 -> t1.commonEdge(t2) != null }.map { t2 -> Pair(t1, t2) } }
        val unconnectedPair = neighborPairs.firstOrNull { p -> polygon.edges.none { e -> e == Edge(p.first.circumscribedCircleCenter, p.second.circumscribedCircleCenter) } }

        if (unconnectedPair == null) {
            processingQueue.remove(p)
            processedPoints.add(p)
            return
        }

        val edge = Edge(unconnectedPair.first.circumscribedCircleCenter, unconnectedPair.second.circumscribedCircleCenter)
        polygon.edges.add(edge)
        (unconnectedPair.first.points + unconnectedPair.second.points).distinct().forEach { point ->
            if (!processingQueue.contains(point) && !processedPoints.contains(point)) {
                processingQueue.add(point)
            }
        }
    }

    fun generateAll() {
        while (canGenerateMore) {
            generateNextEdge()
        }
    }

    data class PointState(val p: Vector, val isActive: Boolean)
}
