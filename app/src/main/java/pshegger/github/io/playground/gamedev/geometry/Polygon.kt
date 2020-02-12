package pshegger.github.io.playground.gamedev.geometry

data class Polygon(val p: Vector, val edges: MutableList<Edge> = mutableListOf()) {
    val vectorRoute: List<Vector>? by lazy {
        val points = edges.flatMap { listOf(it.start, it.end) }.distinct()
        val singleEndPoint = points.find { p -> edges.count { e -> e.start == p || e.end == p } == 1 }
        if (singleEndPoint != null || edges.isEmpty()) {
            return@lazy null
        }

        val startingEdge = edges[0]
        val startingPoint = startingEdge.start
        var nextPoint = startingEdge.end

        val vectorRoute = mutableListOf(startingPoint)
        val addedEdges = mutableListOf(startingEdge)

        while (addedEdges.size < edges.size) {
            val nextEdge = (edges - addedEdges).find { it.start == nextPoint || it.end == nextPoint } ?: return@lazy null
            vectorRoute.add(nextPoint)
            nextPoint = if (nextEdge.start == nextPoint) nextEdge.end else nextEdge.start
            addedEdges.add(nextEdge)
        }

        vectorRoute
    }
}
