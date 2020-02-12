package pshegger.github.io.playground.visualsorting.algorithm

import android.graphics.Color
import androidx.core.util.set
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.log2

class BinaryRadixSort(listener: AlgorithmChangeListener) :
    SortingAlgorithm(2000, 1, TimeUnit.MILLISECONDS, listener) {

    override val algorithmName = "Radix Sort (Binary)"
    override val hideColumnMargin = true

    override fun run() {
        var maxIndex = 0
        for (i in 1 until size) {
            if (array.isBigger(i, maxIndex)) {
                maxIndex = i
            }
        }

        val maxShift = floor(log2(array[maxIndex].toDouble())).toInt()
        radix(0, size - 1, maxShift)
    }

    private fun radix(start: Int, end: Int, shift: Int) {
        if (shift < 0 || start > end) return

        var minI = start
        var maxI = end

        while (minI < maxI) {
            if (isOneBit(minI, shift)) {
                array.swap(minI, maxI)
                overrideColors { delete(maxI) }
                maxI--
            } else {
                overrideColors { delete(minI) }
                minI++
            }

            overrideColors {
                this[start] = Color.RED
                this[end] = Color.RED
                this[minI] = Color.GREEN
                this[maxI] = Color.GREEN
            }

            dispatchUpdate()
            sleep()
        }

        val pivot = if (isOneBit(minI, shift)) {
            minI - 1
        } else {
            minI
        }

        radix(start, pivot, shift - 1)
        radix(pivot + 1, end, shift - 1)

        overrideColors {
            delete(minI)
            delete(maxI)
            delete(start)
            delete(end)
        }
        dispatchUpdate()
    }

    private fun isOneBit(index: Int, shift: Int) = ((array[index] shr shift) and 1) == 1
}
