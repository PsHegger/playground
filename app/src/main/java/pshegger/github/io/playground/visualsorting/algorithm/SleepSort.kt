package pshegger.github.io.playground.visualsorting.algorithm

import android.os.Handler
import android.os.Looper
import java.util.concurrent.TimeUnit

class SleepSort(listener: AlgorithmChangeListener) :
    SortingAlgorithm(200, 10, TimeUnit.MILLISECONDS, listener) {

    override val algorithmName = "Sleep Sort"
    private val handler = Handler(Looper.getMainLooper())

    override fun run() {
        var ctr = 0
        for (i in 0 until size) {
            val value = array[i]
            val delay = value * (size / 10L)
            handler.postDelayed({
                array[ctr++] = value
                dispatchUpdate()
            }, delay)
        }
    }
}
