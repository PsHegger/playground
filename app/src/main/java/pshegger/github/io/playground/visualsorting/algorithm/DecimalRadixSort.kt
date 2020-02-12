package pshegger.github.io.playground.visualsorting.algorithm

import android.graphics.Color
import androidx.core.util.set
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

class DecimalRadixSort(listener: AlgorithmChangeListener) :
    SortingAlgorithm(2000, 1, TimeUnit.MILLISECONDS, listener) {

    override val algorithmName = "Radix Sort (Decimal)"
    override val hideColumnMargin = true

    override fun run() {
        var maxIndex = 0
        for (i in 1 until size) {
            if (array.isBigger(i, maxIndex)) {
                maxIndex = i
            }
        }

        val maxLog = floor(log10(array[maxIndex].toDouble()))
        val maxDiv = (10.0).pow(maxLog).toInt()
        radix(0, size - 1, maxDiv)

        overrideColors { clear() }
        dispatchUpdate()
    }

    private fun radix(start: Int, end: Int, div: Int) {
        if (div < 1 || start > end) return

        overrideColors {
            this[start] = Color.RED
            this[end] = Color.RED
        }
        dispatchUpdate()

        val bucketSizes = MutableList(10) { 0 }
        for (i in start..end) {
            overrideColors { delete(i - 1) }
            bucketSizes[bucket(i, div)]++
            overrideColors {
                this[start] = Color.RED
                this[end] = Color.RED
                this[i] = Color.CYAN
            }
            dispatchUpdate()
        }

        val bucketIndices = MutableList(10) { start }
        val originalIndices = MutableList(10) { start }
        for (i in 1 until 10) {
            bucketIndices[i] = bucketIndices[i - 1] + bucketSizes[i - 1]
            originalIndices[i] = bucketIndices[i]
        }

        var i = start

        while (i < end) {
            var currentBucket = originalIndices.indexOfFirst { it > i } - 1
            if (currentBucket < 0) {
                currentBucket = originalIndices.size - 1
            }
            val bucket = bucket(i, div)
            if (currentBucket != bucket) {
                array.swap(i, bucketIndices[bucket])
                overrideColors { delete(bucketIndices[bucket]) }
                bucketIndices[bucket]++
            } else {
                overrideColors { delete(i) }
                i++
            }

            overrideColors {
                this[start] = Color.RED
                this[end] = Color.RED
                this[i] = Color.GREEN
            }
            bucketIndices.forEach { overrideColors { this[it] = Color.CYAN } }
            dispatchUpdate()
            sleep()
        }
        bucketIndices.forEach { overrideColors { delete(it) } }
        overrideColors {
            this[start] = Color.RED
            this[end] = Color.RED
        }
        dispatchUpdate()

        radix(start, start + bucketSizes[0] - 1, div / 10)
        var startIndex = start
        for (i in 1 until 10) {
            startIndex += bucketSizes[i - 1]
            radix(startIndex, startIndex + bucketSizes[i] - 1, div / 10)
        }

        overrideColors {
            delete(start)
            delete(end)
            delete(i)
        }
    }

    private fun bucket(i: Int, div: Int) = (array[i] % (div * 10)) / div
}
