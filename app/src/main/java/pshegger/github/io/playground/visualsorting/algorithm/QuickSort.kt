package pshegger.github.io.playground.visualsorting.algorithm

import android.graphics.Color
import androidx.core.util.set
import java.util.concurrent.TimeUnit

class QuickSort(listener: AlgorithmChangeListener) :
    SortingAlgorithm(1000, 1, TimeUnit.MILLISECONDS, listener) {

    override val algorithmName = "Quick Sort"
    override val hideColumnMargin = true

    override fun run() {
        quickSort()
        overrideColors { clear() }
        dispatchUpdate()
    }

    private fun quickSort(low: Int = 0, high: Int = array.size - 1) {
        overrideColors {
            this[low] = Color.RED
            this[high] = Color.RED
        }
        if (low < high) {
            val p = partition(low, high)
            quickSort(low, p - 1)
            quickSort(p + 1, high)
        }
        overrideColors {
            delete(low)
            delete(high)
        }
    }

    private fun partition(low: Int, high: Int): Int {
        var i = low
        for (j in low until high) {
            overrideColors { this[j] = Color.GREEN }
            if (array.isSmaller(j, high)) {
                array.swap(i, j)
                i++
            }
            dispatchUpdate()
            sleep()
            overrideColors { delete(j) }
        }
        array.swap(i, high)
        dispatchUpdate()
        sleep()

        return i
    }
}
