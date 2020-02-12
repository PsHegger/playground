package pshegger.github.io.playground.visualsorting.algorithm

import android.graphics.Color
import androidx.core.util.set
import java.util.concurrent.TimeUnit

class BubbleSort(listener: AlgorithmChangeListener) :
    SortingAlgorithm(100, 10, TimeUnit.MILLISECONDS, listener) {

    override val algorithmName = "Bubble Sort"

    override fun run() {
        var n = size

        do {
            var swapped = false
            var i = updateColors(1)
            while (i < n) {
                if (array.isBigger(i - 1, i)) {
                    array.swap(i - 1, i)
                    swapped = true
                }
                dispatchUpdate()
                sleep()
                i = updateColors(i + 1)
            }
            n--
        } while (swapped && running)
        overrideColors { clear() }
        dispatchUpdate()
    }

    private fun updateColors(index: Int): Int {
        overrideColors {
            clear()
            this[index] = Color.RED
            this[index - 1] = Color.GREEN
        }
        return index
    }
}
