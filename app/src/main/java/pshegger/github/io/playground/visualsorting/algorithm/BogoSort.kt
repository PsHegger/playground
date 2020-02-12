package pshegger.github.io.playground.visualsorting.algorithm

import java.util.concurrent.TimeUnit
import kotlin.random.Random

class BogoSort(listener: AlgorithmChangeListener) :
    SortingAlgorithm(7, 10, TimeUnit.MILLISECONDS, listener) {

    override val algorithmName = "Bogo Sort"

    override fun run() {
        var sorted = isSorted()
        while (!sorted) {
            val i1 = Random.nextInt(size)
            val i2 = Random.nextInt(size)
            array.swap(i1, i2)
            dispatchUpdate()
            sleep()
            sorted = isSorted()
        }
    }

    private fun isSorted(): Boolean {
        for (i in 1 until size) {
            if (array.isBigger(i - 1, i)) {
                return false
            }
        }
        return true
    }
}
