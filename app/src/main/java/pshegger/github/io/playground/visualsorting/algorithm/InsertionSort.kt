package pshegger.github.io.playground.visualsorting.algorithm

import android.graphics.Color
import androidx.core.util.set
import java.util.concurrent.TimeUnit

class InsertionSort(listener: AlgorithmChangeListener) :
    SortingAlgorithm(100, 10, TimeUnit.MILLISECONDS, listener) {

    override val algorithmName = "Insertion Sort"

    override fun run() {
        var i = 1
        while (i < size) {
            var j = i
            updateColors(i, j)
            while (j > 0 && array.isBigger(j - 1, j)) {
                array.swap(j, j - 1)
                j--
                updateColors(i, j)
                dispatchUpdate()
                sleep()
            }
            i++
            updateColors(i, j)
        }
        overrideColors { clear() }
        dispatchUpdate()
    }

    private fun updateColors(i: Int, j: Int) {
        overrideColors {
            clear()
            this[i] = Color.RED
            this[j] = Color.GREEN
        }
    }
}
