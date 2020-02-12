package pshegger.github.io.playground.visualsorting.algorithm

import android.graphics.Color
import androidx.core.util.set
import java.util.concurrent.TimeUnit

class HeapSort(listener: AlgorithmChangeListener) :
    SortingAlgorithm(500, 10, TimeUnit.MILLISECONDS, listener) {

    override val algorithmName = "Heap Sort"
    override val hideColumnMargin = true

    override fun run() {
        for (i in (size / 2 - 1 downTo 0)) {
            heapify(size, i)
        }

        for (i in (size - 1 downTo 0)) {
            array.swap(0, i)
            overrideColors { this[i] = Color.RED }
            dispatchUpdate()
            sleep()
            heapify(i, 0)
            overrideColors { delete(i) }
        }
    }

    private fun heapify(n: Int, i: Int) {
        var largest = i
        val l = 2 * i + 1
        val r = 2 * i + 2

        overrideColors { this[i] = Color.GREEN }
        dispatchUpdate()

        if (l < n && array.isBigger(l, largest)) {
            largest = l
        }

        if (r < n && array.isBigger(r, largest)) {
            largest = r
        }

        if (largest != i) {
            array.swap(i, largest)
            heapify(n, largest)
        }

        overrideColors { delete(i) }
        dispatchUpdate()
        sleep()
    }
}
