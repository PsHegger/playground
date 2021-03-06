package pshegger.github.io.playground.gamedev.algorithms

data class Graph<T>(val nodes: List<T>, val edges: List<Edge<T>>) {
    fun shortestPath(start: T, end: T): List<T>? {
        val distances = nodes.associate { it to Float.POSITIVE_INFINITY }.toMutableMap()
        val previouses = nodes.associate { Pair<T, T?>(it, null) }.toMutableMap()
        val unvisitedNodes = nodes.toMutableList()

        distances[start] = 0f

        while (unvisitedNodes.isNotEmpty()) {
            val u = unvisitedNodes.sortedBy { distances[it] }.first()
            unvisitedNodes.remove(u)
            if (u == end) {
                break
            }

            neighbors(u).forEach { e ->
                val v = e.otherEnd(u)
                val alt = e.weight + distances[u]!!
                if (alt < distances[v]!!) {
                    distances[v] = alt
                    previouses[v] = u
                }
            }
        }

        val path = arrayListOf<T>()
        var u: T? = end
        if (previouses[u] != null || u == start) {
            while (u != null) {
                path.add(u)
                u = previouses[u]
            }
        } else {
            return null
        }

        return path.reversed()
    }

    fun isRouteAvailable(start: T, end: T) = shortestPath(start, end) != null

    fun neighbors(n: T): List<Edge<T>> = edges.filter { it.start == n || it.end == n }

    data class Edge<T>(val start: T, val end: T, val weight: Float = 1f) {
        fun otherEnd(n: T): T = if (n == start) end else start
    }
}
