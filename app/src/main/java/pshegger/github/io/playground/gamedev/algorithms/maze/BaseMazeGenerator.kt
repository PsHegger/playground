package pshegger.github.io.playground.gamedev.algorithms.maze

import pshegger.github.io.playground.gamedev.utils.times

abstract class BaseMazeGenerator {
    enum class FieldValue {
        Empty, Wall, Active, NotProcessed
    }

    protected var width: Int = 0
    protected var height: Int = 0

    protected val _fields: ArrayList<ArrayList<FieldValue>> = arrayListOf()
    val fields: List<List<FieldValue>>
        get() = _fields

    val finished: Boolean
        get() = _fields.all { row -> row.all { it != FieldValue.NotProcessed } }

    abstract fun nextStep()

    fun generateAll() {
        while (!finished) {
            nextStep()
        }
    }

    open fun reset(width: Int, height: Int) {
        this.width = width
        this.height = height

        clearFields()
    }

    protected fun clearFields() {
        _fields.clear()
        repeat((1..height).count()) {
            val row = arrayListOf<FieldValue>()
            repeat((1..width).count()) {
                row.add(FieldValue.NotProcessed)
            }
            _fields.add(row)
        }
    }

    protected fun getField(c: Coordinate) = _fields[c.y][c.x]
    protected fun setField(c: Coordinate, value: FieldValue) {
        _fields[c.y][c.x] = value
    }

    protected fun buildTunnel(start: Coordinate, end: Coordinate) {
        val dirX = when {
            end.x < start.x -> -1
            start.x < end.x -> 1
            else -> 0
        }

        val dirY = when {
            end.y < start.y -> -1
            start.y < end.y -> 1
            else -> 0
        }

        var x = start.x
        var y = start.y
        while (getField(end) == FieldValue.NotProcessed) {
            _fields[y][x] = FieldValue.Empty
            x += dirX
            y += dirY
        }
    }

    protected inner class Coordinate(val x: Int, val y: Int) {
        val neighbors: List<Coordinate>
            get() = let { listOf(-1, 0, 1) }
                .let { it * it }
                .filter { Math.abs(it[0]) != Math.abs(it[1]) }
                .map { Coordinate(x + it[0], y + it[1]) }

        val valid: Boolean
            get() = x >= 0 && y >= 0 && x < width && y < height

        val possibleDestinations: List<Coordinate>
            get() = listOf(
                Coordinate(x - 2, y),
                Coordinate(x + 2, y),
                Coordinate(x, y - 2),
                Coordinate(x, y + 2)
            ).filter {
                it.valid && getField(it) == FieldValue.NotProcessed
            }
    }
}
