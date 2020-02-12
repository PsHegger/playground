package pshegger.github.io.playground.visualsorting.algorithm

import android.graphics.Color
import androidx.core.util.set
import java.util.concurrent.TimeUnit

class BitonicSort(listener: AlgorithmChangeListener) :
    SortingAlgorithm(COUNT, 5, TimeUnit.MILLISECONDS, listener) {

    companion object {
        private const val LOG_N = 9
        private const val COUNT = 1 shl LOG_N
    }

    override val algorithmName = "Bitonic Sort"
    override val hideColumnMargin = true

    override fun run() {
        for (i in 0 until LOG_N) {
            for (j in 0..i) {
                kernel(i, j)
            }
        }
    }

    private fun kernel(p: Int, q: Int) {
        val d = 1 shl (p - q)

        for (i in 0 until size) {
            val up = ((i shr p) and 2) == 0
            val t = i or d

            overrideColors {
                delete((i - 1) or d)
                delete(i - 1)
                this[i] = Color.RED
                this[t] = Color.GREEN
            }
            dispatchUpdate()

            if ((i and d) == 0 && array.isBigger(i, t) == up) {
                array.swap(i, t)

                dispatchUpdate()
                sleep()
            }
        }

        overrideColors { clear() }
        dispatchUpdate()
    }
}
