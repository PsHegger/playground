package pshegger.github.io.playground.visualsorting.algorithm

import android.graphics.Color
import androidx.core.util.set
import java.util.concurrent.TimeUnit

class ShellSort(listener: AlgorithmChangeListener) :
    SortingAlgorithm(500, 10, TimeUnit.MILLISECONDS, listener) {

    companion object {
        private val gaps = listOf(701, 301, 132, 57, 23, 10, 4, 1)
    }

    override val algorithmName = "Shell Sort"
    override val hideColumnMargin = true

    override fun run() {
        for (gap in gaps) {
            for (i in gap until size) {
                var j = i
                updateColors(i, j)
                while (j >= gap && array.isBigger(j - gap, j)) {
                    array.swap(j, j - gap)
                    j -= gap
                    updateColors(i, j)
                    dispatchUpdate()
                    sleep()
                }
                updateColors(i, j)
                dispatchUpdate()
            }
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
