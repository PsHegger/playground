package pshegger.github.io.playground.gamedev.algorithms.pathfinding

import pshegger.github.io.playground.gamedev.algorithms.maze.BaseMazeGenerator
import java.util.*

class AStar(maze: List<List<BaseMazeGenerator.FieldValue>>, val heuristic: (Coordinate, Coordinate) -> Float, val tieBreaker: Float = 0.0002F) : BasePathFinder(maze) {
    private var pathFound = false
    private var unvisitedNodes = arrayListOf<NodeData>()
    private var visitedNodes = arrayListOf<NodeData>()
    private var currentNode: NodeData = NodeData(start, null, 0)
    private var pathBackTrack: Coordinate? = stop
    private var emptyBackTrack: Stack<NodeData> = Stack()

    override fun nextStep() {
        if (unvisitedNodes.isEmpty()) {
            val nodes = maze.mapIndexed { y, row ->
                row.mapIndexed { x, fieldValue ->
                    if (fieldValue == BaseMazeGenerator.FieldValue.Empty) {
                        Coordinate(x, y)
                    } else {
                        null
                    }
                }
            }.flatten().filterNotNull().map { NodeData(it, null) }

            unvisitedNodes.addAll(nodes)
            visitedNodes.add(currentNode)
        }

        if (!pathFound) {
            currentNode.c.unvisitedNeighborNodes().forEach { node ->
                if (currentNode.distance + 1 < node.distance) {
                    node.distance = currentNode.distance + 1
                    node.prev = currentNode.c
                }
            }

            unvisitedNodes.remove(currentNode)
            visitedNodes.add(currentNode)

            if (currentNode.c == stop) {
                pathFound = true
            } else {
                if (currentNode.c != start) {
                    changeState(currentNode.c, FieldState.FieldValue.Active)
                }

                currentNode = unvisitedNodes.filter { it.distance != Int.MAX_VALUE }.minBy { it.distance + heuristic(it.c, stop) * (1 + tieBreaker) }!!
            }
        } else {
            pathBackTrack?.let { c ->
                if (c != stop && c != start) {
                    changeState(c, FieldState.FieldValue.Path)
                }

                pathBackTrack = visitedNodes.first { it.c == c }.prev

                visitedNodes.filter { it.prev == pathBackTrack && getState(it.c) == FieldState.FieldValue.Active }
                    .forEach { buildEmptyStack(it) }
            } ?: let {
                if (!emptyBackTrack.empty()) {
                    removeState(emptyBackTrack.pop().c)
                }
            }
        }
    }

    private fun buildEmptyStack(n: NodeData) {
        emptyBackTrack.push(n)

        visitedNodes.filter { it.prev == n.c && getState(it.c) == FieldState.FieldValue.Active }
            .forEach { buildEmptyStack(it) }
    }

    private fun Coordinate.unvisitedNeighborNodes() = unvisitedNodes.filter { (c) -> possibleDestinations.any { dst -> dst == c } }

    private data class NodeData(val c: Coordinate, var prev: Coordinate?, var distance: Int = Int.MAX_VALUE)
}
