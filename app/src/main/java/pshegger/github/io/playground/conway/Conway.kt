package pshegger.github.io.playground.conway

import kotlin.random.Random

class Conway(val width: Int, val height: Int, private val birthChance: Float = 0.5f) {

    private val rng = Random(System.currentTimeMillis())
    private var grid = List(height) { List(width) { rng.nextFloat() < birthChance } }
    private val previousHashes = mutableListOf<Int>()

    fun reset() {
        grid = List(height) { List(width) { rng.nextFloat() < birthChance } }
    }

    fun nextStep() {
        if (resetIfRequired()) return

        grid = List(height) { y ->
            List(width) { x ->
                val currentValue = this[x, y]
                val liveNeighbourCount = liveNeighbourCount(x, y)

                if (currentValue) {
                    when {
                        liveNeighbourCount < 2 -> false
                        liveNeighbourCount > 3 -> false
                        else -> true
                    }
                } else {
                    liveNeighbourCount == 3
                }
            }
        }
    }

    private fun liveNeighbourCount(x: Int, y: Int): Int = (-1..1).sumOf { dy ->
        (-1..1).count { dx ->
            if (dx == 0 && dy == 0) {
                false
            } else {
                grid.getOrNull(y + dy)?.getOrNull(x + dx) ?: false
            }
        }
    }

    private fun resetIfRequired(): Boolean {
        previousHashes.add(grid.hashCode())
        if (previousHashes.size > 120) previousHashes.removeAt(0)

        val reset = previousHashes.any { hash -> previousHashes.count { it == hash } > 30 }

        if (reset) {
            reset()
            previousHashes.clear()
        }

        return reset
    }

    operator fun get(x: Int, y: Int) = grid[y][x]
}
