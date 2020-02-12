package pshegger.github.io.playground.gamedev.algorithms.pathfinding

import pshegger.github.io.playground.gamedev.algorithms.maze.BaseMazeGenerator

abstract class BasePathFinder(val maze: List<List<BaseMazeGenerator.FieldValue>>) {
    val finished: Boolean
        get() = _states.size > 2 && _states.all { it.state == FieldState.FieldValue.StartStop || it.state == FieldState.FieldValue.Path }

    protected val _states: ArrayList<FieldState> = arrayListOf()
    val states: List<FieldState>
        get() = _states

    protected val start: Coordinate
    protected val stop: Coordinate

    abstract fun nextStep()

    init {
        val emptyCells = maze.mapIndexed { y, row ->
            row.mapIndexed { x, fieldValue ->
                if (fieldValue == BaseMazeGenerator.FieldValue.Empty) {
                    Coordinate(x, y)
                } else {
                    null
                }
            }
        }.flatten().filterNotNull()

        start = emptyCells.first()
        stop = emptyCells.last()

        _states.add(FieldState(start.x, start.y, FieldState.FieldValue.StartStop))
        _states.add(FieldState(stop.x, stop.y, FieldState.FieldValue.StartStop))
    }

    fun findPath() {
        while (!finished) {
            nextStep()
        }
    }

    protected fun getField(c: Coordinate) = maze[c.y][c.x]
    protected fun removeState(c: Coordinate) {
        val state = _states.firstOrNull { it.x == c.x && it.y == c.y }

        state?.let {
            _states.remove(it)
        }
    }

    protected fun addState(c: Coordinate, state: FieldState.FieldValue) {
        _states.add(FieldState(c.x, c.y, state))
    }

    protected fun changeState(c: Coordinate, state: FieldState.FieldValue) {
        removeState(c)
        addState(c, state)
    }

    protected fun getState(c: Coordinate) = _states.firstOrNull { it.x == c.x && it.y == c.y }?.state

    data class FieldState(val x: Int, val y: Int, val state: FieldValue) {
        enum class FieldValue {
            StartStop, Active, Path
        }
    }

    inner class Coordinate(val x: Int, val y: Int) {
        val valid: Boolean
            get() = x >= 0 && y >= 0 && y < maze.size && x < maze[y].size

        val possibleDestinations: List<Coordinate>
            get() = listOf(
                Coordinate(x - 1, y),
                Coordinate(x + 1, y),
                Coordinate(x, y - 1),
                Coordinate(x, y + 1)
            ).filter {
                it.valid && getField(it) == BaseMazeGenerator.FieldValue.Empty
            }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Coordinate

            if (x != other.x) return false
            if (y != other.y) return false

            return true
        }

        override fun hashCode(): Int {
            var result = x
            result = 31 * result + y
            return result
        }
    }
}
