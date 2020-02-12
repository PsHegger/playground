package pshegger.github.io.playground.gamedev.algorithms.maze

import java.util.*

class RandomDepthFirstGenerator : BaseMazeGenerator() {
    private var started = false
    private val possibleWays: Stack<Coordinate> = Stack()

    override fun nextStep() {
        if (!started) {
            started = true

            val start = Coordinate(0, 0)

            setField(start, FieldValue.Active)
            possibleWays.push(start)

            return
        }

        if (possibleWays.isEmpty()) {
            _fields.indices.firstOrNull { y -> _fields[y].any { it == FieldValue.NotProcessed } }
                ?.let { y ->
                    _fields[y].indices.filter { _fields[y][it] == FieldValue.NotProcessed }
                        .forEach { x ->
                            _fields[y][x] = FieldValue.Wall
                        }
                }
        } else {
            val last = possibleWays.peek()
            val destinations = last.possibleDestinations

            if (destinations.isNotEmpty()) {
                val dst = destinations.random()
                buildTunnel(last, dst)

                possibleWays.push(dst)
                setField(last, FieldValue.Active)
                setField(dst, FieldValue.Active)
            } else {
                setField(possibleWays.pop(), FieldValue.Empty)
            }
        }
    }

    override fun reset(width: Int, height: Int) {
        super.reset(width, height)

        started = false
        possibleWays.clear()
    }
}
