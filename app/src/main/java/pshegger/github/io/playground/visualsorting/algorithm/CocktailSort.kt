package pshegger.github.io.playground.visualsorting.algorithm

import android.graphics.Color
import androidx.core.util.set
import java.util.concurrent.TimeUnit

class CocktailSort(listener: AlgorithmChangeListener)
    : SortingAlgorithm(100, 10, TimeUnit.MILLISECONDS, listener) {

    override val algorithmName = "Cocktail Sort"

    override fun run() {
        var min = 1
        var max = size - 1

        while (min <= max && running) {
            var newMin = max
            var newMax = min

            var i = updateColors(min, -1)
            while (i <= max) {
                if (array.isBigger(i - 1, i)) {
                    array.swap(i - 1, i)
                    newMax = i
                }
                dispatchUpdate()
                sleep()
                i = updateColors(i + 1, -1)
            }

            max = newMax

            i = updateColors(max - 1, 1)
            while (i >= min) {
                if (array.isBigger(i - 1, i)) {
                    array.swap(i - 1, i)
                    newMin = i - 1
                }
                dispatchUpdate()
                sleep()
                i = updateColors(i - 1, 1)
            }

            min = newMin + 1
        }
        overrideColors { clear() }
        dispatchUpdate()
    }

    private fun updateColors(index: Int, d: Int): Int {
        overrideColors {
            clear()
            this[index] = Color.RED
            this[index + d] = Color.GREEN
        }
        return index
    }
}
